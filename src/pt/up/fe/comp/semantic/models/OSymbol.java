package pt.up.fe.comp.semantic.models;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;

public class OSymbol extends pt.up.fe.comp.jmm.analysis.table.Symbol {
    private final Origin origin;

    public static OSymbol fromSymbol(Symbol symbol, Origin origin) {
        return new OSymbol(symbol.getType(), symbol.getName(), origin);
    }

    public OSymbol(Type type, String name, Origin origin) {
        super(type, name);
        this.origin = origin;
    }

    public Origin getOrigin() {
        return origin;
    }
}
