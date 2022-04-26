package pt.up.fe.comp.semantic;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SymbolTable implements pt.up.fe.comp.jmm.analysis.table.SymbolTable {
    private String thisSuper;
    private String className;
    private List<String> imports;

    private Map<String, Symbol> attributes;

    private Map<String, Method> methods;

    public SymbolTable(String superName, String className, List<String> imports, Map<String, Symbol> attributes, Map<String, Method> methods) {
        this.thisSuper = superName;
        this.className = className;
        this.imports = imports;
        this.attributes = attributes;
        this.methods = methods;
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
        return new LinkedList<>(attributes.values());
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
