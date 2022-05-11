package pt.up.fe.comp.semantic.symbol_table.visitors;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.semantic.symbol_table.SymbolTable;

import java.util.LinkedList;

public class IndexAdd extends PreorderJmmVisitor<Object, Object> {
    private SymbolTable symbolTable;
    private int idx = 1;

    public IndexAdd(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        if (symbolTable.getMethodScope("main") != null) {
            new LinkedList<>(symbolTable.getMethodScope("main").getParameters().values()).get(0).setIndex(1);
        }
        addVisit("Variable", this::visitVariable);
        addVisit("MethodArgsList", this::visitMethodArgsList);
    }

    private Object visitMethodArgsList(JmmNode node, Object dummy) {
        idx = 1;
        return null;
    }

    private Object visitVariable(JmmNode node, Object dummy) {
        if (!node.getJmmParent().getKind().equals("MethodArgsList")) {
            return null;
        }
        String scope = node.getJmmParent().getJmmParent().getJmmChild(1).get("image");
        symbolTable.getMethodScope(scope).getParameters().get(node.getJmmChild(1).get("image")).setIndex(idx++);
        return null;
    }
}
