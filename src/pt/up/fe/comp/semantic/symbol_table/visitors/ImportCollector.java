package pt.up.fe.comp.semantic.symbol_table.visitors;

import pt.up.fe.comp.ImportDeclaration;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class ImportCollector extends AJmmVisitor<Object, Boolean> {
    private final List<String> imports = new LinkedList<>();

    public ImportCollector() {
        addVisit("Start", this::visitStartNode);
        addVisit("ImportDeclaration", this::visitImportDeclaration);
        setDefaultVisit((node, imports) -> true);
    }

    private Boolean visitStartNode(JmmNode root, Object dummy) {
        for (var child: root.getChildren()) {
            visit(child);
        }
        return true;
    }

    private Boolean visitImportDeclaration(JmmNode importNode, Object dummy) {
        imports.add(importNode
                .getChildren()
                .stream()
                .map(path -> path.get("image"))
                .collect(Collectors.joining("."))
        );
        return true;
    }

    public List<String> getImports() {
        return imports;
    }
}
