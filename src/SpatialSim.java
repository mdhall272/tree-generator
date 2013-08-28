import au.com.bytecode.opencsv.CSVWriter;
import dr.app.tools.NexusExporter;
import dr.evolution.tree.*;
import dr.evolution.util.*;
import dr.math.MathUtils;
import dr.math.distributions.Distribution;
import dr.math.distributions.ExponentialDistribution;
import dr.math.distributions.GammaDistribution;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;


import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: mhall
 * Date: 04/06/2013
 * Time: 15:20
 * To change this template use File | Settings | File Templates.
 */
public class SpatialSim {

    private ArrayList<SpatialCase> cases;
    private double transmissionRate;
    private double kernelDispersion;
    private double growthRate;
    private double coalescentStep;
    private FlexibleTree transmissionTree;
    private FlexibleTree phylogeneticTree;
    private int currentStep;
    private HashMap<SpatialCase, SpatialCase> whoInfectedWho;
    private SpatialCase firstCase;
    private HashMap<SpatialCase,Boolean> causesAnInfection;
    private HashMap<Integer, Distribution> latentPeriods;
    private HashMap<Integer, Distribution> infectiousPeriods;
    private Visuals visuals;
    private DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy");
    private DateTime firstDay;
    private String pTreeFileName;
    private String tTreeFileName;
    private String ppTreeFileName;
    private String dataTableFileName;
    private String csvNetworkFileName;
    private boolean useGeography = false;

    public SpatialSim(int noCases, double transmissionRate, double kernelDispersion,
                      HashMap<Integer, Distribution> latentPeriods, HashMap<Integer, Distribution> infectiousPeriods,
                      double growthRate, double coalescentStep, Visuals visuals, String beginning,
                      String pTreeFileName, String tTreeFileName, String ppTreeFileName, String dataTableFileName,
                      String csvNetworkFileName){
        this.transmissionRate = transmissionRate;
        this.kernelDispersion = kernelDispersion;
        this.infectiousPeriods = infectiousPeriods;
        this.latentPeriods = latentPeriods;
        cases = new ArrayList<SpatialCase>();
        this.visuals = visuals;
        for(int i=0; i<noCases; i++){
            double[] coords = {MathUtils.nextDouble(), MathUtils.nextDouble()};
            int latentType = MathUtils.nextInt(latentPeriods.size());
            int infectiousType = MathUtils.nextInt(infectiousPeriods.size());
            cases.add(new SpatialCase(i, latentType, infectiousType, coords, visuals));
        }
        firstDay = formatter.parseDateTime(beginning);
        currentStep = 0;
        whoInfectedWho = new HashMap<SpatialCase,SpatialCase>();
        FlexibleNode transmissionRoot = new FlexibleNode();
        transmissionTree = new FlexibleTree(transmissionRoot, true, true);
        this.growthRate = growthRate;
        this.coalescentStep = coalescentStep;
        causesAnInfection = new HashMap<SpatialCase, Boolean>();
        for(SpatialCase thisCase : cases){
            causesAnInfection.put(thisCase,false);
        }
        this.pTreeFileName = pTreeFileName;
        this.tTreeFileName = tTreeFileName;
        this.ppTreeFileName = ppTreeFileName;
        this.dataTableFileName = dataTableFileName;
        this.csvNetworkFileName = csvNetworkFileName;
    }

    public void step(){
        for(SpatialCase thisCase: cases){
            if(thisCase.isSusceptibleAt(currentStep)){
                double force = forceOnA(thisCase, currentStep);
                double random = MathUtils.nextDouble();
                if(random<force){
                    SpatialCase infector = pickMeAnInfector(thisCase, currentStep);
                    whoInfectedWho.put(thisCase, infector);
                    causesAnInfection.put(infector,true);
                    thisCase.setInfectionDay(currentStep);
                    setInfectiousness(thisCase, currentStep);
                }
            }
        }
        currentStep++;
    }

