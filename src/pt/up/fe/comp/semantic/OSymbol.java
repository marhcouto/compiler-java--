package pt.up.fe.comp.semantic;

import pt.up.fe.comp.jmm.analysis.table.Type;

enum ORIGIN { PARAMS, LOCAL };

public class OSymbol extends pt.up.fe.comp.jmm.analysis.table.Symbol {
    private ORIGIN origin;

    public OSymbol(Type type, String name) {
        super(type, name);
    }

    public ORIGIN getOrigin() {
        return origin;
    }
}
