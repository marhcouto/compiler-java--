package pt.up.fe.comp.ollir;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.semantic.symbol_table.SymbolTable;

import java.util.List;

public class OllirUtils {
    private OllirUtils() {}

    public static String toOllir(Symbol symbol) {
        return String.format("%s.%s", symbol.getName(), toOllir(symbol.getType()));
    }

    public static String toOllir(Type type) {
        return toOllir(type.getName(), type.isArray());
    }

    public static String toOllir(JmmNode annotatedNode) {
        return toOllir(annotatedNode.get("type"), annotatedNode.getAttributes().contains("arr"));
    }

    public static String toOllir(String typeStr, boolean isArray) {
        String ollirType = "";
        if (isArray) {
            ollirType += "array.";
        }
        ollirType += switch (typeStr) {
            case "int" -> "i32";
            case "void", ".Any" -> "V";
            default -> typeStr;
        };
        return ollirType;
    }

    public static String defaultConstructor(String className) {
        return String.format(".construct %s().V {\n" +
                "invokespecial(this, \"<init>\").V;\n" +
                "}\n", className);
    }

    public static boolean isStatic(JmmNode annotatedNode) {
        return annotatedNode.getKind().equals("VarName") && annotatedNode.get("image").equals(annotatedNode.get("type"));
    }

    public static boolean canInline (JmmNode node) {
        //Can inline if function is called over a variable/class
        return node.getKind().equals("VarName");
    }

    public static String generateFields(List<Symbol> fields) {
        StringBuilder fieldCode = new StringBuilder();
        for (var field: fields) {
            fieldCode.append(String.format(".field private %s;\n", toOllir(field)));
        }
        fieldCode.append("\n");
        return fieldCode.toString();
    }
}
