/*
 * Licensed Materials - Property of IBM
 * 5725-A06 5725-A29 5724-Y48 5724-Y49 5724-Y54 5724-Y55
 * Copyright IBM Corporation 1998, 2013. All Rights Reserved.
 *
 * Note to U.S. Government Users Restricted Rights:
 * Use, duplication or disclosure restricted by GSA ADP Schedule
 * Contract with IBM Corp.
 *
 */
// -------------------------------------------------------------- -*- Java -*-
// Java version of OplRunSample.cpp of OPL distrib
// --------------------------------------------------------------------------

package it.polimi.ppap.solver;

import ilog.concert.IloException;
import ilog.opl.*;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class OplRun {
    CommandLine _cl;

    Timer _timer = new Timer();

    static public void main(String[] args) {
        int status = oplRun(args);
        System.exit(status);
    }

    static public int oplRun(String [] args){
        int status = 127;
        try {
            CommandLine cl = new CommandLine(args);
            OplRun oplRun = new OplRun(cl);
            oplRun.run();
            status = 0;
        } catch (IloOplException ex) {
            System.err.println("### OPL exception: " + ex.getMessage());
            ex.printStackTrace();
            status = 2;
        } catch (IloException ex) {
            System.err.println("### CONCERT exception: " + ex.getMessage());
            ex.printStackTrace();
            status = 3;
        } catch (Exception ex) {
            System.err.println("### UNEXPECTED UNKNOWN ERROR ...");
            ex.printStackTrace();
            status = 4;
        }
        return status;
    }

    OplRun(CommandLine cl) throws IloException {
        _cl = cl;
    }

    int run() throws Exception {

        System.out.println(System.getenv("DYLD_LIBRARY_PATH"));
        System.out.println(System.getenv("LD_LIBRARY_PATH"));

        IloOplFactory.setDebugMode(true);
        IloOplFactory oplF = new IloOplFactory();

        if (_cl.getCompileName() != null) {
            IloOplCompiler compiler = oplF.createOplCompiler();
            FileOutputStream ofs = new FileOutputStream(_cl.getCompileName());
            IloOplModelSource modelSource = oplF.createOplModelSource(_cl.getModelFileName());
            compiler.compile(modelSource, ofs);
            ofs.close();
            trace("compile");
            return 0;
        }

        IloOplErrorHandler errHandler = oplF.createOplErrorHandler();
        IloOplRunConfiguration rc = null;
        if (_cl.isProject()) {
            IloOplProject prj = oplF.createOplProject(_cl.getProjectPath());
            rc = prj.makeRunConfiguration(_cl.getRunConfigurationName());
        } else {
            if (_cl.getDataFileNames().size() == 0) {
                rc = oplF.createOplRunConfiguration(_cl.getModelFileName());
            } else {
                String[] dataFiles = _cl.getDataFileNames().toArray(
                        new String[_cl.getDataFileNames().size()]);
                rc = oplF.createOplRunConfiguration(_cl.getModelFileName(),
                        dataFiles);
            }
        }
        rc.setErrorHandler(errHandler);

        trace("initial");

        IloOplModel opl = rc.getOplModel();

        IloOplSettings settings = opl.getSettings();
        settings.setWithLocations(true);
        settings.setWithNames(true);
        settings.setForceElementUsage(_cl.isForceElementUsage());

        if (opl.getModelDefinition().hasMain()) {
            opl.main();
            trace("main");
        } else {
            opl.generate();
            //opl.getCplex().setParam(IloCplex.Param.Emphasis.MIP, IloCplex.MIPEmphasis.Feasibility);
            opl.getCplex().setParam(IloCplex.IntParam.PPriInd, IloCplex.PrimalPricing.Partial);
            opl.getCplex().setParam(IloCplex.IntParam.RootAlg, IloCplex.Algorithm.Primal);
            opl.getCplex().setParam(IloCplex.IntParam.TuningDisplay, IloCplex.TuningStatus.TimeLim);
            trace("generate model");
            if (opl.hasCplex()) {
                if (_cl.getExportName() != null) {
                    opl.getCplex().exportModel(_cl.getExportName());
                    trace("export model", _cl.getExportName());
                }
                if (_cl.isRelaxation()) {
                    System.out
                            .println("RELAXATIONS to obtain a feasible problem: ");
                    int count = opl.printRelaxation(System.out);
                    System.out.println("RELAXATIONS done.");
                    if (count > 0) {
                        opl.postProcess();
                        trace("post process");
                        if (_cl.isVerbose()) {
                            opl.printSolution(System.out);
                            if(_cl.getExternalDataName() != null) {
                                FileOutputStream ofs = new FileOutputStream(_cl.getExternalDataName());
                                opl.printSolution(ofs);
                                ofs.close();
                            }
                        }
                    }
                } else if (_cl.isConflict()) {
                    System.out.println("CONFLICTS to obtain a feasible problem: ");
                    opl.printConflict(System.out);
                    System.out.println("CONFLICTS done.");
                } else {
                    boolean result = opl.getCplex().solve();
                    if (result) {
                        trace("solve");
                        System.out.println();
                        System.out.println();
                        System.out.println("OBJECTIVE: "
                                + opl.getCplex().getObjValue());
                        opl.postProcess();
                        trace("post process");
                        if (_cl.isVerbose()) {
                            opl.printSolution(System.out);
                            if(_cl.getExternalDataName() != null) {
                                FileOutputStream fos = new FileOutputStream(_cl.getExternalDataName());
                                opl.printSolution(fos);
                                fos.close();
                            }
                        }
                    } else {
                        trace("no solution");
                    }
                }
            }else { //opl.hasCP()
                boolean result = opl.getCP().solve();
                if (result) {
                    trace("solve");
                    System.out.println();
                    System.out.println();
                    if (opl.getCP().hasObjective()) {
                        System.out.println("OBJECTIVE: " + opl.getCP().getObjValue());
                    } else {
                        System.out.println("OBJECTIVE: no objective");
                    }
                    opl.postProcess();
                    trace("post process");
                    if (_cl.isVerbose()) {
                        FileOutputStream ofs = new FileOutputStream(_cl.getExternalDataName());
                        opl.printSolution(ofs);
                        ofs.close();
                    }
                } else {
                    trace("no solution");
                }
            }
            trace("done");
        }
        if (_cl.getExternalDataName() != null) {
            //FileOutputStream ofs = new FileOutputStream(_cl.getExternalDataName());
            //opl.printExternalData(ofs);
            //ofs.close();
            trace("write external data", _cl.getExternalDataName());
        }
        if (_cl.getInternalDataName() != null) {
            FileOutputStream ofs = new FileOutputStream(_cl
                    .getInternalDataName());
            opl.printInternalData(ofs);
            ofs.close();
            trace("write internal data", _cl.getInternalDataName());
        }
        if (opl.hasCplex())
            opl.getCplex().end();
        else
            opl.getCP().end();
        oplF.end();
        return 0;
    }

    void trace(String title, String info) {
        System.out.println();
        System.out.print("<<< " + title);
        if (info != null) {
            System.out.print(": " + info);
        }
        if (_cl.isVerbose()) {
            System.out.print(", at " + _timer.getAbsoluteTime() + "s"
                    + ", took " + _timer.getTime() + "s");
            _timer.restart();
        }
        System.out.println();
        System.out.println();
    }

    void trace(String title) {
        trace(title, null);
    }
}

