package pt.up.fe.comp.ollir;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.semantic.symbol_table.SymbolTableFactory;

import java.util.Collections;

public class JmmOptimizer implements JmmOptimization {
    @Override
    public OllirResult toOllir(JmmSemanticsResult semanticsResult) {
        var ollirGenerator = new OllirGenerator(SymbolTableFactory.fromJmmSymbolTable(semanticsResult.getSymbolTable()));
        ollirGenerator.visit(semanticsResult.getRootNode());

        var ollirCode = ollirGenerator.getCode();
        System.out.println(ollirCode);
        return new OllirResult(semanticsResult, ollirCode, Collections.emptyList());
    }
}
