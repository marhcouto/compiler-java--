package pt.up.fe.comp.semantic.analysers;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

public class ScopeAnnotator extends AJmmVisitor<String, Boolean> {
    public ScopeAnnotator() {
        addVisit("Start", this::visitStart);
        addVisit("ClassDeclaration", this::visitClassDeclaration);
        addVisit("MainMethodDeclaration", this::visitMainMethodDeclaration);
        addVisit("MethodDeclaration", this::visitMethodDeclaration);
        addVisit("ArgumentList", this::visitArgumentList);
        //Prevents var decl annotation
        addVisit("Variable", (node, dummy) -> false);
        addVisit("ReturnStatement", this::visitReturnStatement);
        addVisit("VarName", this::visitVarName);
        setDefaultVisit(this::defaultVisitor);
    }

    private Boolean defaultVisitor(JmmNode node, String scope) {
        if (scope == null) {
            return false;
        }
        for (var child: node.getChildren()) {
            visit(child, scope);
        }
        return true;
    }

    private Boolean visitStart(JmmNode node, String dummy) {
        for (var child: node.getChildren()) {
            visit(child);
        }
        return true;
    }

    private Boolean visitClassDeclaration(JmmNode node, String dummy) {
        for (var child: node.getChildren()) {
            visit(child);
        }
        return true;
    }

    private Boolean visitMainMethodDeclaration(JmmNode node, String dummy) {
        for (var child: node.getChildren()) {
            visit(child, "main");
        }
        return true;
    }

    private Boolean visitMethodDeclaration(JmmNode node, String dummy) {
        String scopeName = node.getJmmChild(1).get("image");
        for (var child: node.getChildren()) {
            visit(child, scopeName);
        }
        return true;
    }

    private Boolean visitReturnStatement(JmmNode node, String scopeName) {
        visit(node.getJmmChild(0), scopeName);
        return true;
    }

    private Boolean visitVarName(JmmNode node, String scopeName) {
        node.put("scope", scopeName);
        return true;
    }

    private Boolean visitArgumentList(JmmNode node, String scopeName) {
        node.put("ownerMethod", node.getJmmParent().getJmmChild(1).get("image"));
        defaultVisitor(node, scopeName);
        return true;
    }
}
