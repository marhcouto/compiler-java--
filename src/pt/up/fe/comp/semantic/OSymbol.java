package pt.up.fe.comp.semantic;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.semantic.visitors.Origin;

public class OSymbol extends pt.up.fe.comp.jmm.analysis.table.Symbol {
    private Origin origin;

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