class CommandLine {
    boolean _verbose;
    boolean _forceUsage;
    boolean _isRelaxation;
    boolean _isConflict;

    String _modelFileName;
    ArrayList<String> _dataFileNames = new ArrayList<String>();
    boolean _project;

    String _exportName;
    String _compileName;
    String _externalDataName;
    String _internalDataName;

    CommandLine(String[] args) {
        _verbose = false;
        if (args.length < 1) {
            _modelFileName = ".\\mulprod.mod";
            _dataFileNames.add(".\\mulprod.dat");
        }
        int i = 0;
        for (i = 0; i < args.length; i++) {
            if ("-h".equals(args[i])) {
                usage();
            } else if ("-p".equals(args[i])) {
                _project = true;
            } else if ("-v".equals(args[i])) {
                _verbose = true;
            } else if ("-e".equals(args[i])) {
                i++;
                if (i < args.length && args[i].charAt(0) != '-'
                        && args[i].charAt(0) != '\0') {
                    _exportName = args[i];
                } else {
                    _exportName = "oplRunSample.lp";
                    i--;
                }
            } else if ("-o".equals(args[i])) {
                i++;
                if (i < args.length && args[i].charAt(0) != '-'
                        && args[i].charAt(0) != '\0') {
                    _compileName = args[i];
                } else {
                    usage();
                }
            } else if ("-de".equals(args[i])) {
                i++;
                if (i < args.length && args[i].charAt(0) != '-'
                        && args[i].charAt(0) != '\0') {
                    _externalDataName = args[i];
                } else {
                    usage();
                }
            } else if ("-di".equals(args[i])) {
                i++;
                if (i < args.length && args[i].charAt(0) != '-'
                        && args[i].charAt(0) != '\0') {
                    _internalDataName = args[i];
                } else {
                    usage();
                }
            } else if ("-f".equals(args[i])) {
                _forceUsage = true;
            } else if ("-relax".equals(args[i])) {
                _isRelaxation = true;
            } else if ("-conflict".equals(args[i])) {
                _isConflict = true;
            } else if (args[i].charAt(0) == '-') {
                System.err.println("Unknown option: " + args[i]);
                usage();
            } else {
                break;
            }
        }
        if (i >= args.length & i > 1) {
            usage();
        }
        if ((isProject() & _dataFileNames.size() >= 1) || (isProject() & args.length < 2)) {
            usage();
        }
        if (i < args.length) {
            _modelFileName = args[i];
            for (i++; i < args.length; i++) {
                _dataFileNames.add(args[i]);
            }
        }
    }

