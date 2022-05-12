package pt.up.fe.comp.semantic.symbol_table;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.semantic.models.Method;
import pt.up.fe.comp.semantic.models.ExtendedSymbol;
import pt.up.fe.comp.semantic.models.Origin;
import pt.up.fe.comp.semantic.symbol_table.visitors.ClassDataCollector;
import pt.up.fe.comp.semantic.symbol_table.visitors.ImportCollector;
import pt.up.fe.comp.semantic.symbol_table.visitors.IndexAdd;
import pt.up.fe.comp.semantic.symbol_table.visitors.MethodDataCollector;

import java.util.*;

public class SymbolTableFactory {

    private final ImportCollector importVisitor = new ImportCollector();
    private final ClassDataCollector classVisitor = new ClassDataCollector();

    public SymbolTable generateTable(JmmNode node, List<Report> reports) {
        this.importVisitor.visit(node);
        this.classVisitor.visit(node);
        MethodDataCollector methodVisitor = new MethodDataCollector(reports);
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

    public static SymbolTable fromJmmSymbolTable(pt.up.fe.comp.jmm.analysis.table.SymbolTable symbolTable, JmmNode root) {
        SymbolTable newSymbolTable = new SymbolTable(
            symbolTable.getSuper(),
            symbolTable.getClassName(),
            symbolTable.getImports(),
            buildFieldsFromList(symbolTable.getFields()),
            buildMethodsFromList(symbolTable)
        );
        new IndexAdd(newSymbolTable).visit(root);
        return newSymbolTable;
    }

    private static Map<String, Symbol> buildFieldsFromList(List<Symbol> fields) {
        Map<String, Symbol> fieldMap = new HashMap<>();
        for (var field: fields) {
            fieldMap.put(field.getName(), field);
        }
        return fieldMap;
    }

    private static Map<String, Method> buildMethodsFromList(pt.up.fe.comp.jmm.analysis.table.SymbolTable symbolTable) {
        Map<String, Method> methodMap = new HashMap<>();
        for (var methodName: symbolTable.getMethods()) {
            Map<String, ExtendedSymbol> scopeSymbols = new HashMap<>();
            for (var param: symbolTable.getParameters(methodName)) {
                scopeSymbols.put(param.getName(), ExtendedSymbol.fromSymbol(param, Origin.PARAMS));
            }
            for (var localVar: symbolTable.getLocalVariables(methodName)) {
                scopeSymbols.put(localVar.getName(), ExtendedSymbol.fromSymbol(localVar, Origin.LOCAL));
            }
            Method curMethod = new Method(methodName, symbolTable.getReturnType(methodName), scopeSymbols);
            methodMap.put(curMethod.getName(), curMethod);
        }
        return methodMap;
    }
}
