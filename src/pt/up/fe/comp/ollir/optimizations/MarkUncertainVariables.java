package pt.up.fe.comp.ollir.optimizations;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.semantic.models.ExtendedSymbol;
import pt.up.fe.comp.semantic.symbol_table.SymbolTable;

public class MarkUncertainVariables extends PreorderJmmVisitor<Boolean, Object> {

    private final SymbolTable symbolTable;
    private final String scope;

    public MarkUncertainVariables(SymbolTable symbolTable, String scope) {
        this.scope = scope;
        this.symbolTable = symbolTable;
        addVisit("LoopBody", this::visitLoopBody);
        addVisit("AsmOp", this::visitAsmOp);
        addVisit("MethodBody", this::visitMethodBody);
    }

    private Object visitLoopBody(JmmNode node, Boolean dummy) {
        for (var child: node.getChildren()) {
            visit(child, true);
        }
        return null;
    }

    private Object visitMethodBody(JmmNode node, Boolean dummy) {
        for (var child: node.getChildren()) {
            visit(child);
        }
        return null;
    }

    private Object visitAsmOp(JmmNode node, Boolean insideLoop) {
        if (!node.getJmmChild(0).getKind().equals("VarName")) return null;
        String varName = node.getJmmChild(0).get("image");
        if (insideLoop != null) {
            ExtendedSymbol s = symbolTable.getSymbol(scope, varName);
            System.out.println("LINE:"  + node + " " + node.get("line"));
            s.setCertaintyLimitLine(Integer.parseInt(node.get("line")));
        }
        return null;
    }
}