    private void setInfectiousness(SpatialCase thisCase, int time){
        int daysLatent = 0;

        if(latentPeriods.get(thisCase.getLatentType())!=null){
            double random2 = MathUtils.nextDouble();
            boolean stillLatent = true;
            daysLatent = 1;
            while(stillLatent){
                if(latentPeriods.get(thisCase.getLatentType()).cdf(daysLatent)>random2){
                    stillLatent = false;
                } else {
                    daysLatent++;
                }
            }
        }
        thisCase.setInfectiousDay(time+daysLatent);

        double random3 = MathUtils.nextDouble();
        boolean stillInfectious = true;
        int daysInfectious = 1;
        while(stillInfectious){
            if(infectiousPeriods.get(thisCase.getInfectiousType()).cdf(daysInfectious)>random3){
                stillInfectious = false;
            } else {
                daysInfectious++;
            }
        }
        thisCase.setCullDay(time+daysLatent+daysInfectious);
    }

    private double forceOnA(SpatialCase thisCase, int time){
        double total = 0;
        for(SpatialCase otherCase : cases){
            if(thisCase!=otherCase){
                total+=forceOnADueToB(thisCase, otherCase, time);
            }
        }
        return total;
    }

    private boolean infectionsRemain(int time){
        boolean out = false;
        for(SpatialCase thisCase: cases){
            if(thisCase.isInfectedAt(time)){
                out = true;
                break;
            }
        }
        return out;
    }

    private SpatialCase pickMeAnInfector(SpatialCase thisCase, int time){
        double[] forces = new double[cases.size()];
        for(SpatialCase otherCase : cases){
            if(thisCase!=otherCase){
                forces[cases.indexOf(otherCase)]=forceOnADueToB(thisCase, otherCase, time);
            }
        }
        int pick = MathUtils.randomChoicePDF(forces);
        return cases.get(pick);
    }

    private double forceOnADueToB(SpatialCase caseA, SpatialCase caseB, int time){
        if(caseB.isInfectiousAt(time)){
            if(useGeography){
                return transmissionRate*Math.exp(-kernelDispersion * distance(caseA.getCoords(),caseB.getCoords()));
            } else {
                return transmissionRate;
            }
        } else {
            return 0;
        }
    }

    private double distance(double[] a, double[] b){
        return Math.sqrt(Math.pow(a[0]-b[0],2)+Math.pow(a[1]-b[1],2));
    }

    public void runSim(){
        firstCase = cases.get(MathUtils.nextInt(cases.size()));
        whoInfectedWho.put(firstCase,null);
        firstCase.setInfectionDay(-1);
        setInfectiousness(firstCase, -1);
        while(infectionsRemain(currentStep)){
            step();
        }
        makeTransmissionTree();
        outputTree(transmissionTree, tTreeFileName);
        makePhylogeneticTree();
        outputTree(phylogeneticTree, ppTreeFileName);
        outputTree(makeWellBehavedTree(), pTreeFileName);
        outputCSVNetwork(csvNetworkFileName);
        outputDataTable(dataTableFileName, firstDay);
    }

    private void makeTransmissionTree(){
        transmissionTree.beginTreeEdit();
        transmissionTree.setNodeAttribute(transmissionTree.getRoot(), "location", firstCase.getName());
        buildBranches(firstCase, (FlexibleNode)transmissionTree.getRoot());
        transmissionTree.endTreeEdit();
        transmissionTree = new FlexibleTree((FlexibleNode)transmissionTree.getRoot(), false, true);
    }

    private void makePhylogeneticTree(){
        phylogeneticTree = new FlexibleTree(transmissionTree, true);

        for(SpatialCase thisCase: cases){
            if(causesAnInfection.get(thisCase)){
                rewire(thisCase, doWithinCaseSimulation(thisCase));
            }
        }
    }

