package pt.up.fe.comp.ollir;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.semantic.Constants;
import pt.up.fe.comp.semantic.models.Origin;
import pt.up.fe.comp.semantic.symbol_table.SymbolTable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OllirGenerator extends AJmmVisitor<String, String> {
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
        addVisit("IntegerLiteral", (node, dummy) -> String.format("%s.%s", node.get("image"), "i32"));
        addVisit("False", (node, dummy) -> "0" + ".bool");
        addVisit("True", (node, dummy) -> "1" + ".bool");
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

    private String visitStart(JmmNode node, String dummy) {
        injectImports();
        for (var child: node.getChildren()) {
            visit(child);
        }
        return null;
    }

    private String visitClassDeclaration(JmmNode node, String dummy) {
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

    private String visitMainMethod(JmmNode node, String dummy) {
        numTempVars = 0;
        String mainArgsName = node.getJmmChild(0).get("image");
        code.append(String.format(".method public static main(%s.array.String).V{\n", mainArgsName));
        visit(node.getJmmChild(1), "main");
        code.append("ret.V;\n");
        code.append("}\n");
        return null;
    }

    private String visitMethod(JmmNode node, String scope) {
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

    private String visitMethodBody(JmmNode node, String scope) {
        for (var child: node.getChildren()) {
            visit(child, scope);
        }
        return null;
    }

    private String visitBinOp(JmmNode node, String scope) {
        String lhs = visit(node.getJmmChild(0), scope);
        String rhs = visit(node.getJmmChild(1), scope);
        String tempVar = generateTempVar();
        String operator;
        switch (node.get("op")) {
            case "ADD":
                operator = "+";
                break;
            case "MUL":
                operator = "*";
                break;
            case "DIV":
                operator = "/";
                break;
            case "LT":
                operator = "<";
                break;
            case "SUB":
                operator = "-";
                break;
            case "AND":
                operator = "&&";
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
        return String.format("%s.%s", tempVar, OllirUtils.toOllir(node));
    }
    private String visitFnCallOp(JmmNode node, String scope) {
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
            called = visit(node.getJmmChild(0), scope);
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
        return curVar;
    }

    private String visitCreateObj(JmmNode node, String dummy) {
        String tempVar = generateTempVar();
        String type = OllirUtils.toOllir(node);
        code.append(String.format("%s.%s :=.%s new(%s).%s;\n", tempVar, type, type, type, type));
        code.append(String.format("invokespecial(%s.%s, \"<init>\").V;\n", tempVar, type));
        return String.format("%s.%s", tempVar, type);
    }

    private String visitCreateArrObj(JmmNode node, String scope) {
        String size = visit(node.getJmmChild(0), scope);
        String tempVar = generateTempVar();
        code.append(String.format("%s.array.i32 :=.array.i32 new(array, %s).array.i32;\n", tempVar, size));
        return tempVar + ".array.i32";
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
            argExpressions.add(visit(argList.getChildren().get(i), scope));
        }
        return argExpressions;
    }

    public String visitVariable(JmmNode node, String scope) {
        if (!node.getJmmParent().getKind().equals("MethodArgsList")) {
            return "";
        }
        String variableName = node.getJmmChild(1).get("image");
        return OllirUtils.toOllir(symbolTable.getSymbol(scope, variableName));
    }

    private String visitVarName(JmmNode node, String scope) {
        Origin varOrigin = symbolTable.getSymbolOrigin(scope, node.get("image"));
        switch (varOrigin) {
            case LOCAL:
                return String.format("%s.%s", node.get("image"), OllirUtils.toOllir(node));
            case IMPORT_PATH:
                return String.format("%s.%s", node.get("image"), Constants.ANY_TYPE);
            case PARAMS:
                return String.format("$%d.%s.%s",
                    symbolTable.getParamIndex(scope, node.get("image")),
                    node.get("image"),
                    OllirUtils.toOllir(node)
                );
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
                return String.format("%s.%s",newVar, type);
        }
        return null;
    }

    private String visitAsmOp(JmmNode node, String scope) {
        String expr = visit(node.getJmmChild(1), scope);
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
                String dest = visit(node.getJmmChild(0), scope);
                code.append(String.format("%s :=.%s %s;\n",
                   dest,
                   OllirUtils.toOllir(node.getJmmChild(0)),
                   expr
                ));
                break;
            }
            case "ArrAccess": {
                String arrName = node.getJmmChild(0).getJmmChild(0).get("image");
                String accessedIndex = visit(node.getJmmChild(0).getJmmChild(1), scope);
                String arrAccess;
                if (symbolTable.getSymbolOrigin(scope, arrName).equals(Origin.CLASS_FIELD)) {
                    String fetchedField = generateTempVar();
                    code.append(String.format("%s.array.i32 :=.array.i32 getfield(this, %s.array.i32).array.i32;\n", fetchedField, arrName));
                    arrAccess = buildArrAccess(fetchedField, Origin.CLASS_FIELD, accessedIndex, null);
                } else {
                    arrAccess = buildArrAccess(node.getJmmChild(0), accessedIndex, scope);
                }
                code.append(String.format("%s :=.i32 %s;\n",
                        arrAccess,
                        expr
                ));
            }
        }
        return null;
    }

    private String visitReturnStatement(JmmNode node, String scope) {
        String retVal = visit(node.getJmmChild(0), scope);
        code.append(String.format("ret.%s %s;\n",
            OllirUtils.toOllir(symbolTable.getReturnType(scope)),
            retVal
        ));
        return null;
    }

    private String visitMethodArgsList(JmmNode node, String scope) {
        if (node.getChildren().isEmpty()) {
            return null;
        }
        code.append(visit(node.getJmmChild(0), scope));
        for (int i = 1; i < node.getChildren().size(); i++) {
            code.append(String.format(", %s", visit(node.getJmmChild(i), scope)));
        }
        return null;
    }

    private String visitArrAccess(JmmNode node, String scope) {
        String idx = visit(node.getJmmChild(1), scope);
        return buildArrAccess(node, idx, scope);
    }

    private String visitUnaryOp(JmmNode node, String scope) {
        String generatedCode;
        switch (node.get("op")) {
            case "NOT":
                generatedCode = generateTempVar() + ".bool";
                code.append(String.format("%s :=.bool !.bool %s;\n",
                        generatedCode,
                        visit(node.getJmmChild(0), scope)
                ));
                break;
            case "SIM":
                generatedCode = generateTempVar() + (".i32");
                code.append(String.format("%s :=.i32 0.i32 -.i32 %s;\n",
                    generatedCode,
                    visit(node.getJmmChild(0), scope)
                ));
                break;
            default:
                throw new RuntimeException("Invalid Unary operator");
        }
        return generatedCode;
    }

    public String visitWhile(JmmNode node, String scope) {
        int labelIdx = labels++;
        code.append(String.format("Loop%d: \n", labelIdx));
        String condition = visit(node.getJmmChild(0), scope);
        code.append(String.format("if (!.bool %s) goto EndLoop%d;\n", condition, labelIdx));
        for (var child: node.getJmmChild(1).getChildren()) {
            visit(child, scope);
        }
        code.append(String.format("goto Loop%d;\n", labelIdx));
        code.append(String.format("EndLoop%d: \n", labelIdx));
        return null;
    }

    private String visitIfStm(JmmNode node, String scope) {
        int labelIdx = labels++;
        String condition = visit(node.getJmmChild(0), scope);
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

    private String visitLength(JmmNode node, String scope) {
        String arrAccess = visit(node.getJmmChild(0), scope);
        String tempVar = generateTempVar() + ".i32";
        code.append(String.format("%s :=.i32 arraylength(%s).i32;\n", tempVar, arrAccess));
        return tempVar;
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
