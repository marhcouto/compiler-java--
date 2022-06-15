package pt.up.fe.comp.semantic.protectors;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;

public class ChangeFieldInVarName extends PreorderJmmVisitor<Object, Object> {
    public ChangeFieldInVarName() {
        addVisit("VarName", this::visitVarName);
    }

    private Object visitVarName(JmmNode node, Object dummy) {
        if(node.get("image").equals("field")) {
            node.put("image", "c7b56fc6ae72486a99347336c378dd06");
        }
        return null;
    }
}