    private FlexibleTree doWithinCaseSimulation(SpatialCase thisCase){
        Taxa taxa = new Taxa();
        double activeTime = thisCase.getCullDay() - thisCase.getInfectionDay();
        Taxon cullTaxon = new Taxon(thisCase.getName()+"|");
        taxa.addTaxon(cullTaxon);
        cullTaxon.setDate(new Date(activeTime, Units.Type.DAYS, false));
        for(SpatialCase potentialChildCase : cases){
            if(whoInfectedWho.get(potentialChildCase)==thisCase){
                double timeToTransmission = potentialChildCase.getInfectionDay() - thisCase.getInfectionDay();
                Taxon transmissionTaxon = new Taxon(potentialChildCase.getName()+"|");
                transmissionTaxon.setDate(new Date(timeToTransmission, Units.Type.DAYS, false));
                taxa.addTaxon(transmissionTaxon);
            }
        }
        return simulateCoalescent(taxa, growthRate, coalescentStep);
    }


    public void outputCSVNetwork(String fileName){
        try{
            CSVWriter csvOut = new CSVWriter(new FileWriter(fileName));
            String[] header = new String[2];
            header[0] = "Child";
            header[1] = "Parent";
            csvOut.writeNext(header);
            for(SpatialCase thisCase : cases){
                String childID = thisCase.getName();
                String infectorID;
                try{
                    infectorID = whoInfectedWho.get(thisCase).getName();
                } catch(NullPointerException e){
                    infectorID = "Start";
                }
                String[] line = new String[2];
                line[0]=childID;
                line[1]=infectorID;
                csvOut.writeNext(line);
            }
            csvOut.close();
        }
        catch(IOException e){
            System.out.println("IOException");
        }
    }

    private void outputDataTable(String fileName, DateTime firstInfection){
        try{
            CSVWriter csvOut = new CSVWriter(new FileWriter(fileName));
            String[] header = new String[8];
            header[0] = "Farm_ID";
            header[1] = "Taxon";
            header[2] = "Infection_date";
            header[3] = "Infectious_date";
            header[4] = "Exam_date";
            header[5] = "Cull_date";
            header[6] = "Latitude";
            header[7] = "Longitude";
            csvOut.writeNext(header);

            for(SpatialCase aCase : cases){
                if(aCase.wasEverInfected()){
                    DateTime infectionDate = firstInfection.plusDays(1 + aCase.getInfectionDay());
                    DateTime infectiousDate = firstInfection.plusDays(1 + aCase.getInfectiousDay());
                    DateTime cullDate = firstInfection.plusDays(1 + aCase.getCullDay());
                    // and for now:
                    DateTime examDate = cullDate;
                    double latitude = aCase.getCoords()[0];
                    double longitude = aCase.getCoords()[1];

                    String[] line = new String[8];
                    line[0] = aCase.getName();
                    line[1] = aCase.getName()+"|"+examDate.getDayOfMonth()+"_"
                            +examDate.getMonthOfYear()+"_"
                            +examDate.getYear();
                    line[2] = formatter.print(infectionDate);
                    line[3] = formatter.print(infectiousDate);
                    line[4] = formatter.print(examDate);
                    line[5] = formatter.print(cullDate);
                    line[6] = Double.toString(latitude);
                    line[7] = Double.toString(longitude);
                    csvOut.writeNext(line);
                }
            }
            csvOut.close();
        } catch(IOException e){
            System.out.println("IOException");
        }
    }

