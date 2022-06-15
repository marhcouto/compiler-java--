package pt.up.fe.comp.semantic.symbol_table;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.semantic.models.ExtendedSymbol;
import pt.up.fe.comp.semantic.models.Method;
import pt.up.fe.comp.semantic.models.Origin;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SymbolTable implements pt.up.fe.comp.jmm.analysis.table.SymbolTable {
    private List<Report> reports;
    private final String thisSuper;
    private final String className;
    private final List<String> imports;
    private final Map<String, ExtendedSymbol> fields;
    private final Map<String, Method> methods;

    public SymbolTable(String superName, String className, List<String> imports, Map<String, ExtendedSymbol> fields, Map<String, Method> methods, List<Report> reports) {
        this.thisSuper = superName;
        this.className = className;
        this.imports = imports;
        this.fields = fields;
        this.methods = methods;
        this.reports = reports;
    }

    public SymbolTable(String superName, String className, List<String> imports, Map<String, ExtendedSymbol> fields, Map<String, Method> methods) {
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

    public List<ExtendedSymbol> getExtendedFields() {
        return new LinkedList<>(fields.values());
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
        return new LinkedList<>(methods.get(methodSignature).getParameters());
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
        return methods.get(methodName).getVariables().containsKey(symbolName) || (fields.containsKey(symbolName) && !methodName.equals("main")) || hasSymbolInImportPath(symbolName);
    }

    public ExtendedSymbol getSymbol(String methodName, String symbolName) {
        if (methods.get(methodName).getVariables().containsKey(symbolName)) {
            return methods.get(methodName).getVariables().get(symbolName);
        }
        if (fields.containsKey(symbolName)) {
            return fields.get(symbolName);
        }
        if (hasSymbolInImportPath(symbolName)) {
            return new ExtendedSymbol(
                new Type(symbolName, false),
                "symbolName", Origin.IMPORT_PATH
            );
        }
        return null;
    }

    public Origin getSymbolOrigin(String methodScope, String symbolName) {
        if (this.getMethodScope(methodScope).getVariables().containsKey(symbolName)) {
            return this.getMethodScope(methodScope).getVariables().get(symbolName).getOrigin();
        } else if (this.fields.containsKey(symbolName)) {
            return Origin.CLASS_FIELD;
        } else if (this.hasSymbolInImportPath(symbolName)) {
            return Origin.IMPORT_PATH;
        }
        throw new RuntimeException("Should not have reached here with missing symbols");
    }

    public int getParamIndex(String methodScope, String paramName) {
        return this.getMethodScope(methodScope)
                .getParameters()
                .stream()
                .filter(symbol -> symbol.getName().equals(paramName))
                .collect(Collectors.toList())
                .get(0)
                .getIndex();
    }

    @Override
    public List<Symbol> getLocalVariables(String methodSignature) {
        return new LinkedList<>(methods.get(methodSignature).getLocalVars().values());
    }
}
