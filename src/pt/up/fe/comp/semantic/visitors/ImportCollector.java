package pt.up.fe.comp.semantic.visitors;

import pt.up.fe.comp.ImportDeclaration;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.LinkedList;
import java.util.List;

public class ImportCollector extends AJmmVisitor<Object, Boolean> {
    private final List<String> imports = new LinkedList<>();

    public ImportCollector() {
        addVisit("Start", this::visitStartNode);
        addVisit("ImportDeclaration", this::visitImportDeclaration);
        setDefaultVisit((node, imports) -> true);
    }

    private Boolean visitStartNode(JmmNode root, Object dummy) {
        for (var child: root.getChildren().get(0).getChildren()) {
            visit(child);
        }
        return true;
    }

    private Boolean visitImportDeclaration(JmmNode importNode, Object dummy) {
        String importPath = "";
        for (var importPathNode: importNode.getChildren()) {
            importPath = String.join(importPath, importPathNode.get("image"));
        }
        imports.add(importPath);
        return true;
    }

    public List<String> getImports() {
        return imports;
    }
}
