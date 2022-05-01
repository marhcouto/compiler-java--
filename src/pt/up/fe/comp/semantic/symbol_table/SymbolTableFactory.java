package pt.up.fe.comp.semantic.symbol_table;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.semantic.symbol_table.visitors.ClassDataCollector;
import pt.up.fe.comp.semantic.symbol_table.visitors.ImportCollector;
import pt.up.fe.comp.semantic.symbol_table.visitors.MethodDataCollector;

import java.util.List;

public class SymbolTableFactory {

    private final ImportCollector importVisitor = new ImportCollector();
    private final ClassDataCollector classVisitor = new ClassDataCollector();

    public SymbolTable generateTable(JmmNode node, List<Report> reports) {
        this.importVisitor.visit(node);
        this.classVisitor.visit(node);
        MethodDataCollector methodVisitor = new MethodDataCollector(this.classVisitor.getFields(), reports);
        methodVisitor.visit(node);
        return new SymbolTable(
                this.classVisitor.getThisSuper(),
                this.classVisitor.getClassName(),
                this.importVisitor.getImports(),
                this.classVisitor.getFields(),
                methodVisitor.getMethods(),
                reports
        );
    }
}
