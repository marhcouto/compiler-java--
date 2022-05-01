package pt.up.fe.comp.semantic;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.report.Report;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SymbolTable implements pt.up.fe.comp.jmm.analysis.table.SymbolTable {
    private List<Report> reports;
    private final String thisSuper;

    private final String className;
    private final List<String> imports;
    private final Map<String, Symbol> fields;
    private final Map<String, Method> methods;

    public SymbolTable(String superName, String className, List<String> imports, Map<String, Symbol> fields, Map<String, Method> methods, List<Report> reports) {
        this.thisSuper = superName;
        this.className = className;
        this.imports = imports;
        this.fields = fields;
        this.methods = methods;
        this.reports = reports;
    }

    @Override
    public List<String> getImports() {
        return imports;
    }

    @Override
    public String getClassName() {
        return className;
    }

    @Override
    public String getSuper() {
        return thisSuper;
    }

    @Override
    public List<Symbol> getFields() {
        return new LinkedList<>(fields.values());
    }

    @Override
    public List<String> getMethods() {
        return new LinkedList<>(methods.keySet());
    }

    @Override
    public Type getReturnType(String methodSignature) {
        return methods.get(methodSignature).getReturnType();
    }

    @Override
    public List<Symbol> getParameters(String methodSignature) {
        return new LinkedList<>(methods.get(methodSignature).getParameters().values());
    }

    @Override
    public List<Symbol> getLocalVariables(String methodSignature) {
        return new LinkedList<>(methods.get(methodSignature).getLocalVars().values());
    }
}
