package pt.up.fe.comp.ollir;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.semantic.symbol_table.SymbolTable;

public class OllirGenerator extends AJmmVisitor<String, OllirInstruction> {
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
        addVisit("VarName", (node, dummy) -> new OllirInstruction("", node.get("image")));
        addVisit("ArgumentList", this::visitArgumentList);
        setDefaultVisit((node, dummy) -> new OllirInstruction("", ""));
    }

    private OllirInstruction visitStart(JmmNode node, String dummy) {
        injectImports();
        for (var child: node.getChildren()) {
            visit(child);
        }
        return null;
    }

    private OllirInstruction visitClassDeclaration(JmmNode node, String dummy) {
        String extendsStr = symbolTable.getSuper() != null ? String.format("extends %s", symbolTable.getSuper()) : "";
        StringBuilder classBody = new StringBuilder();
        classBody.append(OllirUtils.generateFields(symbolTable.getFields()));
        classBody.append(OllirUtils.defaultConstructor(symbolTable.getClassName()));
        for (var child: node.getChildren()) {
            classBody.append(visit(child));
        }
        code.append(String.format("public %s %s {\n%s\n}", symbolTable.getClassName(), extendsStr, classBody));
        return null;
    }

    private OllirInstruction visitMainMethod(JmmNode node, String dummy) {
        numTempVars = 0;
        String mainArgsName = node.getJmmChild(0).get("image");
        return new OllirInstruction(
            "",
            String.format(".method public static main(%s.array.String).V{\n%s}\n", mainArgsName, visit(node.getJmmChild(1), "main"))
        );
    }

    private OllirInstruction visitMethod(JmmNode node, String dummy) {
        numTempVars = 0;
        return new OllirInstruction("", "");
    }

    public OllirInstruction visitMethodBody(JmmNode node, String scope) {
        StringBuilder methodBody = new StringBuilder();
        for (var child: node.getChildren()) {
            methodBody.append(visit(child, scope));
        }
        return new OllirInstruction("", methodBody.toString());
    }

    public OllirInstruction visitFnCallOp(JmmNode node, String scope) {
        String tempVar = null;
        JmmNode called = node.getJmmChild(0);
        JmmNode method = node.getJmmChild(1);
        StringBuilder fnCallCode = new StringBuilder();
        if (OllirUtils.canInline(node)) {

        }
        if (OllirUtils.isStatic(called)) {
            fnCallCode.append(String.format("invokestatic(%s, \"%s\"", called.get("image"), method.get("image")));
        }
        fnCallCode.append(String.format(").%s;\n", OllirUtils.toOllir(node)));
    }

    private OllirInstruction visitArgumentList(JmmNode node, String scope) {
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

    public String getCode() {
        return code.toString();
    }
}
