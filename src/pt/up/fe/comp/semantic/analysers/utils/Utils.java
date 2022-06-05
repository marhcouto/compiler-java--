package pt.up.fe.comp.semantic.analysers.utils;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.List;

public final class Utils {
    public static String buildTypeAnnotatedError(JmmNode node, String expectedType, boolean expectedArray) {
        String styledNodeType = node.get("type");
        String styledExpectedType = expectedType;
        if (node.getAttributes().contains("arr")) {
            styledNodeType += "[]";
        }
        if (expectedArray) {
            styledExpectedType += "[]";
        }
        return String.format("Expected type %s but found %s", styledExpectedType, styledNodeType);
    }

    private static String prettyTypeToString(Type type) {
        if (type.isArray()) {
            return String.format("%s[]", type.getName());
        }
        return type.getName();
    }

    public static String prettyNodeTypeToString(JmmNode node) {
        if (node.getAttributes().contains("arr")) {
            return String.format("%s[]", node.get("type"));
        }
        return node.get("type");
    }

    public static String buildParamTypes(List<Symbol> args) {
        StringBuilder argStr = new StringBuilder("(");
        if (args.size() != 0) {
            argStr.append(prettyTypeToString(args.get(0).getType()));
        }
        for (int i = 1; i < args.size(); i++) {
            argStr.append(String.format(", %s", prettyTypeToString(args.get(i).getType())));
        }
        argStr.append(")");
        return argStr.toString();
    }

    public static String buildArgTypes(List<JmmNode> args) {
        StringBuilder argStr = new StringBuilder("(");
        if (args.size() != 0) {
            argStr.append(prettyNodeTypeToString(args.get(0)));
        }
        for (int i = 1; i < args.size(); i++) {
            argStr.append(String.format(", %s", prettyNodeTypeToString(args.get(0))));
        }
        argStr.append(")");
        return argStr.toString();
    }
}
