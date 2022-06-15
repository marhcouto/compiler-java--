package pt.up.fe.comp;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import org.eclipse.jgit.util.IO;
import pt.up.fe.comp.jasmin.JasminEmitter;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
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

    public static void main(String[] args) throws IOException {
        SpecsSystem.programStandardInit();

        SpecsLogs.info("Executing with args: " + Arrays.toString(args));

        // read the input code
        if (args.length > 3) {
            throw new RuntimeException("Expected at most 3 arguments");
        }
        File inputFile = new File(args[args.length - 1]);
        if (!inputFile.isFile()) {
            throw new RuntimeException("Expected a path to an existing input file, got '" + args[args.length - 1] + "'.");
        }
        String input = SpecsIo.read(inputFile);

        // Create config
        Map<String, String> config = new HashMap<>();
        config.put("inputFile", args[args.length - 1]);
        config.put("optimize", "false");
        config.put("registerAllocation", "-1");
        config.put("debug", "false");
        config.put("verbose", "false");

        for(int i = 0; i < args.length; i++) {
            if (args[i].equals("-o")) {
                config.put("optimize", "true");
            }
            if (args[i].equals("-d")) {
                config.put("debug", "true");
            }
            if (args[i].equals("-v")) {
                config.put("verbose", "true");
            }
        }


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

        if (config.get("optimize").equals("true")) {
            analysisResult = new JmmOptimizer().optimize(analysisResult);
        }

        // OLLIR STAGE
        JmmOptimization optimizer = new JmmOptimizer();
        OllirResult optimizationResult = optimizer.toOllir(analysisResult);
        // OLLIR reports
        // TestUtils.noErrors(optimizationResult.getReports());
        for (Report r : optimizationResult.getReports()) {
            System.out.println(r.toString());
        }

        // JASMIN STAGE
        JasminEmitter jasminEmitter = new JasminEmitter();
        JasminResult jasminResult = jasminEmitter.toJasmin(optimizationResult);
        // Jasmin reports
        // TestUtils.noErrors(jasminResult.getReports());
        for (Report r : jasminResult.getReports()) {
            System.out.println(r.toString());
        }

        if (config.get("verbose").equals("true")) {
            // TREE PRINT
            System.out.println("\n\nTREE:\n");
            System.out.println(parserResult.getRootNode().toTree());

            // TABLE PRINT
            System.out.println("\n\nTABLE:\n");
            System.out.println(analysisResult.getSymbolTable().print());

            // OLLIR CODE PRINT
            System.out.println("\n\nOLLIR:\n");
            System.out.println(optimizationResult.getOllirCode());

            // JASMIN CODE PRINT
            System.out.println("\n\nJASMIN:\n");
            System.out.println(jasminResult.getJasminCode());
        }

        if (config.get("debug").equals("false")) {
            writeJasminCode(args[args.length - 1], jasminResult);
        }
    }

    private static void writeJasminCode(String inputFilePath, JasminResult result) throws IOException {
        String fileName = Paths.get(inputFilePath).getFileName().toString();
        String fileNameWOExtension = fileName.split("\\.")[0];
        Files.writeString(Paths.get(fileNameWOExtension + ".j"), result.getJasminCode());
    }

}
