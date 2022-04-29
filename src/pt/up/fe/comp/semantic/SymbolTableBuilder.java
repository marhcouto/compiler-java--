package pt.up.fe.comp.semantic;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.semantic.visitors.ClassDataCollector;
import pt.up.fe.comp.semantic.visitors.ImportCollector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SymbolTableBuilder {

    private final ImportCollector importVisitor = new ImportCollector();
    private final ClassDataCollector classVisitor = new ClassDataCollector();

    public SymbolTable generateTable(JmmNode node) {
        this.importVisitor.visit(node);
        this.classVisitor.visit(node);
        var map = new HashMap<String, Method>();
        return new SymbolTable(this.classVisitor.getThisSuper(), this.classVisitor.getClassName(), this.importVisitor.getImports(), this.classVisitor.getAttributes(), map);
    }
}
