package pt.up.fe.comp.semantic;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.semantic.visitors.ClassDataCollector;
import pt.up.fe.comp.semantic.visitors.ImportCollector;
import pt.up.fe.comp.semantic.visitors.MethodDataCollector;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class SymbolTableFactory {

    private final ImportCollector importVisitor = new ImportCollector();
    private final ClassDataCollector classVisitor = new ClassDataCollector();

    public SymbolTable generateTable(JmmNode node) {
        this.importVisitor.visit(node);
        this.classVisitor.visit(node);
        List<Report> reportList = new LinkedList<>();
        MethodDataCollector methodVisitor = new MethodDataCollector(this.classVisitor.getFields(), reportList);
        methodVisitor.visit(node);
        return new SymbolTable(
                this.classVisitor.getThisSuper(),
                this.classVisitor.getClassName(),
                this.importVisitor.getImports(),
                this.classVisitor.getFields(),
                methodVisitor.getMethods(),
                reportList
        );
    }
}
