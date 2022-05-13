package pt.up.fe.comp.ollir;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
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
        switch (typeStr) {
            case "int":
                ollirType += "i32";
                break;
            case "void", ".Any":
                ollirType += "V";
                break;
            default:
                ollirType += typeStr;
                break;
        };
        return ollirType;
    }

    public static boolean isStatic(JmmNode annotatedNode) {
        return annotatedNode.getKind().equals("VarName") && annotatedNode.get("image").equals(annotatedNode.get("type"));
    }

    public static boolean needToPlaceVariable (JmmNode node) {
        //Can inline if function child is a variable name
        return !node.getJmmParent().getKind().equals("MethodBody");
    }

    public static int findParamIdx(JmmNode methodArgsList, String paramName) {
        List<JmmNode> params = methodArgsList.getChildren();
        for (int i = 0; i < params.size(); i++) {
            JmmNode param = params.get(i);
            if (param.getJmmChild(1).get("image").equals(paramName)) {
                return i + 1;
            }
        }
        throw new RuntimeException("Should not get here with invalid paramName");
    }

    public static boolean isVariableOrLiteral(String id){
        return id.matches("(((_|[a-zA-z])(_|\\d|[a-zA-Z])*)\\.(([a-zA-z])(\\d|[a-zA-Z])*))|\\d|true|false|this");
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
