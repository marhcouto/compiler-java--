package pt.up.fe.comp.semantic.visitors;

import pt.up.fe.comp.ImportDeclaration;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.LinkedList;
import java.util.List;

public class ImportCollector extends AJmmVisitor<List<String>, Integer> {
    private int visitedNodes;

    public ImportCollector() {
        this.visitedNodes = 0;
        addVisit("Start", this::visitStartNode);
        addVisit("ImportDeclaration", this::visitImportDeclaration);
        addVisit("ImportPath", this::visitImportPath);
        setDefaultVisit((node, imports) -> ++visitedNodes);
    }

    private Integer visitStartNode(JmmNode root, List<String> imports) {
        for (var child: root.getChildren()) {
            visit(child, imports);
        }
        return ++visitedNodes;
    }

    private Integer visitImportDeclaration(JmmNode importNode, List<String> imports) {
        List<String> importPath = new LinkedList<>();
        int importPathNNodes = 0;
        for (var importPathNode: importNode.getChildren()) {
            importPathNNodes += visit(importPathNode, importPath);
        }
        imports.add(String.join(".", importPath));
        visitedNodes += importPathNNodes;
        return importPathNNodes;
    }

    private Integer visitImportPath(JmmNode path, List<String> importDeclPath) {
        importDeclPath.add(path.get("image"));
        return 1;
    }
}