    private FlexibleTree simulateCoalescent(Taxa taxa, double growthRate, double step){
        double maxTime = 0;
        ArrayList<FlexibleNode> nodes = new ArrayList<FlexibleNode>();
        ArrayList<FlexibleNode> activeNodes = new ArrayList<FlexibleNode>();
        ArrayList<FlexibleNode> defunctNodes = new ArrayList<FlexibleNode>();
        for(Taxon taxon : taxa){
            FlexibleNode node = new FlexibleNode();
            node.setTaxon(taxon);
            nodes.add(node);
            if(taxon.getDate().getTimeValue()>maxTime){
                maxTime = taxon.getDate().getTimeValue();
            }
        }
        double currentTime = maxTime;
        for(FlexibleNode node : nodes){
            node.setHeight(maxTime - node.getTaxon().getDate().getTimeValue());
            if(node.getTaxon().getDate().getTimeValue()==maxTime){
                activeNodes.add(node);
            }
        }
        while(currentTime-step>=0){
            // if anything has appeared in this time step:
            for(FlexibleNode node : nodes){
                if(node.getTaxon().getDate().getTimeValue()>=currentTime && !defunctNodes.contains(node) &&
                        !activeNodes.contains(node)){
                    activeNodes.add(node);
                }
            }
            int active = activeNodes.size();
            if(active>=2){
                int nextStepPopSize = getPopulationSize(growthRate, currentTime-step);
                double a = active*(active-1);
                double b = 2*nextStepPopSize;
                double probCoalescence = a/b;
                double randomDraw = MathUtils.nextDouble();
                if(randomDraw<probCoalescence){
                    // coalescence has happened
                    int firstIndex = MathUtils.nextInt(active);
                    int secondIndex = firstIndex;
                    while(secondIndex==firstIndex){
                        secondIndex = MathUtils.nextInt(active);
                    }
                    FlexibleNode firstNode = activeNodes.get(firstIndex);
                    FlexibleNode secondNode = activeNodes.get(secondIndex);
                    FlexibleNode newNode = new FlexibleNode();
                    newNode.addChild(firstNode);
                    newNode.addChild(secondNode);
                    defunctNodes.add(firstNode);
                    defunctNodes.add(secondNode);
                    activeNodes.remove(firstNode);
                    activeNodes.remove(secondNode);
                    newNode.setHeight(maxTime - currentTime);
                    activeNodes.add(newNode);
                }
            }
//          Unnecessary?
//            for(FlexibleNode node : nodes){
//                if(node.getTaxon().getDate().getTimeValue()>=currentTime && !defunctNodes.contains(node) &&
//                        !activeNodes.contains(node)){
//                    activeNodes.add(node);
//                }
//            }
            currentTime -= step;
        }
        // find the root
        FlexibleNode aNode = nodes.get(0);
        while(aNode.getParent()!=null){
            aNode = aNode.getParent();
        }

        FlexibleTree aTree = new FlexibleTree(aNode, true, false);

        return aTree;
    }


    private static int getPopulationSize(double growthRate, double time){
        return (int)Math.ceil(Math.pow(1+growthRate,time));
    }

    private void rewire(SpatialCase thisCase, FlexibleTree replacement){

        ArrayList<FlexibleNode> nodesForRewiring = findSubgraph(thisCase);
        FlexibleNode parent = null;
        // find the root of the subtree for rewiring
        for(FlexibleNode node : nodesForRewiring){
            if(phylogeneticTree.getParent(node)!=null &&
                    !phylogeneticTree.getNodeAttribute(phylogeneticTree.getParent(node),"location")
                            .equals(phylogeneticTree.getNodeAttribute(node, "location"))){
                parent = (FlexibleNode)phylogeneticTree.getParent(node);
                break;
            }
        }
        if(parent==null){
            parent = (FlexibleNode)phylogeneticTree.getRoot();
            nodesForRewiring.remove(parent);
        }
        for(FlexibleNode node: nodesForRewiring){
            ((FlexibleNode)phylogeneticTree.getParent(node)).removeChild(node);
        }
        HashMap<FlexibleNode,FlexibleNode> matching = new HashMap<FlexibleNode,FlexibleNode>();
        for(FlexibleNode node: nodesForRewiring){
            if(phylogeneticTree.getChildCount(node)>=2){
                throw new RuntimeException("Node retains two children");
            } else if(phylogeneticTree.getChildCount(node)==0){
                for(int i=0; i<replacement.getExternalNodeCount(); i++){
                    FlexibleNode replacementNode = (FlexibleNode)replacement.getExternalNode(i);
                    if(replacement.getNodeTaxon(replacementNode).getId()
                            .startsWith(phylogeneticTree.getNodeAttribute(node, "location")+"|")){
                        matching.put(replacementNode,node);
                    }
                }
            } else {
                for(int i=0; i<replacement.getExternalNodeCount(); i++){
                    FlexibleNode replacementNode = (FlexibleNode)replacement.getExternalNode(i);
                    if(replacement.getNodeTaxon(replacementNode).getId()
                            .startsWith(phylogeneticTree.getNodeAttribute(node.getChild(0), "location")+"|")){
                        matching.put(replacementNode,node);
                    }
                }
            }
        }
        FlexibleNode replacementRoot = (FlexibleNode)replacement.getRoot();

        FlexibleNode coalescentRootIncorporated = rewireDown(thisCase, replacement, replacementRoot,
                thisCase.getCullDay(), matching);
        parent.addChild(coalescentRootIncorporated);
        coalescentRootIncorporated.setLength(thisCase.getCullDay() - thisCase.getInfectionDay()
                - replacementRoot.getHeight());
        if(thisCase.getCullDay() - thisCase.getInfectionDay() - replacementRoot.getHeight()<0){
            System.out.println("WARNING: coalescence before infection!");
        }
        if(thisCase.getCullDay() - thisCase.getInfectionDay() - replacementRoot.getHeight()<-1){
            System.out.println("WARNING: coalescence more than 1 day before infection!");
        }

        FlexibleTree aTree = new FlexibleTree((FlexibleNode)phylogeneticTree.getRoot(), true, false);

        phylogeneticTree = aTree;
    }

