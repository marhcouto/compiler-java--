package pt.up.fe.comp.ollir.optimizations;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;

import java.util.HashMap;
import java.util.Map;

public class CheckIfVarUsedInsideLoop extends PreorderJmmVisitor<Boolean, Map<String, JmmNode>> {
    Map<String, JmmNode> blacklistedVariables;

    public CheckIfVarUsedInsideLoop() {
        addVisit("LoopBody", this::visitLoopBody);
        addVisit("AsmOp", this::visitAsmOp);
        addVisit("MethodBody", this::visitMethodBody);
    }

    private Map<String, JmmNode> visitLoopBody(JmmNode node, Boolean dummy) {
        for (var child: node.getChildren()) {
            visit(child, true);
        }
        return null;
    }

    private Map<String, JmmNode> visitMethodBody(JmmNode node, Boolean dummy) {
        blacklistedVariables = new HashMap<>();
        for (var child: node.getChildren()) {
            visit(child);
        }
        return blacklistedVariables;
    }

    private Map<String, JmmNode> visitAsmOp(JmmNode node, Boolean insideLoop) {
        String varName = node.getJmmChild(0).get("image");
        if (insideLoop != null && !blacklistedVariables.containsKey(varName)) {
            blacklistedVariables.put(varName, node.getJmmChild(0));
        }
        return null;
    }
}
