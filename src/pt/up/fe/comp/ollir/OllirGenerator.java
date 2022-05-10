package pt.up.fe.comp.ollir;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.semantic.models.Origin;
import pt.up.fe.comp.semantic.symbol_table.SymbolTable;

public class OllirGenerator extends AJmmVisitor<String, String> {
    private final StringBuilder code = new StringBuilder();
    private final SymbolTable symbolTable;

    private int numTempVars = 0;

    OllirGenerator(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        addVisit("Start", this::visitStart);
        addVisit("ClassDeclaration", this::visitClassDeclaration);
        addVisit("MainMethodDeclaration", this::visitMainMethod);
        addVisit("MethodDeclaration", this::visitMethod);
        addVisit("MethodBody", this::visitMethodBody);
        addVisit("FnCallOp", this::visitFnCallOp);
        addVisit("VarName", this::visitVarName);
        addVisit("IntegerLiteral", (node, dummy) -> node.get("image"));
        addVisit("False", (node, dummy) -> "false");
        addVisit("True", (node, dummy) -> "true");
        addVisit("ArgumentList", this::visitArgumentList);
        addVisit("BinOp", this::visitBinOp);
        addVisit("CreateObj", this::visitCreateObj);
        addVisit("CreateArrObj", this::visitCreateArrObj);
        addVisit("Variable", this::visitVariable);
        addVisit("ReturnStatement", this::visitReturnStatement);
        addVisit("AsmOp", this::visitAsmOp);
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
        String operator = switch (node.get("op")) {
            case "ADD" -> "+";
            case "MUL" -> "*";
            case "DIV" -> "/";
            case "LT" -> "<";
            case "SUB" -> "-";
            case "AND" -> "&&";
            default -> throw new RuntimeException("To make the compiler calm");
        };
        code.append(String.format("%s.%s :=.%s %s.%s %s.%s %s.%s;\n",
                tempVar,
                OllirUtils.toOllir(node),
                OllirUtils.toOllir(node),
                lhs,
                OllirUtils.toOllir(node.getJmmChild(0)),
                operator,
                OllirUtils.toOllir(node),
                rhs,
                OllirUtils.toOllir(node.getJmmChild(1))
        ));
        return tempVar;
    }
    private String visitFnCallOp(JmmNode node, String scope) {
        String called = null;
        String curVar = null;
        if (OllirUtils.needToPlaceVariable(node.getJmmChild(0))) {
            called = visit(node.getJmmChild(0), scope);
        }
        if (OllirUtils.needToPlaceVariable(node)) {
            String type = OllirUtils.toOllir(node);
            curVar = generateTempVar();
            code.append(String.format("%s.%s:=.%s ", curVar, type, type));
        }
        if (node.getJmmChild(0).getKind().equals("VarName")){
            called = node.getJmmChild(0).get("image");
        }
        injectCall(called, node);
        return curVar;
    }

    private String visitCreateObj(JmmNode node, String dummy) {
        String tempVar = generateTempVar();
        String type = OllirUtils.toOllir(node);
        code.append(String.format("%s.%s :=.%s new(%s).%s;\n", tempVar, type, type, type, type));
        code.append(String.format("invokespecial(%s.%s, \"<init>\").V;\n", tempVar, type));
        return tempVar;
    }

    private String visitCreateArrObj(JmmNode node, String scope) {
        String size = visit(node.getJmmChild(0), scope);
        String tempVar = generateTempVar();
        code.append(String.format("%s.array :=.array new(array, %s.i32).array;\n", tempVar, size));
        return tempVar;
    }

    private void injectCall(String called, JmmNode node) {
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
        code.append(String.format(").%s;\n", OllirUtils.toOllir(node)));
    }

    private String visitArgumentList(JmmNode node, String scope) {
        if (!node.getChildren().isEmpty()) {
            code.append(visit(node.getJmmChild(0), scope));
        }
        for (var child: node.getChildren()) {
            code.append(String.format(", %s", visit(child, scope)));
        }
        return null;
    }

    public String visitVariable(JmmNode node, String scope) {
        if (!node.getJmmParent().getKind().equals("MethodArgsList")) {
            return "";
        }
        String variableName = node.getJmmChild(1).get("image");
        return String.format("%s.%s", variableName,
            OllirUtils.toOllir(symbolTable.getSymbol(scope, variableName))
        );
    }

    private String visitVarName(JmmNode node, String scope) {
        Origin varOrigin = symbolTable.getSymbolOrigin(scope, node.get("image"));
        switch (varOrigin) {
            case LOCAL, IMPORT_PATH:
                return node.get("image");
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
                return newVar;
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
                    code.append(String.format("putfield(this, %s.%s, %s.%s).%s;\n",
                        varName,
                        OllirUtils.toOllir(node.getJmmChild(0)),
                        expr,
                        OllirUtils.toOllir(node.getJmmChild(1)),
                        OllirUtils.toOllir(node.getJmmChild(0))
                    ));
                    break;
                }
                String dest = visit(node.getJmmChild(0), scope);
                code.append(String.format("%s.%s :=.%s %s.%s;\n",
                   dest,
                   OllirUtils.toOllir(node.getJmmChild(0)),
                   OllirUtils.toOllir(node.getJmmChild(0)),
                   expr,
                   OllirUtils.toOllir(node.getJmmChild(1))
                ));
            }
        }
        return null;
    }

    private String visitReturnStatement(JmmNode node, String scope) {
        String retVal = visit(node.getJmmChild(0), scope);
        code.append(String.format("ret.%s %s.%s;\n",
            OllirUtils.toOllir(symbolTable.getReturnType(scope)),
            retVal,
            OllirUtils.toOllir(node.getJmmChild(0))
        ));
        return null;
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

    public static String defaultConstructor(String className) {
        return String.format(".construct %s().V {\n" +
                "invokespecial(this, \"<init>\").V;\n" +
                "}\n", className);
    }

    public String getCode() {
        return code.toString();
    }
}
