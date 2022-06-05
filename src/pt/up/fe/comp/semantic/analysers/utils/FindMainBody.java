package pt.up.fe.comp.semantic.analysers.utils;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PostorderJmmVisitor;

public class FindMainBody extends PostorderJmmVisitor<Object, Object> {
    private JmmNode mainMethodBody;
    public FindMainBody() {
        addVisit("MainMethodDeclaration", this::visitMainMethodDeclaration);
    }

    private Object visitMainMethodDeclaration(JmmNode node, Object dummy) {
        mainMethodBody = node.getJmmChild(1);
        return null;
    }

    public JmmNode getMainMethodBody() {
        return mainMethodBody;
    }
}
