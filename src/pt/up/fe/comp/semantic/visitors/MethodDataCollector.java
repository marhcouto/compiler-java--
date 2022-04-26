package pt.up.fe.comp.semantic.visitors;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.semantic.Method;
import pt.up.fe.comp.semantic.OSymbol;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MethodDataCollector extends AJmmVisitor<Object, Symbol> {
    private Map<String, Method> methods;

    private Map<String, Symbol> parentScope;

    public MethodDataCollector(Map<String, Symbol> parentScope) {
        this.parentScope = parentScope;

        addVisit("Start", this::visitStart);
        addVisit("MainMethodDeclaration", this::visitMainMethodDecl);
        addVisit("MethodDeclaration", this::visitMethodDecl);
        addVisit("VarDeclaration", this::visitVarDecl);
        setDefaultVisit((node, dummy) -> null);
    }

    public Symbol visitStart(JmmNode node, Object dummy) {
        for (var child: node.getChildren()) {
            visit(child);
        }
        return null;
    }

    private Symbol visitMainMethodDecl(JmmNode node, Object dummy) {
        Map<String, OSymbol> methodAttributeMap = new HashMap<>();
        List<JmmNode> methodChildren = node.getChildren();
        Method newMethod = new Method("main", new Type("void", false), parentScope);
        methodAttributeMap.put(methodChildren.get(0).get("image"), new OSymbol(
            new Type("String", true),
            methodChildren.get(0).get("image"),
            Origin.PARAMS
        ));
        for (var childDecl: methodChildren.get(1).getChildren()) {
            Symbol visitResult = visit(childDecl);
            if (visitResult != null) {
                newMethod.getVariables().put(visitResult.getName(), OSymbol.fromSymbol(visitResult, Origin.LOCAL));
            }
        }
        methods.put(newMethod.getName(), newMethod);
        return null;
    }

    private Symbol visitMethodDecl(JmmNode node, Object dummy) {
        boolean isArray = true;
        try {
            node.getChildren().get(0).get("arr");
        } catch (NullPointerException e) {
            isArray = false;
        }
        Type type = new Type(node.getChildren().get(0).get("image"), isArray);
        String methodName = node.getChildren().get(1).get("image");
        Method method = new Method(methodName, type, this.parentScope);
        for (var childDecl: node.getChildren().get(2).getChildren()) {
            Symbol visitResult = visit(childDecl);
            if (visitResult != null) {
                method.getVariables().put(visitResult.getName(), OSymbol.fromSymbol(visitResult, Origin.PARAMS));
            }
        }
        for (var childDecl: node.getChildren().get(3).getChildren()) {
            Symbol visitResult = visit(childDecl);
            if (visitResult != null) {
                method.getVariables().put(visitResult.getName(), OSymbol.fromSymbol(visitResult, Origin.LOCAL));
            }
        }
        return null;
    }

    private Symbol visitVarDecl(JmmNode node, Object dummy) {
        List<JmmNode> varDecl = node.getChildren();
        boolean isArray = true;
        try {
            node.getChildren().get(0).get("arr");
        } catch (NullPointerException e) {
            isArray = false;
        }
        return new OSymbol(new Type(node.getChildren().get(0).get("image"), isArray),
                node.getChildren().get(1).get("image"),
                Origin.LOCAL
        );
    }

    public Map<String, Method> getMethods() {
        return methods;
    }
}