    private int numberOfInfections(){
        int count = 0;
        for(SpatialCase aCase : cases){
            if(aCase.wasEverInfected()){
                count++;
            }
        }
        return count;
    }

    private FlexibleNode rewireDown(SpatialCase thisCase, FlexibleTree replacement, FlexibleNode oldNode,
                                    double cullDate, HashMap<FlexibleNode,FlexibleNode> map){
        if(replacement.isExternal(oldNode)){
            return map.get(oldNode);
        } else {
            FlexibleNode newNode = new FlexibleNode();
            newNode.setHeight((currentStep - 1 - cullDate) + oldNode.getHeight());
            for(int i=0; i<oldNode.getChildCount(); i++){
                newNode.addChild(rewireDown(thisCase, replacement, oldNode.getChild(i), cullDate, map));
            }
            newNode.setAttribute("location", thisCase.getName());

            return newNode;
        }
    }

    private ArrayList<FlexibleNode> findSubgraph(SpatialCase thisCase){
        ArrayList<FlexibleNode> out = new ArrayList<FlexibleNode>();
        for(int i=0; i<phylogeneticTree.getNodeCount(); i++){
            FlexibleNode node = (FlexibleNode)phylogeneticTree.getNode(i);
            if(phylogeneticTree.getNodeAttribute(node, "location").equals(thisCase.getName())){
                out.add(node);
            }
        }
        return out;
    }

    private void buildBranches(SpatialCase thisCase, FlexibleNode connectingNode){
        ArrayList<SpatialCase> children = new ArrayList<SpatialCase>();
        for(SpatialCase possibleChild : cases){
            if(whoInfectedWho.get(possibleChild)==thisCase){
                children.add(possibleChild);
            }
        }
        Collections.sort(children);
        FlexibleNode parentNode = connectingNode;
        for(SpatialCase child : children){
            FlexibleNode infNode = new FlexibleNode();
            transmissionTree.addChild(parentNode, infNode);
            transmissionTree.setBranchLength(infNode, (child.getInfectionDay() + 1)
                    - transmissionTree.getNodeHeight(parentNode));
            transmissionTree.setNodeAttribute(infNode, "location", thisCase.getName());
            buildBranches(child, infNode);
            parentNode = infNode;
        }
        FlexibleNode cullNode = new FlexibleNode();
        transmissionTree.addChild(parentNode, cullNode);
        transmissionTree.setBranchLength(cullNode, (thisCase.getCullDay() + 1)
                - transmissionTree.getNodeHeight(parentNode));

        DateTime examDate = firstDay.plusDays(1 + thisCase.getExamDay());

        transmissionTree.setNodeTaxon(cullNode, new Taxon(thisCase.getName()+"|"+examDate.getDayOfMonth()+"_"
                +examDate.getMonthOfYear()+"_"
                +examDate.getYear()));

        cullNode.setAttribute("location", thisCase.getName());
    }

