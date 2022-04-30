package pt.up.fe.comp.semantic;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.HashMap;
import java.util.Map;

public class Method {
    private String name;
    private Type returnType;
    private Map<String, Symbol> parentTable;
    private Map<String, OSymbol> variables;

    public Method() {
        this(null, null, new HashMap<>(), new HashMap<>());
    }

    public Method(String name, Type returnType, Map<String, Symbol> parentTable) {
        this(name, returnType, parentTable, new HashMap<>());
    }

    public Method(String name, Type returnType, Map<String, Symbol> parentTable, Map<String, OSymbol> variables) {
        this.name = name;
        this.returnType = returnType;
        this.parentTable = parentTable;
        this.variables = variables;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setReturnType(Type returnType) {
        this.returnType = returnType;
    }

    public void setParentTable(Map<String, Symbol> parentTable) {
        this.parentTable = parentTable;
    }

    public void setVariables(Map<String, OSymbol> variables) {
        this.variables = variables;
    }

    public String getName() {
        return name;
    }

    public Type getReturnType() {
        return returnType;
    }

    public Map<String, Symbol> getParameters() {
        Map<String, Symbol> parameters = new HashMap<>();
        for (OSymbol variable: variables.values()) {
            if (variable.getOrigin() == Origin.PARAMS) {
                parameters.put(variable.getName(), variable);
            }
        }
        return parameters;
    }

    public Map<String, Symbol> getLocalVars() {
        Map<String, Symbol> parameters = new HashMap<>();
        for (OSymbol variable: variables.values()) {
            if (variable.getOrigin() == Origin.LOCAL) {
                parameters.put(variable.getName(), variable);
            }
        }
        return parameters;
    }

    public Map<String, OSymbol> getVariables() {
        return variables;
    }

    public void addVariable(OSymbol variable) {
        this.variables.put(variable.getName(), variable);
    }
}
