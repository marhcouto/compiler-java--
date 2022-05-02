package pt.up.fe.comp.semantic;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;

public class TypeAnnotator extends PreorderJmmVisitor<Object, Type> {
    private SymbolTable symbolTable;

    public TypeAnnotator(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        addVisit("BinOp", this::visitBinOp);
        addVisit("UnaryOp", this::visitUnaryOp);
        addVisit("True", this::visitSingleBoolLiteral);
        addVisit("False", this::visitSingleBoolLiteral);
        addVisit("This", this::visitThis);
        addVisit("CreateArrObj", this::visitCreateArrObj);
        addVisit("CreateObj", this::visitCreateObj);
        addVisit("ArrAccess", this::visitArrAccess);
    }

    private Type visitBinOp(JmmNode node, Object dummy) {
        switch (node.get("op")) {
            case "ADD", "MUL", "DIV", "SUB":
                node.put("type", "int");
                break;
            case "LT", "AND":
                node.put("type", "bool");
                break;
        }
        for (var child: node.getChildren()) {
            visit(child);
        }
        return null;
    }

    private Type visitUnaryOp(JmmNode node, Object dummy) {
        switch (node.get(node.get("op"))) {
            case "NOT":
                node.put("type", "bool");
                break;
            case "SIM":
                node.put("type", "int");
        }
        return null;
    }

    private Type visitSingleBoolLiteral(JmmNode node, Object dummy) {
        node.put("type", "bool");
        return null;
    }

    private Type visitThis(JmmNode node, Object dummy) {
        node.put("type", symbolTable.getClassName());
        return null;
    }

    private Type visitCreateArrObj(JmmNode node, Object dummy) {
        node.put("type", "int[]");
        return null;
    }

    private Type visitCreateObj(JmmNode node, Object dummy) {
        node.put("type", node.getJmmChild(0).get("image"));
        return null;
    }

    private Type visitArrAccess(JmmNode node, Object dummy) {
        node.put("type", "int");
        return null;
    }
}