    public ArrayList<SpatialCase> getCases(){
        return cases;
    }

    public HashMap<SpatialCase, SpatialCase> getWIW(){
        return whoInfectedWho;
    }

    private void outputTree(FlexibleTree tree, String fileName){
        try{
            PrintStream tWriter = new PrintStream(fileName);
            NexusExporter exporter = new NexusExporter(tWriter);
            exporter.exportTree(tree);
            tWriter.flush();
        } catch(IOException e){
            System.err.println("IOException");
        }
    }

    private FlexibleTree makeWellBehavedTree(){
        FlexibleTree newPhylogeneticTree = new FlexibleTree(phylogeneticTree, true);
        newPhylogeneticTree.beginTreeEdit();
        for(int i=0; i<newPhylogeneticTree.getInternalNodeCount(); i++){
            FlexibleNode node = (FlexibleNode)newPhylogeneticTree.getInternalNode(i);
            if(newPhylogeneticTree.getChildCount(node)==1){
                FlexibleNode parent = (FlexibleNode)newPhylogeneticTree.getParent(node);
                FlexibleNode child = (FlexibleNode)newPhylogeneticTree.getChild(node, 0);
                if(parent!=null){
                    double childHeight = newPhylogeneticTree.getNodeHeight(child);
                    newPhylogeneticTree.removeChild(parent, node);
                    newPhylogeneticTree.addChild(parent, child);
                    newPhylogeneticTree.setNodeHeight(child, childHeight);
                } else {
                    child.setParent(null);
                    newPhylogeneticTree.setRoot(child);
                }
            }
        }
        newPhylogeneticTree.endTreeEdit();
        return new FlexibleTree(newPhylogeneticTree);
    }

    public static void main(String[] args){
        try{
            BufferedWriter truthTeller = new BufferedWriter(new FileWriter("trueValues.csv"));
            truthTeller.write("Number,Transmission_rate,Infectious_mean,Infectious_sd");
            truthTeller.newLine();
            for(int i=1; i<11; i++){
                double trRate=0;
                double infmean;
                double infSD;

                double random_k=0;
                double random_theta=0;

                boolean bigenough = false;
                while(!bigenough){

                    trRate = MathUtils.nextDouble()*0.1;
                    infmean = MathUtils.nextDouble()*9 + 1;
                    infSD = MathUtils.nextDouble()*infmean*0.5;

                    random_k = Math.pow(infmean,2)/Math.pow(infSD,2);
                    random_theta = Math.pow(infSD,2)/infmean;

                    HashMap<Integer, Distribution> latentPeriods = new HashMap<Integer, Distribution>();
                    latentPeriods.put(0, null);

                    HashMap<Integer, Distribution> infectiousPeriods = new HashMap<Integer, Distribution>();
                    infectiousPeriods.put(0, new GammaDistribution(random_k, random_theta));




                    SpatialSim simulation = new SpatialSim(20, trRate, 0.1, latentPeriods, infectiousPeriods, 15, 0.01, null,
                            "01/12/2011", "pTree_"+i+".nex", "tTree_"+i+".nex", "ppTree_"+i+".nex", "data_"+i+".csv",
                            "network_"+i+".csv");
                    simulation.runSim();
                    if(simulation.numberOfInfections()>15){
                        bigenough = true;
                    }
                }

                String line = Integer.toString(i)+","+Double.toString(trRate)+","+Double.toString(random_k)+","+Double.toString(random_theta);

                truthTeller.write(line);
                truthTeller.newLine();

            }
            truthTeller.flush();
        } catch(IOException e){
            System.out.print("IOException");
        }
    }

}
