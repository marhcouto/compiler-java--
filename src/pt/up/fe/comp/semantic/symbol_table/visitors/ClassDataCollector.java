package pt.up.fe.comp.semantic.symbol_table.visitors;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassDataCollector extends AJmmVisitor<Object, Boolean> {
    private String thisSuper;
    private String className;
    private final Map<String, Symbol> fields = new HashMap<>();
    public ClassDataCollector() {
        addVisit("Start", this::visitStart);
        addVisit("ClassDeclaration", this::visitClassNode);
        addVisit("ClassName", this::collectClassName);
        addVisit("ClassParent", this::collectParent);
        addVisit("Variable", this::collectAttrData);
        setDefaultVisit((node, dummy) -> true);
    }

    private Boolean visitStart(JmmNode start, Object dummy) {
        for (var child: start.getChildren()) {
            visit(child);
        }
        return true;
    }

    private Boolean visitClassNode(JmmNode classNode, Object dummy) {
        for (var child: classNode.getChildren()) {
            visit(child);
        }
        return true;
    }

    private Boolean collectClassName(JmmNode classNameNode, Object dummy) {
        this.className = classNameNode.get("image");
        return true;
    }

    private Boolean collectParent(JmmNode parentNameNode, Object dummy) {
        this.thisSuper = parentNameNode.get("image");
        return true;
    }

    private Boolean collectAttrData(JmmNode declNode, Object dummy) {
        boolean isArray = false;
        if (declNode.getJmmChild(0).getAttributes().contains("arr")) {
            isArray = true;
        }
        Symbol newSymbol = new Symbol(new Type(
            declNode.getJmmChild(0).get("image"),
            isArray
        ), declNode.getJmmChild(1).get("image"));
        fields.put(newSymbol.getName(), newSymbol);
        return true;
    }

    public String getThisSuper() {
        return thisSuper;
    }

    public String getClassName() {
        return className;
    }
    public Map<String, Symbol> getFields() {
        return fields;
    }
}