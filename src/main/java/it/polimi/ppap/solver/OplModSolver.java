package it.polimi.ppap.solver;

import it.polimi.deib.ppap.node.services.Service;
import peersim.core.Node;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class OplModSolver {

    final static String CPLEX_PATH = "/opt/ibm/ILOG/CPLEX_Studio128/opl/bin/x86-64_linux";

    String templateFilePath = "templates/ppap.dat";
    String oplDataFilePath = "data/ppap.dat";
    String oplModelFilePath = "model/ppap.mod";

    public void generateData(Map<Node, Map<Service, Float>> nodeServiceDemand){
        ClassLoader classLoader = getClass().getClassLoader();
        String absoluteTemplateFilePath = classLoader.getResource(templateFilePath).getPath();
        String absoluteDataFilePath = classLoader.getResource(oplDataFilePath).getPath();
        String absoluteModelFilePath = classLoader.getResource(oplModelFilePath).getPath();
        OplDataWritter oplDataWritter = new OplDataWritter(nodeServiceDemand, oplDataFilePath, templateFilePath);//TODO
        try {
            oplDataWritter.generateData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void solve(){
        ClassLoader classLoader = getClass().getClassLoader();
        String absoluteDataFilePath = classLoader.getResource(oplDataFilePath).getPath();
        String absoluteModelFilePath = classLoader.getResource(oplModelFilePath).getPath();
        OplRun.oplRun(new String[]{"-v", absoluteModelFilePath, absoluteDataFilePath});
    }
}
