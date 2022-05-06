package pt.up.fe.comp.semantic.symbol_table;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.semantic.models.Method;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

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

    public SymbolTable(String superName, String className, List<String> imports, Map<String, Symbol> fields, Map<String, Method> methods) {
        this(superName, className, imports, fields, methods, Collections.emptyList());
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

    public Method getMethodScope(String name) {
        return methods.get(name);
    }

    public Boolean hasSymbolInImportPath(String symbol) {
        /* The following regex tries to match the imput symbol against the end of an import statement
         * to check if a given symbol was imported.
         */
        Pattern pattern = Pattern.compile(String.format("%s\\b", symbol));
        for (var importPath: imports) {
            if (pattern.matcher(importPath).find()) {
                return true;
            }
        }
        return false;
    }

    public Boolean hasSymbol(String methodName, String symbolName) {
        return methods.get(methodName).getVariables().containsKey(symbolName) || fields.containsKey(symbolName) || hasSymbolInImportPath(symbolName);
    }

    public Symbol getSymbol(String methodName, String symbolName) {
        if (methods.get(methodName).getVariables().containsKey(symbolName)) {
            return methods.get(methodName).getVariables().get(symbolName);
        }
        if (fields.containsKey(symbolName)) {
            return fields.get(symbolName);
        }
        if (hasSymbolInImportPath(symbolName)) {
            return new Symbol(
                new Type(symbolName, false),
                "symbolName"
            );
        }
        return null;
    }

    @Override
    public List<Symbol> getLocalVariables(String methodSignature) {
        return new LinkedList<>(methods.get(methodSignature).getLocalVars().values());
    }
}
