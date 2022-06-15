package pt.up.fe.comp.ollir;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.ollir.optimizations.ConstantPropagator;
import pt.up.fe.comp.semantic.symbol_table.SymbolTable;
import pt.up.fe.comp.semantic.symbol_table.SymbolTableFactory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class JmmOptimizer implements JmmOptimization {

    private final List<Report> reports = new LinkedList<>();
    @Override
    public OllirResult toOllir(JmmSemanticsResult semanticsResult) {
        var ollirGenerator = new OllirGenerator(new SymbolTableFactory().generateTable(semanticsResult.getRootNode(), reports));
        // var ollirGenerator = new OllirGenerator(SymbolTableFactory.fromJmmSymbolTable(semanticsResult.getSymbolTable(), semanticsResult.getRootNode()));
        ollirGenerator.visit(semanticsResult.getRootNode());
        return new OllirResult(semanticsResult, ollirGenerator.getCode(), Collections.emptyList());
    }

    @Override
    public JmmSemanticsResult optimize(JmmSemanticsResult semanticsResult) {
        if(Boolean.parseBoolean(semanticsResult.getConfig().get("optimize"))) {
            new ConstantPropagator().visit(semanticsResult.getRootNode());
        }
        return semanticsResult;
    }
}
