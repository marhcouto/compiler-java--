package pt.up.fe.comp.semantic.analysers;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PostorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp.semantic.Constants;
import pt.up.fe.comp.semantic.models.Method;
import pt.up.fe.comp.semantic.symbol_table.SymbolTable;

import java.util.ArrayList;
import java.util.List;

public class TypeChecker extends PostorderJmmVisitor<Object, Object> {
    private final SymbolTable symbolTable;
    private List<Report> reports;

    public TypeChecker(SymbolTable symbolTable, List<Report> reports) {
        this.reports = reports;
        this.symbolTable = symbolTable;
        addVisit("BinOp", this::visitBinOp);
        addVisit("VarName", this::visitVarName);
        addVisit("Length", this::visitLength);
        addVisit("True", this::visitSingleBoolLiteral);
        addVisit("False", this::visitSingleBoolLiteral);
        addVisit("This", this::visitThis);
        addVisit("IntegerLiteral", this::visitIntegerLiteral);
        addVisit("UnaryOp", this::visitUnaryOp);
        addVisit("WhileStm", this::visitLoop);
        addVisit("IfStm", this::visitLoop);
        addVisit("FnCallOp", this::visitFnCallOp);
        addVisit("AsmOp", this::visitAsmOp);
        addVisit("ArrAccess", this::visitArrAccess);
        addVisit("CreateArrObj", this::visitCreateArrObj);
        addVisit("CreateObj", this::visitCreateObj);
    }

    private Object visitSingleBoolLiteral(JmmNode node, Object dummy) {
        node.put("type", "bool");
        return null;
    }

    private Boolean visitThis(JmmNode node, Object dummy) {
        node.put("type", symbolTable.getClassName());
        return null;
    }

    private Boolean visitIntegerLiteral(JmmNode node, Object dummy) {
        node.put("type", "int");
        return null;
    }

    private Object visitLength(JmmNode node, Object dummy) {
        JmmNode subject = node.getJmmChild(0);
        if (subject.getAttributes().contains("arr") || subject.get("type").equals(Constants.ANY_TYPE)) {
            node.put("type", "int");
            return null;
        }
        reports.add(new Report(
                ReportType.ERROR,
                Stage.SEMANTIC,
                Integer.parseInt(node.get("line")),
                Integer.parseInt(node.get("column")),
                Utils.buildTypeAnnotatedError(node.getJmmChild(0), Constants.ANY_TYPE, true)
        ));
        throw new AnalysisException();
    }

    private Object visitVarName(JmmNode node, Object dummy) {
        if (node.getJmmParent().getKind().equals("Variable")) {
            return null;
        }
        String nodeScope = node.get("scope");
        Type nodeType = symbolTable.getSymbol(nodeScope, node.get("image")).getType();
        node.put("type", nodeType.getName());
        if (nodeType.isArray()){
            node.put("arr", "true");
        }
        return null;
    }

    private boolean matchExpectedType(JmmNode node, String expectedType, boolean expectsArray) {
        String nodeType = node.get("type");
        boolean nodeIsArray = node.getAttributes().contains("arr");
        return (nodeType.equals(expectedType) && (expectsArray == nodeIsArray)) || nodeType.equals(Constants.ANY_TYPE);
    }

    private Object visitBinOp(JmmNode node, Object dummy) {
        String expectedType = "int";
        if (node.get("op").equals("AND")) {
            expectedType = "bool";
        }
        JmmNode leftChild = node.getJmmChild(0);
        JmmNode rightChild = node.getJmmChild(1);
        if (!matchExpectedType(leftChild, expectedType, false)) {
            reports.add(new Report(
                    ReportType.ERROR,
                    Stage.SEMANTIC,
                    Integer.parseInt(node.get("line")),
                    Integer.parseInt(node.get("column")),
                    Utils.buildTypeAnnotatedError(leftChild, expectedType, false)
            ));
            throw new AnalysisException();
        }
        if (!matchExpectedType(rightChild, expectedType, false)) {
            reports.add(new Report(
                    ReportType.ERROR,
                    Stage.SEMANTIC,
                    Integer.parseInt(node.get("line")),
                    Integer.parseInt(node.get("column")),
                    Utils.buildTypeAnnotatedError(rightChild, expectedType, false)
            ));
            throw new AnalysisException();
        }
        switch (node.get("op")) {
            case "ADD", "MUL", "DIV", "SUB" -> node.put("type", "int");
            case "LT", "AND" -> node.put("type", "bool");
        }
        return null;
    }

    private Object visitUnaryOp(JmmNode node, Object dummy) {
        String expectedType = null;
        switch (node.get("op")) {
            case "NOT" -> expectedType = "bool";
            case "SIM" -> expectedType = "int";
        }
        JmmNode child = node.getJmmChild(0);
        if (!matchExpectedType(child, expectedType, false)) {
            reports.add(new Report(
                    ReportType.ERROR,
                    Stage.SEMANTIC,
                    Integer.parseInt(node.get("line")),
                    Integer.parseInt(node.get("column")),
                    Utils.buildTypeAnnotatedError(child, expectedType, false)
            ));
            throw new AnalysisException();
        }
        node.put("type", expectedType);
        return null;
    }

    private Object visitLoop(JmmNode node, Object dummy) {
        JmmNode stopCondition = node.getJmmChild(0);
        if (matchExpectedType(stopCondition, "bool", false)) {
            node.put("type", "bool");
            return null;
        }
        reports.add(new Report(
                ReportType.ERROR,
                Stage.SEMANTIC,
                Integer.parseInt(node.get("line")),
                Integer.parseInt(node.get("column")),
                Utils.buildTypeAnnotatedError(stopCondition, "bool", false)
        ));
        throw new AnalysisException();
    }

