package pt.up.fe.comp;

import java.io.File;
import java.util.*;

import pt.up.fe.comp.jasmin.JasminEmitter;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.ollir.JmmOptimizer;
import pt.up.fe.comp.semantic.JmmAnalyser;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.SpecsSystem;

public class Launcher {

    public static void main(String[] args) {
        SpecsSystem.programStandardInit();

        SpecsLogs.info("Executing with args: " + Arrays.toString(args));

        // read the input code
        if (args.length != 1) {
            throw new RuntimeException("Expected a single argument, a path to an existing input file.");
        }
        File inputFile = new File(args[0]);
        if (!inputFile.isFile()) {
            throw new RuntimeException("Expected a path to an existing input file, got '" + args[0] + "'.");
        }
        String input = SpecsIo.read(inputFile);

        // Create config
        Map<String, String> config = new HashMap<>();
        config.put("inputFile", args[0]);
        config.put("optimize", "false");
        config.put("registerAllocation", "-1");
        config.put("debug", "false");

        // PARSING STAGE
        SimpleParser parser = new SimpleParser();
        JmmParserResult parserResult = parser.parse(input, config);

        // Check if there are parsing errors
        // TestUtils.noErrors(parserResult.getReports());
        for (Report r : parserResult.getReports()) {
            System.out.println(r.toString());
        }


        // SEMANTIC ANALYSIS STAGE
        JmmAnalyser analyser = new JmmAnalyser();
        JmmSemanticsResult analysisResult = analyser.semanticAnalysis(parserResult);

        // Check if there are semantic errors
        // TestUtils.noErrors(analysisResult.getReports());
        for (Report r : analysisResult.getReports()) {
            System.out.println(r.toString());
        }

        //Tree printer
        System.out.println(parserResult.getRootNode().toTree());

        // SEMANTIC ANALYSIS STAGE
        JmmOptimization optimizer = new JmmOptimizer();
        OllirResult optimizationResults = optimizer.toOllir(analysisResult);

        // Check if there are semantic errors
        // TestUtils.noErrors(analysisResult.getReports());
        for (Report r : optimizationResults.getReports()) {
            System.out.println(r.toString());
        }

        // HOLY TABLE PRINTER
        System.out.println(analysisResult.getSymbolTable().print());

        // TODO - JUNTAR COM O RESTO DAS FASES
        /* Descomentar quando se adicionar o resto
        // Instantiate JasminBackend
        var jasminEmitter = new JasminEmitter();

        //Jasmin stage
        var jasminResult = jasminEmitter.toJasmin(ollirResult);
        TestUtils.noErrors(jasminResult);
        */


    }

}
