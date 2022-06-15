package pt.up.fe.comp.ollir.optimizations;

import pt.up.fe.comp.IntegerLiteral;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;

import java.util.*;

public class ConstantPropagator extends AJmmVisitor<Object, Object> {
    private Map<String, JmmNode> scopeConstants;
    private Map<String, JmmNode> blackListedVariables;

    public ConstantPropagator() {
        addVisit("AsmOp", this::visitAsmOp);
        addVisit("MethodBody", this::visitMethodBody);
        addVisit("VarName", this::visitVarName);
        setDefaultVisit((node, dummy) -> {
            for (var child : node.getChildren()) {
                visit(child);
            }
            return null;
        });
    }

    private Object visitVarName(JmmNode node, Object dummy) {
        if (node.getJmmParent().getKind().equals("Variable") || !scopeConstants.containsKey(node.get("image"))) {
            return null;
        }
        node.replace(JmmNodeImpl.fromJson(scopeConstants.get(node.get("image")).toJson()));
        return null;
    }

    private Object visitMethodBody(JmmNode node, Object dummy) {
        scopeConstants = new HashMap<>();
        blackListedVariables = new CheckIfVarUsedInsideLoop().visit(node);
        for (var child: node.getChildren()) {
            visit(child);
        }
        return null;
    }

    private Object visitAsmOp(JmmNode node, Object dummy) {
        visit(node.getJmmChild(1));
        if (!node.getJmmChild(0).getKind().equals("VarName")) {
            visit(node.getJmmChild(0));
        }
        if(!node.getJmmChild(0).getKind().equals("VarName")) {
            return null;
        }
        String varName = node.getJmmChild(0).get("image");
        JmmNode valueNode = node.getJmmChild(1);
        if (!node.getJmmChild(0).getKind().equals("VarName")) {
            return null;
        }
        if (((valueNode.getKind().equals("IntegerLiteral") || valueNode.getKind().equals("True") || valueNode.getKind().equals("False"))) && (!blackListedVariables.containsKey(varName) || Integer.parseInt(blackListedVariables.get(varName).get("line")) > Integer.parseInt(valueNode.get("line")))) {
            JmmNode constant = node.getJmmChild(1);
            constant.removeParent();
            scopeConstants.put(varName, constant);
            node.getJmmParent().removeJmmChild(node);
        } else if (scopeConstants.containsKey(varName)) {
            // Removes variable if it is no longer a constant
            scopeConstants.remove(varName);
        }
        return null;
    }
}
