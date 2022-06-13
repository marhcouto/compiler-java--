package pt.up.fe.comp.semantic.models;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Method {
    private String name;
    private Type returnType;
    private Map<String, ExtendedSymbol> variables;

    public Method() {
        this(null, null, new HashMap<>());
    }

    public Method(String name, Type returnType) {
        this(name, returnType, new HashMap<>());
    }

    public Method(String name, Type returnType, Map<String, ExtendedSymbol> variables) {
        this.name = name;
        this.returnType = returnType;
        this.variables = variables;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Type getReturnType() {
        return returnType;
    }

    public List<ExtendedSymbol> getParameters() {
        List<ExtendedSymbol> parameters = new ArrayList<>();
        for (ExtendedSymbol variable: variables.values()) {
            if (variable.getOrigin() == Origin.PARAMS) {
                parameters.add(variable);
            }
        }
        parameters.sort((elem1, elem2) -> {
            if (elem1.getIndex() < elem2.getIndex()) {
                return -1;
            }
            if (elem1.getIndex() == elem2.getIndex()) {
                return 0;
            }
            return 1;
        });
        return parameters;
    }

    public Map<String, Symbol> getLocalVars() {
        Map<String, Symbol> parameters = new HashMap<>();
        for (ExtendedSymbol variable: variables.values()) {
            if (variable.getOrigin() == Origin.LOCAL) {
                parameters.put(variable.getName(), variable);
            }
        }
        return parameters;
    }

    public Map<String, ExtendedSymbol> getVariables() {
        return variables;
    }

    public void addVariable(ExtendedSymbol variable) {
        this.variables.put(variable.getName(), variable);
    }
}