    private boolean checkVarHasMethod(JmmNode node) {
        String subjectType = node.getJmmChild(0).get("type");
        Method calledMethod = symbolTable.getMethodScope(node.getJmmChild(1).get("image"));
        if ((subjectType.equals(symbolTable.getClassName()) && calledMethod != null) || cantDetermineArgsType(node)) {
            return true;
        }
        return false;
    }

    private boolean cantDetermineArgsType(JmmNode node) {
        String subjectType = node.getJmmChild(0).get("type");
        String calledMethod = node.getJmmChild(1).get("image");
        return subjectType.equals(Constants.ANY_TYPE) || // Type any can't determine which methods are available
                symbolTable.hasSymbolInImportPath(subjectType) || // Symbol was imported
                (
                    subjectType.equals(symbolTable.getClassName()) && // Symbol is the current class
                    symbolTable.getMethodScope(calledMethod) == null && // The called method isn't available in the scope
                    symbolTable.getSuper() != null // However, the class extends another class
                );
    }

    private boolean methodArgsCompatible(JmmNode node) {
        String subjectType = node.getJmmChild(0).get("type");
        if (subjectType.equals(Constants.ANY_TYPE)) {
            return true;
        }
        List<Symbol> params = new ArrayList<>(symbolTable.getMethodScope(node.getJmmChild(1).get("image")).getParameters().values());
        List<JmmNode> args = node.getJmmChild(2).getChildren();
        if (args.size() != params.size()) {
            return false;
        }
        for (int i = 0; i < args.size(); i++) {
            Type currentType = params.get(i).getType();
            if (!matchExpectedType(args.get(i), currentType.getName(), currentType.isArray())) {
                return false;
            }
        }
        return true;
    }

    private Object visitFnCallOp(JmmNode node, Object scope) {
        if (!checkVarHasMethod(node)) {
            reports.add(new Report(
                    ReportType.ERROR,
                    Stage.SEMANTIC,
                    Integer.parseInt(node.get("line")),
                    Integer.parseInt(node.get("column")),
                    String.format("Method '%s' not found in '%s' definition", node.getJmmChild(1).get("image"), node.getJmmChild(0).get("type"))
            ));
            throw new AnalysisException();
        }
        if (cantDetermineArgsType(node)) {
            node.put("type", Constants.ANY_TYPE);
            return null;
        }
        List<Symbol> expectedParams = new ArrayList<>(symbolTable.getMethodScope(node.getJmmChild(1).get("image")).getParameters().values());
        List<JmmNode> args = node.getJmmChild(2).getChildren();
        if (!methodArgsCompatible(node)) {
            reports.add(new Report(
                    ReportType.ERROR,
                    Stage.SEMANTIC,
                    Integer.parseInt(node.get("line")),
                    Integer.parseInt(node.get("column")),
                    String.format("Gave %s but method expected %s", Utils.buildArgTypes(args), Utils.buildParamTypes(expectedParams))
            ));
            throw new AnalysisException();
        }
        Method methodBeingCalled = symbolTable.getMethodScope(node.getJmmChild(1).get("image"));
        node.put("type", methodBeingCalled.getReturnType().getName());
        if (methodBeingCalled.getReturnType().isArray()) {
            node.put("arr", "true");
        }
        return null;
    }

    private Object visitAsmOp(JmmNode node, Object scope) {
        JmmNode dest = node.getJmmChild(0);
        JmmNode orig = node.getJmmChild(1);
        if (matchExpectedType(orig, dest.get("type"), dest.getAttributes().contains("arr"))) {
            return null;
        }
        reports.add(new Report(
                ReportType.ERROR,
                Stage.SEMANTIC,
                Integer.parseInt(node.get("line")),
                Integer.parseInt(node.get("column")),
                String.format("Tried to assign '%s' to a variable of type '%s'", Utils.prettyNodeTypeToString(orig), Utils.prettyNodeTypeToString(dest))
        ));
        throw new AnalysisException();
    }

    private Object visitArrAccess(JmmNode node, Object scope) {
        JmmNode underlyingVar = node.getJmmChild(0);
        Symbol symbol = symbolTable.getSymbol(underlyingVar.get("scope"), underlyingVar.get("image"));
        if (!matchExpectedType(node.getJmmChild(1), "int", false)) {
            reports.add(new Report(
                    ReportType.ERROR,
                    Stage.SEMANTIC,
                    Integer.parseInt(node.get("line")),
                    Integer.parseInt(node.get("column")),
                    String.format("Array index must be of type int but '%s' was found instead", Utils.prettyNodeTypeToString(node.getJmmChild(1)))
            ));
            throw new AnalysisException();
        }
        node.put("type", symbol.getType().getName());
        return null;
    }
    private Object visitCreateArrObj(JmmNode node, Object scope) {
        JmmNode sizeNode = node.getJmmChild(0);
        if (!matchExpectedType(sizeNode, "int", false)) {
            reports.add(new Report(
                    ReportType.ERROR,
                    Stage.SEMANTIC,
                    Integer.parseInt(node.get("line")),
                    Integer.parseInt(node.get("column")),
                    String.format("Array size must be of type int but '%s' was found instead", Utils.prettyNodeTypeToString(node.getJmmChild(0)))
            ));
            throw new AnalysisException();
        }
        node.put("type", "int");
        node.put("arr", "true");
        return null;
    }

    private Object visitCreateObj(JmmNode node, Object scope) {
        node.put("type", node.getJmmChild(0).get("image"));
        return null;
    }
}
