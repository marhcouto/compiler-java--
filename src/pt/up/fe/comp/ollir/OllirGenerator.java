package pt.up.fe.comp.ollir;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.ollir.optimizations.MarkUncertainVariables;
import pt.up.fe.comp.semantic.Constants;
import pt.up.fe.comp.semantic.models.ExtendedSymbol;
import pt.up.fe.comp.semantic.models.Origin;
import pt.up.fe.comp.semantic.symbol_table.SymbolTable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class OllirGenerator extends AJmmVisitor<String, List<String>> {
    private final StringBuilder code = new StringBuilder();
    private final SymbolTable symbolTable;

    private int numTempVars = 0;
    private int labels = 0;

    OllirGenerator(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        addVisit("Start", this::visitStart);
        addVisit("ClassDeclaration", this::visitClassDeclaration);
        addVisit("MainMethodDeclaration", this::visitMainMethod);
        addVisit("MethodDeclaration", this::visitMethod);
        addVisit("MethodBody", this::visitMethodBody);
        addVisit("FnCallOp", this::visitFnCallOp);
        addVisit("VarName", this::visitVarName);
        addVisit("IntegerLiteral", (node, dummy) -> Arrays.asList(String.format("%s.%s", node.get("image"), "i32"), "Constant", node.get("image")));
        addVisit("False", (node, dummy) -> Arrays.asList("0" + ".bool", "Constant", "0"));
        addVisit("True", (node, dummy) -> Arrays.asList("1" + ".bool", "Constant", "1"));
        addVisit("BinOp", this::visitBinOp);
        addVisit("UnaryOp", this::visitUnaryOp);
        addVisit("CreateObj", this::visitCreateObj);
        addVisit("CreateArrObj", this::visitCreateArrObj);
        addVisit("Variable", this::visitVariable);
        addVisit("ArrAccess", this::visitArrAccess);
        addVisit("ReturnStatement", this::visitReturnStatement);
        addVisit("MethodArgsList", this::visitMethodArgsList);
        addVisit("AsmOp", this::visitAsmOp);
        addVisit("WhileStm", this::visitWhile);
        addVisit("IfStm", this::visitIfStm);
        addVisit("Length", this::visitLength);
        setDefaultVisit((node, dummy) -> null);
    }

    private List<String> visitStart(JmmNode node, String dummy) {
        injectImports();
        for (var child: node.getChildren()) {
            visit(child);
        }
        return null;
    }

    private List<String> visitClassDeclaration(JmmNode node, String dummy) {
        String extendsStr = symbolTable.getSuper() != null ? String.format("extends %s", symbolTable.getSuper()) : "";
        code.append(String.format("public %s %s {\n", symbolTable.getClassName(), extendsStr));
        code.append(OllirUtils.generateFields(symbolTable.getFields()));
        code.append(defaultConstructor(symbolTable.getClassName()));
        for (var child: node.getChildren()) {
            visit(child);
        }
        code.append("}");
        return null;
    }

    private List<String> visitMainMethod(JmmNode node, String dummy) {
        numTempVars = 0;
        String mainArgsName = node.getJmmChild(0).get("image");
        code.append(String.format(".method public static main(%s.array.String).V{\n", mainArgsName));
        visit(node.getJmmChild(1), "main");
        code.append("ret.V;\n");
        code.append("}\n");
        return null;
    }

    private List<String> visitMethod(JmmNode node, String scope) {
        numTempVars = 0;
        String methodName = node.getJmmChild(1).get("image");
        code.append(String.format(".method public %s (", methodName));
        visit(node.getJmmChild(2), methodName);
        code.append(String.format(").%s {\n", OllirUtils.toOllir(symbolTable.getReturnType(methodName))));
        visit(node.getJmmChild(3), methodName);
        visit(node.getJmmChild(4), methodName);
        code.append("}\n");
        return null;
    }

    private List<String> visitMethodBody(JmmNode node, String scope) {
        new MarkUncertainVariables(symbolTable, scope).visit(node);
        for (var child: node.getChildren()) {
            visit(child, scope);
        }
        return null;
    }

    private List<String> visitBinOp(JmmNode node, String scope) {
        List<String> resultLhs = visit(node.getJmmChild(0), scope);
        List<String> resultRhs = visit(node.getJmmChild(1), scope);
        String lhs = resultLhs.get(0);
        String rhs = resultRhs.get(0);
        String expressionVariability = "Not Constant";
        Integer value = null;
        String valueString = null;
        if (resultLhs.size() > 2 && resultRhs.size() > 2 && resultLhs.get(1).equals("Constant") && resultRhs.get(1).equals("Constant")) {
            expressionVariability = "Constant";
        }
        String tempVar = generateTempVar();
        String operator;
        switch (node.get("op")) {
            case "ADD":
                operator = "+";
                if (expressionVariability.equals("Constant")) {
                    value = (Integer.parseInt(resultLhs.get(2)) + Integer.parseInt(resultRhs.get(2)));
                }
                break;
            case "MUL":
                operator = "*";
                if (expressionVariability.equals("Constant")) {
                    value = (Integer.parseInt(resultLhs.get(2)) * Integer.parseInt(resultRhs.get(2)));
                }
                break;
            case "DIV":
                operator = "/";
                if (expressionVariability.equals("Constant")) {
                    value = (Integer.parseInt(resultLhs.get(2)) / Integer.parseInt(resultRhs.get(2)));
                }
                break;
            case "LT":
                operator = "<";
                if (expressionVariability.equals("Constant")) {
                    value = (Integer.parseInt(resultLhs.get(2)) < Integer.parseInt(resultRhs.get(2))) ? 1 : 0;
                }
                break;
            case "SUB":
                operator = "-";
                if (expressionVariability.equals("Constant")) {
                    value = (Integer.parseInt(resultLhs.get(2)) - Integer.parseInt(resultRhs.get(2)));
                }
                break;
            case "AND":
                operator = "&&";
                if (expressionVariability.equals("Constant")) {
                    value = ((Integer.parseInt(resultLhs.get(2)) >= 1) && (Integer.parseInt(resultRhs.get(2)) >= 1)) ? 1 : 0;
                }
                break;
            default:
                throw new RuntimeException("To make the compiler calm");
        }
        code.append(String.format("%s.%s :=.%s %s %s.%s %s;\n",
                tempVar,
                OllirUtils.toOllir(node),
                OllirUtils.toOllir(node),
                lhs,
                operator,
                OllirUtils.toOllir(node),
                rhs
        ));
        if (value != null) valueString = value.toString();
        return Arrays.asList(String.format("%s.%s", tempVar, OllirUtils.toOllir(node)), expressionVariability, valueString);
    }
    private List<String> visitFnCallOp(JmmNode node, String scope) {
        String called = null;
        String curVar = null;
        List<String> args = runArgExpressions(node.getJmmChild(2), scope);
        args = args.stream().map((varName) -> {
            if (varName.contains("[")) {
                String tempVar = generateTempVar() + ".i32";
                code.append(String.format("%s :=.i32 %s;\n", tempVar, varName));
                return tempVar;
            }
            return varName;
        }).collect(Collectors.toList());
        if (OllirUtils.needToPlaceVariable(node.getJmmChild(0))) {
            var temp = visit(node.getJmmChild(0), scope);
            if (temp != null)
                called = visit(node.getJmmChild(0), scope).get(0);
        }
        if (OllirUtils.needToPlaceVariable(node)) {
            String type = OllirUtils.toOllir(node);
            curVar = String.format("%s.%s", generateTempVar(), type);
            code.append(String.format("%s :=.%s ", curVar, type));
        }
        if (node.getJmmChild(0).getKind().equals("VarName")){
            called = node.getJmmChild(0).get("image");
        }
        injectCall(called, node, args);
        return Collections.singletonList(curVar);
    }

    private List<String> visitCreateObj(JmmNode node, String dummy) {
        String tempVar = generateTempVar();
        String type = OllirUtils.toOllir(node);
        code.append(String.format("%s.%s :=.%s new(%s).%s;\n", tempVar, type, type, type, type));
        code.append(String.format("invokespecial(%s.%s, \"<init>\").V;\n", tempVar, type));
        return Collections.singletonList(String.format("%s.%s", tempVar, type));
    }

    private List<String> visitCreateArrObj(JmmNode node, String scope) {
        String size = visit(node.getJmmChild(0), scope).get(0);
        String tempVar = generateTempVar();
        code.append(String.format("%s.array.i32 :=.array.i32 new(array, %s).array.i32;\n", tempVar, size));
        return Collections.singletonList(tempVar + ".array.i32");
    }

    private void injectCall(String called, JmmNode node, List<String> args) {
        JmmNode method = node.getJmmChild(1);
        if (OllirUtils.isStatic(node.getJmmChild(0))) {
            code.append(String.format("invokestatic(%s, \"%s\"", called, method.get("image")));
        } else {
            String type = OllirUtils.toOllir(node.getJmmChild(0));
            if (called == null) {
                code.append(String.format("invokevirtual(this, \"%s\"", method.get("image")));
            } else {
                code.append(String.format("invokevirtual(%s.%s, \"%s\"", called, type, method.get("image")));
            }
        }
        if (!args.isEmpty()) {
            for (String arg : args) {
                code.append(String.format(", %s", arg));
            }
        }
        code.append(String.format(").%s;\n", OllirUtils.toOllir(node)));
    }

    private List<String> runArgExpressions(JmmNode argList, String scope) {
        List<String> argExpressions = new ArrayList<>();
        for (int i = 0; i < argList.getChildren().size(); i++) {
            argExpressions.add(visit(argList.getChildren().get(i), scope).get(0));
        }
        return argExpressions;
    }

    public List<String> visitVariable(JmmNode node, String scope) {
        if (!node.getJmmParent().getKind().equals("MethodArgsList")) {
            return Collections.singletonList("");
        }
        String variableName = node.getJmmChild(1).get("image");
        return Collections.singletonList(OllirUtils.toOllir(symbolTable.getSymbol(scope, variableName)));
    }

    private List<String> visitVarName(JmmNode node, String scope) {
        Origin varOrigin = symbolTable.getSymbolOrigin(scope, node.get("image"));
        ExtendedSymbol variable = symbolTable.getSymbol(scope, node.get("image"));
        switch (varOrigin) {
            case LOCAL:
                if (!variable.getValue().equals("") && (variable.getCertaintyLimitLine() > Integer.parseInt(node.get("line")))) {
                    return Arrays.asList(String.format("%s.%s", node.get("image"), OllirUtils.toOllir(node)), "Constant", variable.getValue());
                } else {
                    return Collections.singletonList(String.format("%s.%s", node.get("image"), OllirUtils.toOllir(node)));
                }
            case IMPORT_PATH:
                return Collections.singletonList(String.format("%s.%s", node.get("image"), Constants.ANY_TYPE));
            case PARAMS:
                return Collections.singletonList(String.format("$%d.%s.%s",
                        symbolTable.getParamIndex(scope, node.get("image")),
                        node.get("image"),
                        OllirUtils.toOllir(node)
                ));
            case CLASS_FIELD:
                String newVar = generateTempVar();
                String type = OllirUtils.toOllir(node);
                code.append(String.format("%s.%s :=.%s getfield(this, %s.%s).%s;\n",
                        newVar,
                        type,
                        type,
                        node.get("image"),
                        type,
                        type
                ));
                return Collections.singletonList(String.format("%s.%s", newVar, type));
        }
        return null;
    }

    private List<String> visitAsmOp(JmmNode node, String scope) {
        List<String> visitExpression = visit(node.getJmmChild(1), scope);
        String expr = visitExpression.get(0);
        switch (node.getJmmChild(0).getKind()) {
            case "VarName": {
                String varName = node.getJmmChild(0).get("image");
                Origin varOrigin = symbolTable.getSymbolOrigin(scope, varName);
                if (varOrigin.equals(Origin.CLASS_FIELD)) {
                    code.append(String.format("putfield(this, %s.%s, %s).%s;\n",
                            varName,
                            OllirUtils.toOllir(node.getJmmChild(0)),
                            expr,
                            OllirUtils.toOllir(node.getJmmChild(0))
                    ));
                    break;
                }
                JmmNode variable = node.getJmmChild(0);
                String dest = visit(variable, scope).get(0);
                ExtendedSymbol s = symbolTable.getSymbol(scope, variable.get("image"));
                if (visitExpression.size() > 2 && visitExpression.get(1).equals("Constant")) {
                    s.setValue(visitExpression.get(2));
                }
                code.append(String.format("%s :=.%s %s;\n",
                        dest,
                        OllirUtils.toOllir(node.getJmmChild(0)),
                        expr
                ));
                break;
            }
            case "ArrAccess": {
                List<String> arrAccess = visit(node.getJmmChild(0), scope);
                code.append(String.format("%s :=.i32 %s;\n", arrAccess.get(0), expr));
                break;
            }
        }
        return null;
    }

    private List<String> visitReturnStatement(JmmNode node, String scope) {
        String retVal = visit(node.getJmmChild(0), scope).get(0);
        code.append(String.format("ret.%s %s;\n",
                OllirUtils.toOllir(symbolTable.getReturnType(scope)),
                retVal
        ));
        return null;
    }

    private List<String> visitMethodArgsList(JmmNode node, String scope) {
        if (node.getChildren().isEmpty()) {
            return null;
        }
        code.append(visit(node.getJmmChild(0), scope).get(0));
        for (int i = 1; i < node.getChildren().size(); i++) {
            code.append(String.format(", %s", visit(node.getJmmChild(i), scope).get(0)));
        }
        return null;
    }

    private List<String> visitArrAccess(JmmNode node, String scope) {
        String idx = visit(node.getJmmChild(1), scope).get(0);
        return Collections.singletonList(buildArrAccess(node, idx, scope));
    }

    private List<String> visitUnaryOp(JmmNode node, String scope) {
        String generatedCode;
        List<String> visitResult = visit(node.getJmmChild(0), scope);
        switch (node.get("op")) {
            case "NOT":
                generatedCode = generateTempVar() + ".bool";
                code.append(String.format("%s :=.bool !.bool %s;\n",
                        generatedCode,
                        visit(node.getJmmChild(0), scope).get(0)
                ));
                if (visitResult.size() > 2 && visitResult.get(1).equals("Constant")) {
                    return Arrays.asList(generatedCode, "Constant", visitResult.get(2).equals("1") ? "0" : "1");
                }
                break;
            case "SIM":
                generatedCode = generateTempVar() + (".i32");
                code.append(String.format("%s :=.i32 0.i32 -.i32 %s;\n",
                        generatedCode,
                        visitResult.get(0)
                ));
                if (visitResult.size() > 2 && visitResult.get(1).equals("Constant")) {
                    int value = -Integer.parseInt(visitResult.get(2));
                    return Arrays.asList(generatedCode, "Constant", Integer.toString(value));
                }
                break;
            default:
                throw new RuntimeException("Invalid Unary operator");
        }
        return List.of(generatedCode);
    }

    public List<String> visitWhile(JmmNode node, String scope) {
        int labelIdx = labels++;
        String loopString = String.format("Loop%d: \n", labelIdx);
        code.append(loopString);
        List<String> conditionResult = visit(node.getJmmChild(0), scope);
        String condition = conditionResult.get(0);
        if (conditionResult.size() > 2 && conditionResult.get(1).equals("Constant")) {
            if (conditionResult.get(2).equals("1")) {
                for (var child: node.getJmmChild(1).getChildren()) {
                    visit(child, scope);
                }
                condition = visit(node.getJmmChild(0), scope).get(0);
                code.append(String.format("if (%s) goto Loop%d;\n", condition, labelIdx));
            } else {
                int startIndexOfLoopString = code.lastIndexOf(loopString);
                code.delete(startIndexOfLoopString, startIndexOfLoopString + loopString.length());
            }
        } else {
            code.append(String.format("if (!.bool %s) goto EndLoop%d;\n", condition, labelIdx));
            for (var child: node.getJmmChild(1).getChildren()) {
                visit(child, scope);
            }
            code.append(String.format("goto Loop%d;\n", labelIdx));
            code.append(String.format("EndLoop%d: \n", labelIdx));
        }
        return null;
    }

    private List<String> visitIfStm(JmmNode node, String scope) {
        int labelIdx = labels++;
        String condition = visit(node.getJmmChild(0), scope).get(0);
        code.append(String.format("if (!.bool %s) goto Else%d;\n", condition, labelIdx));
        for (var child: node.getJmmChild(1).getChildren()) {
            visit(child, scope);
        }
        code.append(String.format("goto EndIf%d;\n", labelIdx));
        code.append(String.format("Else%d: \n", labelIdx));
        for (var child: node.getJmmChild(2).getChildren()) {
            visit(child, scope);
        }
        code.append(String.format("EndIf%d: \n", labelIdx));
        return null;
    }

    private List<String> visitLength(JmmNode node, String scope) {
        String arrAccess = visit(node.getJmmChild(0), scope).get(0);
        String tempVar = generateTempVar() + ".i32";
        code.append(String.format("%s :=.i32 arraylength(%s).i32;\n", tempVar, arrAccess));
        return List.of(tempVar);
    }

    private void injectImports() {
        for (var importStm: symbolTable.getImports()) {
            code.append(String.format("import %s;\n", importStm));
        }
        code.append("\n");
    }

    private String generateTempVar() {
        return String.format("t%d", this.numTempVars++);
    }

    private String buildArrAccess(JmmNode arrAccess, String arrIdx, String scope) {
        String arrName = arrAccess.getJmmChild(0).get("image");
        return buildArrAccess(arrName, symbolTable.getSymbolOrigin(scope, arrName), arrIdx, scope);
    }

    private String buildArrAccess(String arrName, Origin arrOrigin, String arrIdx, String scope) {
        String fixedArrIdx = arrIdx;
        if (!OllirUtils.isVariableOrLiteral(arrIdx)) {
            fixedArrIdx = generateTempVar() + ".i32";
            code.append(String.format("%s :=.i32 %s;\n", fixedArrIdx, arrIdx));
        }
        switch (arrOrigin) {
            case IMPORT_PATH:
                throw new RuntimeException("Class cannot be accessed as an array");
            case PARAMS: {
                int paramIndex = symbolTable.getParamIndex(scope, arrName);
                return String.format("$%d.%s[%s].i32", paramIndex, arrName, fixedArrIdx);
            }
            case CLASS_FIELD: {
                String tempVar = generateTempVar();
                code.append(String.format("%s.array.i32 :=.array.i32 getfield(this, %s.array.i32).array.i32;\n", tempVar, arrName));
                return String.format("%s[%s].i32", tempVar, fixedArrIdx);
            }
            default: {
                return String.format("%s[%s].i32", arrName, fixedArrIdx);
            }
        }
    }

    private static String defaultConstructor(String className) {
        return String.format(".construct %s().V {\n" +
                "invokespecial(this, \"<init>\").V;\n" +
                "}\n", className);
    }

    public String getCode() {
        return code.toString();
    }
}
