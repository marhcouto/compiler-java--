package pt.up.fe.comp.ollir.optimizations;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;

import java.util.HashSet;
import java.util.Set;

public class CheckIfVarUsedInsideLoop extends PreorderJmmVisitor<Boolean, Set<String>> {
    Set<String> blacklistedVariables;

    public CheckIfVarUsedInsideLoop() {
        addVisit("LoopBody", this::visitLoopBody);
        addVisit("AsmOp", this::visitAsmOp);
        addVisit("MethodBody", this::visitMethodBody);
    }

    private Set<String> visitLoopBody(JmmNode node, Boolean dummy) {
        for (var child: node.getChildren()) {
            visit(child, true);
        }
        return null;
    }

    private Set<String> visitMethodBody(JmmNode node, Boolean dummy) {
        blacklistedVariables = new HashSet<>();
        for (var child: node.getChildren()) {
            visit(child);
        }
        return blacklistedVariables;
    }

    private Set<String> visitAsmOp(JmmNode node, Boolean insideLoop) {
        if (insideLoop != null) {
            blacklistedVariables.add(node.getJmmChild(0).get("image"));
        }
        return null;
    }
}
