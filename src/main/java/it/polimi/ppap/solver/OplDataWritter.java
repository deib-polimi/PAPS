package it.polimi.ppap.solver;

import it.polimi.deib.ppap.node.services.Service;
import it.polimi.ppap.service.ServiceDemand;
import it.polimi.ppap.topology.node.FogNode;
import peersim.core.Node;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class OplDataWritter {

    String oplDataFilePath, templateFilePath;
    FileWriter writer;
    Random rand = new Random();

    private final String MIGRATION_COST = "$MIGRATION_COST";
    private final String NODE_NAME_LIST = "$NODE_NAME_LIST";
    private final String CAPACITY_LIST = "$CAPACITY_LIST";
    private final String FUNCTION_NAME_LIST = "$FUNCTION_NAME_LIST";
    private final String DEMAND_SOURCE_N = "$DEMAND_SOURCE_N";
    private final String DEMAND_LEVEL_MATRIX = "$DEMAND_LEVEL_MATRIX";
    private final String SOURCE_NODE_DELAY_MATRIX = "$SOURCE_NODE_DELAY_MATRIX";
    private final String MAX_DELAY = "$MAX_DELAY";

    final Set<Service> admittedServices;
    final Map<FogNode, Map<Service, ServiceDemand>> nodeServiceDemand;

    public OplDataWritter(Set<Service> admittedServices, Map<FogNode, Map<Service, ServiceDemand>> nodeServiceDemand, float optimizationBeta, String oplDataFilePath, String templateFilePath){
        this.admittedServices = admittedServices;
        this.nodeServiceDemand = nodeServiceDemand;
        this.oplDataFilePath = oplDataFilePath;
        this.templateFilePath = templateFilePath;
        this.optimizationBeta = optimizationBeta;
    }

    public void generateData() throws IOException {
        openFile();
        writeFile();
        closeFile();
    }

    //TODO parametrize in configs
    final String migrationCost = "1";
    final int baseCapacity = 1024;
    final int colocatedSourceNodeDelay = 2;
    final int maxSourceNodeDelay = 30;
    final float optimizationBeta;

    private void writeFile() throws IOException {
        String template = readTemplateFile();
        template = template.replace(MIGRATION_COST, migrationCost);
        String nodesList = buildNodesList();
        template = template.replace(NODE_NAME_LIST, nodesList);
        String capacityList = buildCapacityList(baseCapacity);
        template = template.replace(CAPACITY_LIST, capacityList);
        String functionsList = buildFunctionsList();
        template = template.replace(FUNCTION_NAME_LIST, functionsList);
        template = template.replace(DEMAND_SOURCE_N, nodeServiceDemand.size() + "");
        String demandLevelMatrix = buildDemandLevelMatrix();
        template = template.replace(DEMAND_LEVEL_MATRIX, demandLevelMatrix);
        String sourceNodeDelayMatrix = buildSourceNodeDelayMatrix();
        //buildSourceNodeDelayMatrix(nodeServiceDemand.size(), nodeServiceDemand.size(), maxSourceNodeDelay, colocatedSourceNodeDelay);
        template = template.replace(SOURCE_NODE_DELAY_MATRIX, sourceNodeDelayMatrix);
        String maxDelay = buildServiceDelayConstraintVector();
        template = template.replace(MAX_DELAY, maxDelay);
        writer.write(template);
    }

    private String buildDemandLevelMatrix() {
        StringBuilder sb = new StringBuilder("\n");
        for(Node node : nodeServiceDemand.keySet()){
            sb.append("[");
            List<String> functionDemandList = new ArrayList<>();
            for(Service service : nodeServiceDemand.get(node).keySet()){
                int serviceDemand = (int) Math.ceil(nodeServiceDemand.get(node).get(service).getDemand());
                int memoryMultiplier = (int)(service.getMemory() / 128); //TODO
                functionDemandList.add(((int)serviceDemand * memoryMultiplier) + "");
            }
            sb.append(String.join(",", functionDemandList));
            sb.append("]");
            sb.append("\n");
        }
        return sb.toString();
    }

    private String buildSourceNodeDelayMatrix(){
        StringBuilder sb = new StringBuilder("\n");
        for(FogNode sourceNode : nodeServiceDemand.keySet()){
            sb.append("[");
            List<String> interNodeDelayList = new ArrayList<>();
            for(FogNode targetNode : nodeServiceDemand.keySet()){
                interNodeDelayList.add(sourceNode.getLinkDelay(targetNode.getID()) + "");
            }
            sb.append(String.join(",", interNodeDelayList));
            sb.append("]");
            sb.append("\n");
        }
        return sb.toString();
    }

    private String buildServiceDelayConstraintVector(){
        StringBuilder sb = new StringBuilder("");
        sb.append("[");
        List<String> delayConstraintList = new ArrayList<>();
        for(Service service : admittedServices){
            int delayConstraint = (int) (optimizationBeta * (service.getRT() - service.getET()));
            delayConstraintList.add(delayConstraint + "");
        }
        sb.append(String.join(",", delayConstraintList));
        sb.append("]");
        return sb.toString();
    }

    private String buildSourceNodeDelayMatrix(int demandSourceNumber, int nodesNumber, int maxSourceNodeDelay, int fronthaulSourceNodeDelay) {
        StringBuilder sb = new StringBuilder("\n");
        for(int d = 0; d < demandSourceNumber; d++){
            sb.append("[");
            List<String> sourceNodeDelayList = new ArrayList<>();
            for(int n = 0; n < nodesNumber; n++){
                if(d != n)
                    sourceNodeDelayList.add(getLinearRandomNumber(maxSourceNodeDelay) + ""); //TODO
                else
                    sourceNodeDelayList.add(fronthaulSourceNodeDelay + "");
            }
            sb.append(String.join(",", sourceNodeDelayList));
            sb.append("]");
            sb.append("\n");
        }
        return sb.toString();
    }

    public int getLinearRandomNumber(int maxSize){
        //Get a linearly multiplied random number
        int randomMultiplier = maxSize * (maxSize + 1) / 2;
        int randomInt = rand.nextInt(randomMultiplier);

        //Linearly iterate through the possible values to find the correct one
        int linearRandomNumber = 0;
        for(int i=maxSize; randomInt >= 0; i--){
            randomInt -= i;
            linearRandomNumber++;
        }
        return linearRandomNumber;
    }

    private String buildNodesList() {
        List<String> nodeList = new ArrayList<>();
        for(Node node : nodeServiceDemand.keySet())
            nodeList.add("\"Node_" + node.getID() + "\"");
        return String.join(",", nodeList);
    }

    private String buildCapacityList(int baseCapacity) {
        List<String> nodeList = new ArrayList<>();
        for(FogNode node : nodeServiceDemand.keySet())
            nodeList.add(node.getMemoryCapacity()/baseCapacity + "");
        return String.join(",", nodeList);
    }

    private String buildFunctionsList() {
        List<String> functionsList = new ArrayList<>();
        for(Service service : admittedServices)
            functionsList.add("\"" + service + "\"");
        return String.join(",", functionsList);
    }



    private String readTemplateFile() throws IOException {
        String content = new String ( Files.readAllBytes( Paths.get(templateFilePath) ) );
        return content;
    }

    private void openFile() throws IOException {
        File outputFile = new File(oplDataFilePath);
        if(outputFile.exists()) {
            outputFile.delete();
            outputFile.createNewFile();
        }
        writer = new FileWriter(outputFile);
    }

    private void closeFile() throws IOException {
        writer.close();
    }
}