    void usage() {
        System.err.println();
        System.err.println("Usage:");
        System.err.println("OplRunSample [options] model-file [data-file ...]");
        System.err
                .println("OplRunSample [options] -p project-path [run-configuration]");
        System.err.println("  options ");
        System.err.println("    -h               this help message");
        System.err.println("    -v               verbose");
        System.err.println("    -e [export-file] export model");
        System.err.println("    -de [dat-file]   write external data");
        System.err.println("    -di [dat-file]   write internal data");
        System.err.println("    -o output-file   compile model");
        System.err.println("    -f               force element usage");
        System.err
                .println("    -relax           calculate relaxations needed for feasible model");
        System.err
                .println("    -conflict        calculate a conflict for an infeasible model");
        System.err.println();
        System.exit(0);
    }

    String getModelFileName() {
        return _modelFileName;
    }

    List<String> getDataFileNames() {
        return _dataFileNames;
    }

    boolean isProject() {
        return _project;
    }

    String getProjectPath() {
        return _project ? _modelFileName : null;
    }

    String getRunConfigurationName() {
        return (_project && _dataFileNames.size() == 1) ? (String) _dataFileNames
                .get(0)
                : null;
    }

    boolean isVerbose() {
        return _verbose;
    }

    boolean isForceElementUsage() {
        return _forceUsage;
    }

    boolean isRelaxation() {
        return _isRelaxation;
    }

    boolean isConflict() {
        return _isConflict;
    }

    String getExportName() {
        return _exportName;
    }

    String getCompileName() {
        return _compileName;
    }

    String getExternalDataName() {
        return _externalDataName;
    }

    String getInternalDataName() {
        return _internalDataName;
    }
}

class Timer {
    long _time = System.currentTimeMillis();

    long _startTime = System.currentTimeMillis();

    public void restart() {
        _time = System.currentTimeMillis();
    }

    public float getTime() {
        return (System.currentTimeMillis() - _time) / 1000;
    }

    public float getAbsoluteTime() {
        return (System.currentTimeMillis() - _startTime) / 1000;
    }
}
