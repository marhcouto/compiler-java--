package pt.up.fe.comp.semantic.analysers;

import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp.semantic.Constants;
import pt.up.fe.comp.semantic.Method;
import pt.up.fe.comp.semantic.symbol_table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;

import java.util.List;

public class TypeAnnotatorAndCheckMethodExistence extends TopDownScopeAnalyser {
    private final SymbolTable symbolTable;

    public TypeAnnotatorAndCheckMethodExistence(SymbolTable symbolTable, List<Report> reports) {
        super(symbolTable, reports);
        this.symbolTable = symbolTable;
        addVisit("BinOp", this::visitBinOp);
        addVisit("UnaryOp", this::visitUnaryOp);
        addVisit("True", this::visitSingleBoolLiteral);
        addVisit("False", this::visitSingleBoolLiteral);
        addVisit("This", this::visitThis);
        addVisit("IntegerLiteral", this::visitIntegerLiteral);
        addVisit("CreateArrObj", this::visitCreateArrObj);
        addVisit("CreateObj", this::visitCreateObj);
        addVisit("Variable", (node, dummy) -> false);
        addVisit("ArrAccess", this::visitArrAccess);
        addVisit("FnCallOp", this::visitFnCallOp);
    }

    private Boolean visitBinOp(JmmNode node, String scope) {
        switch (node.get("op")) {
            case "ADD", "MUL", "DIV", "SUB" -> node.put("type", "int");
            case "LT", "AND" -> node.put("type", "bool");
        }
        for (var child: node.getChildren()) {
            visit(child, scope);
        }
        return true;
    }

    private Boolean visitUnaryOp(JmmNode node, String scope) {
        switch (node.get("op")) {
            case "NOT" -> node.put("type", "bool");
            case "SIM" -> node.put("type", "int");
        }
        for (var child: node.getChildren()) {
            visit(child, scope);
        }
        return true;
    }

    private Boolean visitSingleBoolLiteral(JmmNode node, String dummy) {
        node.put("type", "bool");
        return true;
    }

    private Boolean visitThis(JmmNode node, String dummy) {
        node.put("type", symbolTable.getClassName());
        return true;
    }

    private Boolean visitIntegerLiteral(JmmNode node, String dummy) {
        node.put("type", "int");
        return true;
    }

    private Boolean visitCreateArrObj(JmmNode node, String dummy) {
        node.put("type", "int[]");
        return true;
    }

    private Boolean visitCreateObj(JmmNode node, String dummy) {
        node.put("type", node.getJmmChild(0).get("image"));
        return true;
    }

    private Boolean visitArrAccess(JmmNode node, String scope) {
        String typeName = symbolTable.getSymbol(scope, node.getJmmChild(0).get("image")).getType().getName();
        node.put("type", typeName);
        return true;
    }

    private Boolean visitFnCallOp(JmmNode node, String scope) {
        if (node.getJmmChild(0).getKind().equals("VarName")) {
            String varName = node.getJmmChild(0).get("image");
            node.getJmmChild(0).put("type", symbolTable.getSymbol(scope, varName).getType().getName());
        }
        if (!node.getJmmChild(0).getAttributes().contains("type") && !visit(node.getJmmChild(0), scope)) {
            return false;
        }
        String childNodeType = node.getJmmChild(0).get("type");
        if (childNodeType.equals(Constants.ANY_TYPE) || symbolTable.hasSymbolInImportPath(childNodeType)) {
            node.put("type", Constants.ANY_TYPE);
            return true;
        }
        if (childNodeType.equals(symbolTable.getClassName())) {
            Method method = symbolTable.getMethodScope(node.getJmmChild(1).get("image"));
            if (method != null) {
                node.put("type", method.getReturnType().getName());
                return true;
            } else if (symbolTable.getSuper() != null) {
                //Wildcard type
                node.put("type", Constants.ANY_TYPE);
                return true;
            }
        }
        reports.add(new Report(
                ReportType.ERROR,
                Stage.SEMANTIC,
                Integer.parseInt(node.get("line")),
                Integer.parseInt(node.get("column")),
                String.format("Method '%s' not found in '%s' definition", node.getJmmChild(1).get("image"), childNodeType)
        ));
        return false;
    }
}
