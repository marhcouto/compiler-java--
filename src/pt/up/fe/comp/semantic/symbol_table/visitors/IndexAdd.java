package pt.up.fe.comp.semantic.symbol_table.visitors;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.semantic.models.ExtendedSymbol;
import pt.up.fe.comp.semantic.symbol_table.SymbolTable;

import java.util.LinkedList;
import java.util.stream.Collectors;

public class IndexAdd extends PreorderJmmVisitor<Object, Object> {
    private SymbolTable symbolTable;
    private int idx = 1;

    public IndexAdd(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        if (symbolTable.getMethodScope("main") != null) {
            symbolTable.getMethodScope("main").getParameters().get(0).setIndex(1);
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
        ExtendedSymbol parameter = symbolTable
                .getMethodScope(scope)
                .getParameters()
                .stream()
                .filter(elem -> elem.getName().equals(node.getJmmChild(1).get("image")))
                .collect(Collectors.toList())
                .get(0);
        if (parameter != null) {
            parameter.setIndex(idx++);
        }
        return null;
    }
}
