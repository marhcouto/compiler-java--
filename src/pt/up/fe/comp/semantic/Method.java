package pt.up.fe.comp.semantic;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.HashMap;
import java.util.Map;

public class Method {
    private String name;
    private Type returnType;
    private Map<String, OSymbol> variables = new HashMap<>();

    public String getName() {
        return name;
    }

    public Type getReturnType() {
        return returnType;
    }

    public Map<String, Symbol> getParameters() {
        Map<String, Symbol> parameters = new HashMap<>();
        for (OSymbol variable: variables.values()) {
            if (variable.getOrigin() == ORIGIN.PARAMS) {
                parameters.put(variable.getName(), variable);
            }
        }
        return parameters;
    }

    public Map<String, Symbol> getLocalVars() {
        Map<String, Symbol> parameters = new HashMap<>();
        for (OSymbol variable: variables.values()) {
            if (variable.getOrigin() == ORIGIN.LOCAL) {
                parameters.put(variable.getName(), variable);
            }
        }
        return parameters;
    }

    public Map<String, OSymbol> getVariables() {
        return variables;
    }
}
