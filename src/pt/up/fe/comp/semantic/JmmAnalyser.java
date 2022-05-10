package pt.up.fe.comp.semantic;

import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.semantic.analysers.*;
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
        if (reports.isEmpty()) {
            new ScopeAnnotator().visit(parserResult.getRootNode());
            try {
                new TypeChecker(symbolTable, reports).visit(parserResult.getRootNode());
            } catch (AnalysisException e) { /* Errors are already handled somewhere*/ }
        }
        new CheckVarInit(symbolTable, reports).visit(parserResult.getRootNode());
        return new JmmSemanticsResult(parserResult, symbolTable, reports);
    }
}
