package pt.up.fe.comp.ollir.optimizations;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ConstantPropagator extends PreorderJmmVisitor<Object, Object> {
    private Map<String, JmmNode> scopeConstants;
    private Set<String> blackListedVariables;

    public ConstantPropagator() {
        addVisit("AsmOp", this::visitAsmOp);
        addVisit("MethodBody", this::visitMethodBody);
        addVisit("VarName", this::visitVarName);
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

    private Object visitAsmOp(JmmNode node, Object dummy) {
        if(!node.getJmmChild(0).getKind().equals("VarName")) {
            return null;
        }
        String varName = node.getJmmChild(0).get("image");
        JmmNode valueNode = node.getJmmChild(1);
        if (!node.getJmmChild(0).getKind().equals("VarName")) {
            return null;
        }
        if (((valueNode.getKind().equals("IntegerLiteral") || valueNode.getKind().equals("True") || valueNode.getKind().equals("False"))) && !blackListedVariables.contains(varName)) {
            scopeConstants.put(varName, node.getJmmChild(1));
            node.getJmmParent().removeJmmChild(node);
        } else if (scopeConstants.containsKey(varName)) {
            // Removes variable if it is no longer a constant
            scopeConstants.remove(varName);
        }
        return null;
    }
}
