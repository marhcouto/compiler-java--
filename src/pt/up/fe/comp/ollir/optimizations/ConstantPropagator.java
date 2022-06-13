package pt.up.fe.comp.ollir.optimizations;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ConstantPropagator extends PreorderJmmVisitor<Boolean, Object> {
    private Map<String, JmmNode> scopeConstants;
    private Set<String> blackListedVariables;

    public ConstantPropagator() {
        addVisit("AsmOp", this::visitAsmOp);
        addVisit("MethodBody", this::visitMethodBody);
        addVisit("VarName", this::visitVarName);
        addVisit("LoopBody", this::visitLoopBody);
    }

    private Object visitVarName(JmmNode node, Object dummy) {
        if (node.getJmmParent().getKind().equals("Variable") || !scopeConstants.containsKey(node.get("image"))) {
            return null;
        }
        node.replace(scopeConstants.get(node.get("image")));
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

    private Object visitAsmOp(JmmNode node, Boolean insideLoop) {
        JmmNode valueNode = node.getJmmChild(1);
        if (!node.getJmmChild(0).getKind().equals("VarName")) {
            return null;
        }
        if (((valueNode.getKind().equals("IntegerLiteral") || valueNode.getKind().equals("True") || valueNode.getKind().equals("False"))) && insideLoop != null) {
            scopeConstants.put(node.getJmmChild(0).get("image"), node.getJmmChild(1));
            node.getJmmParent().removeJmmChild(node);
        } else if (scopeConstants.containsKey(node.getJmmChild(0).get("image"))) {
            // Removes variable if it is no longer a constant
            scopeConstants.remove(node.getJmmChild(0).get("image"));
        }
        return null;
    }

    private Object visitLoopBody(JmmNode node, Object dummy) {
        for (var child: node.getChildren()) {
            visit(child, true);
        }
        return null;
    }
}
