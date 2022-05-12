package pt.up.fe.comp.semantic.models;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;

public class ExtendedSymbol extends pt.up.fe.comp.jmm.analysis.table.Symbol {
    private final Origin origin;
    private int index;

    public static ExtendedSymbol fromSymbol(Symbol symbol, Origin origin) {
        return new ExtendedSymbol(symbol.getType(), symbol.getName(), -1, origin);
    }

    public static ExtendedSymbol fromSymbol(Symbol symbol, int index, Origin origin) {
        return new ExtendedSymbol(symbol.getType(), symbol.getName(), index, origin);
    }

    public ExtendedSymbol(Type type, String name, int index, Origin origin) {
        super(type, name);
        this.origin = origin;
        this.index = index;
    }

    public ExtendedSymbol(Type type, String name, Origin origin) {
        super(type, name);
        this.origin = origin;
        this.index = -1;
    }

    public Origin getOrigin() {
        return origin;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
