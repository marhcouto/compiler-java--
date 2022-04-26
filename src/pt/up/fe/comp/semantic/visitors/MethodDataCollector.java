package pt.up.fe.comp.semantic.visitors;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.semantic.Method;
import pt.up.fe.comp.semantic.OSymbol;

import java.util.List;
import java.util.Map;

public class MethodDataCollector extends AJmmVisitor<Method, Boolean> {
    private Map<String, Method> methods;

    private Map<String, Symbol> parentScope;

    public MethodDataCollector(Map<String, Symbol> parentScope) {
        this.parentScope = parentScope;

        addVisit("MainMethodDeclaration", this::visitMainMethodDecl);
        addVisit("MethodDeclaration", this::visitMethodDecl);
        //addVisit("VarDeclaration", this::visitVarDecl);
    }

    public Boolean visitMainMethodDecl(JmmNode node, Method dummy) {
        List<JmmNode> methodChildren = node.getChildren();
        Method newMethod = new Method("main", new Type("String", true), parentScope);
        for (var childDecl: methodChildren.get(1).getChildren()) {
            visit(childDecl, newMethod);
        }
        methods.put(newMethod.getName(), newMethod);
        return true;
    }

    public Boolean visitMethodDecl(JmmNode node, Method dummy) {
        boolean isArray = true;
        try {
            node.getChildren().get(0).get("arr");
        } catch (NullPointerException e) {
            isArray = false;
        }
        Type type = new Type(node.getChildren().get(0).get("image"), isArray);
        String methodName = (String) node.getChildren().get(0).get("image");
        Method method = new Method(methodName, type, this.parentScope);
        return null;
    }
}
