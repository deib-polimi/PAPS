package it.polimi.ppap.solver;

import it.polimi.deib.ppap.node.services.Service;
import it.polimi.ppap.common.scheme.PlacementAllocationSchema;
import it.polimi.ppap.model.FogNode;
import peersim.core.Node;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

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

    final Map<FogNode, Map<Service, Float>> nodeServiceDemand;

    public OplDataWritter(Map<FogNode, Map<Service, Float>> nodeServiceDemand, String oplDataFilePath, String templateFilePath){
        this.oplDataFilePath = oplDataFilePath;
        this.templateFilePath = templateFilePath;
        this.nodeServiceDemand = nodeServiceDemand;
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
        String sourceNodeDelayMatrix = buildSourceNodeDelayMatrix(nodeServiceDemand.size(), nodeServiceDemand.size(), maxSourceNodeDelay, colocatedSourceNodeDelay);
        template = template.replace(SOURCE_NODE_DELAY_MATRIX, sourceNodeDelayMatrix);
        writer.write(template);
    }

    private String buildDemandLevelMatrix() {
        StringBuilder sb = new StringBuilder("\n");
        for(Node node : nodeServiceDemand.keySet()){
            sb.append("[");
            List<String> functionDemandList = new ArrayList<>();
            for(Service service : nodeServiceDemand.get(node).keySet()){
                float serviceDemand = nodeServiceDemand.get(node).get(service);
                int memoryMultiplier = (int)(service.getMemory() / 128); //TODO
                functionDemandList.add(((int)serviceDemand * memoryMultiplier) + "");
            }
            sb.append(String.join(",", functionDemandList));
            sb.append("]");
            sb.append("\n");
        }
        return sb.toString();
    }

    private String buildSourceNodeDelayMatrix(int demandSourceNumber, int nodesNumber, int maxSourceNodeDelay, int colocatedSourceNodeDelay) {
        StringBuilder sb = new StringBuilder("\n");
        for(int d = 0; d < demandSourceNumber; d++){
            sb.append("[");
            List<String> sourceNodeDelayList = new ArrayList<>();
            for(int n = 0; n < nodesNumber; n++){
                if(d != n)
                    sourceNodeDelayList.add(getLinearRandomNumber(maxSourceNodeDelay) + ""); //TODO
                else
                    sourceNodeDelayList.add(colocatedSourceNodeDelay + "");
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
        Node firstNode = nodeServiceDemand.keySet().iterator().next();
        for(Service service : nodeServiceDemand.get(firstNode).keySet())
            functionsList.add("\"" + service + "\"");
        return String.join(",", functionsList);
    }



    private String readTemplateFile() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        String absoluteTemplateFilePath = classLoader.getResource(templateFilePath).getPath();
        String content = new String ( Files.readAllBytes( Paths.get(absoluteTemplateFilePath) ) );
        return content;
    }

    private void openFile() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        String absoluteDataFilePath = classLoader.getResource(oplDataFilePath).getPath();
        File outputFile = new File(absoluteDataFilePath);
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
