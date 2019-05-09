package it.polimi.ppap.solver;

import com.google.gson.*;
import it.polimi.deib.ppap.node.services.Service;
import it.polimi.ppap.service.AggregateServiceAllocation;
import it.polimi.ppap.topology.FogNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OplModSolver {

    final static String CPLEX_PATH = "/opt/ibm/ILOG/CPLEX_Studio128/opl/bin/x86-64_linux";

    String templateFilePath = "templates/ppap.dat";
    String oplDataFilePath = "data/ppap.dat";
    String oplModelFilePath = "model/ppap.mod";
    String oplResultsFilePath = "results/solution.dat";

    public void generateData(Map<FogNode, Map<Service, Float>> nodeServiceDemand){
        ClassLoader classLoader = getClass().getClassLoader();
        /*String absoluteTemplateFilePath = classLoader.getResource(templateFilePath).getPath();
        String absoluteDataFilePath = classLoader.getResource(oplDataFilePath).getPath();
        String absoluteModelFilePath = classLoader.getResource(oplModelFilePath).getPath();*/
        OplDataWritter oplDataWritter = new OplDataWritter(nodeServiceDemand, oplDataFilePath, templateFilePath);//TODO
        try {
            oplDataWritter.generateData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<FogNode, Map<Service, AggregateServiceAllocation>>
    solve(Map<FogNode, Map<Service, Float>> nodeServiceDemand){
        ClassLoader classLoader = getClass().getClassLoader();
        String absoluteDataFilePath = classLoader.getResource(oplDataFilePath).getPath();
        String absoluteModelFilePath = classLoader.getResource(oplModelFilePath).getPath();
        String absoluteResultsFilePath = classLoader.getResource(oplResultsFilePath).getPath();
        OplRun.oplRun(new String[]{"-v", "-de", absoluteResultsFilePath, absoluteModelFilePath, absoluteDataFilePath});
        String solution = null;
        try {
            solution = readResultsFromFile(absoluteResultsFilePath);
        } catch (IOException e) {
            e.printStackTrace();
            throw new OplSolutionNotFoundException();
        }
        return parseSolution(solution, nodeServiceDemand);
    }

    public class OplSolutionNotFoundException extends RuntimeException{}


    private Map<FogNode, Map<Service, AggregateServiceAllocation>>
    parseSolution(String solution, Map<FogNode, Map<Service, Float>> nodeServiceDemand){
        solution = solution.replaceAll("\n|  ", "");
        Pattern pattern = Pattern.compile("(Supply = (\\[.*\\]);)");
        Matcher match = pattern.matcher(solution);
        if(match.find()){
            String supply = match.group(2);
            supply = supply.replaceAll("(\\d )", "$1,");
            supply = supply.replaceAll("(\\] \\[)", "],[");
            String supplyJson = "{\"solution\": " + supply + "}";
            JsonParser jsonParser = new JsonParser();
            JsonElement jsonTree = jsonParser.parse(supplyJson);
            JsonObject solutionJsonObject = jsonTree.getAsJsonObject();
            JsonElement solutionJsonArray = solutionJsonObject.get("solution");
            return parseSourcesJsonArray((JsonArray) solutionJsonArray, nodeServiceDemand);
        }else
            throw new SolutionMalFormedException();
    }

    public class SolutionMalFormedException extends RuntimeException{}

    private Map<FogNode, Map<Service, AggregateServiceAllocation>>
    parseSourcesJsonArray(JsonArray solutionJsonArray,
                          Map<FogNode, Map<Service, Float>> nodeServiceDemand){
        Iterator<JsonElement> sourceIt = solutionJsonArray.iterator();
        Map<FogNode, Map<Service, AggregateServiceAllocation>> nodeServicePlacement = new TreeMap<>();
        Iterator<FogNode> sourceNodeIt = nodeServiceDemand.keySet().iterator();
        while(sourceIt.hasNext()){
            JsonArray sourceArray = (JsonArray) sourceIt.next();
            Iterator<JsonElement> nodeIt = sourceArray.iterator();
            FogNode sourceNode = sourceNodeIt.next();
            Iterator<FogNode> targetNodeIt = nodeServiceDemand.keySet().iterator();
            parseNodeJsonArray(nodeServiceDemand, nodeServicePlacement, nodeIt, sourceNode, targetNodeIt);
        }
        return nodeServicePlacement;
    }

    private void
    parseNodeJsonArray(Map<FogNode, Map<Service, Float>> nodeServiceDemand,
                       Map<FogNode, Map<Service, AggregateServiceAllocation>> nodeServicePlacement,
                       Iterator<JsonElement> nodeIt,
                       FogNode sourceNode,
                       Iterator<FogNode> targetNodeIt) {
        while(nodeIt.hasNext()){
            FogNode targetNode = targetNodeIt.next();
            if(!nodeServicePlacement.containsKey(targetNode))
                nodeServicePlacement.put(targetNode, new TreeMap<Service,AggregateServiceAllocation>());
            JsonArray functionArray = (JsonArray) nodeIt.next();
            Iterator<JsonElement> functionIt = functionArray.iterator();
            Iterator<Service> targetServiceIt = nodeServiceDemand.get(sourceNode).keySet().iterator();
            parseFunctiosJsonArray(nodeServiceDemand, nodeServicePlacement, sourceNode, targetNode, functionIt, targetServiceIt);
        }
    }

    private void
    parseFunctiosJsonArray(Map<FogNode, Map<Service, Float>> nodeServiceDemand,
                           Map<FogNode, Map<Service, AggregateServiceAllocation>> nodeServicePlacement,
                           FogNode sourceNode,
                           FogNode targetNode,
                           Iterator<JsonElement> functionIt,
                           Iterator<Service> targetServiceIt) {
        Map<Service, AggregateServiceAllocation> serviceAggregateDemand = nodeServicePlacement.get(targetNode);
        while(functionIt.hasNext()){
            JsonPrimitive allocationElement = (JsonPrimitive) functionIt.next();
            Service targetService = targetServiceIt.next();
            float placedFraction = allocationElement.getAsFloat();
            float demand = placedFraction * nodeServiceDemand.get(sourceNode).get(targetService);
            AggregateServiceAllocation aggregateServiceDemand = serviceAggregateDemand.getOrDefault(
                    targetService, new AggregateServiceAllocation());
            serviceAggregateDemand.put(targetService, aggregateServiceDemand);
            aggregateServiceDemand.addServiceAllocation(sourceNode, targetService, demand, placedFraction);
        }
    }

    private static String readResultsFromFile(String filePath) throws IOException {
        String content = new String ( Files.readAllBytes( Paths.get(filePath) ) );
        return content;
    }
}
