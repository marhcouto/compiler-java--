package pt.up.fe.comp.semantic;

import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.semantic.analysers.CheckValidSymbolAccess;
import pt.up.fe.comp.semantic.analysers.TypeAnnotatorAndCheckMethodExistence;
import pt.up.fe.comp.semantic.symbol_table.SymbolTable;
import pt.up.fe.comp.semantic.symbol_table.SymbolTableFactory;

import java.util.LinkedList;
import java.util.List;

public class JmmAnalyser implements JmmAnalysis {
    private final List<Report> reports = new LinkedList<>();

    @Override
    public JmmSemanticsResult semanticAnalysis(JmmParserResult parserResult) {
        SymbolTable symbolTable = new SymbolTableFactory().generateTable(parserResult.getRootNode(), reports);
        new CheckValidSymbolAccess(symbolTable, reports).visit(parserResult.getRootNode());
        TypeAnnotatorAndCheckMethodExistence typeAnnotator = new TypeAnnotatorAndCheckMethodExistence(symbolTable, reports);
        typeAnnotator.visit(parserResult.getRootNode());
        return new JmmSemanticsResult(parserResult, symbolTable, reports);
    }
}
