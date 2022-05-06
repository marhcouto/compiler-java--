package pt.up.fe.comp.semantic.symbol_table.visitors;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp.semantic.models.Method;
import pt.up.fe.comp.semantic.models.OSymbol;
import pt.up.fe.comp.semantic.models.Origin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MethodDataCollector extends AJmmVisitor<Object, Symbol> {

    private List<Report> reportList;

    private final Map<String, Method> methods = new HashMap<>();

    public MethodDataCollector(List<Report> reportList) {
        this.reportList = reportList;

        addVisit("Start", this::visitStart);
        addVisit("ClassDeclaration", this::visitClassDecl);
        addVisit("MainMethodDeclaration", this::visitMainMethodDecl);
        addVisit("MethodDeclaration", this::visitMethodDecl);
        addVisit("Variable", this::visitVarDecl);
        setDefaultVisit((node, dummy) -> null);
    }

    public Symbol visitStart(JmmNode node, Object dummy) {
        for (JmmNode child: node.getChildren()) {
            visit(child);
        }
        return null;
    }

    public Symbol visitClassDecl(JmmNode node, Object dummy) {
        for (var child: node.getChildren()) {
            visit(child);
        }
        return null;
    }

    private Symbol visitMainMethodDecl(JmmNode node, Object dummy) {
        if (methods.containsKey("main")) {
            reportList.add(new Report(
                ReportType.ERROR,
                Stage.SEMANTIC,
                Integer.parseInt(node.get("line")),
                Integer.parseInt(node.get("column")),
                "Found method previously declared: " + "main"
            ));
            return null;
        }
        List<JmmNode> methodChildren = node.getChildren();
        Method newMethod = new Method("main", new Type("void", false));

        // Argument
        newMethod.addVariable(new OSymbol(
            new Type("String", true),
            methodChildren.get(0).get("image"),
            Origin.PARAMS
        ));

        // Variable Declarations
        for (var childDecl: methodChildren.get(1).getChildren()) {
            Symbol visitResult = visit(childDecl);
            if (visitResult != null) {
                newMethod.addVariable(OSymbol.fromSymbol(visitResult, Origin.LOCAL));
            }
        }

        // Add to list of methods
        methods.put(newMethod.getName(), newMethod);
        return null;
    }

    private Symbol visitMethodDecl(JmmNode node, Object dummy) {
        String methodName = node.getChildren().get(1).get("image");
        if (methods.containsKey(methodName)) {
            reportList.add(new Report(
                    ReportType.ERROR,
                    Stage.SEMANTIC,
                    Integer.parseInt(node.get("line")),
                    Integer.parseInt(node.get("column")),
                    "Found method previously declared: " + methodName
            ));
            return null;
        }

        boolean isArray = false;
        if (node.getJmmChild(0).getAttributes().contains("arr")) {
            isArray = true;
        }

        // Method return type
        Type type = new Type(node.getChildren().get(0).get("image"), isArray);
        Method newMethod = new Method(methodName, type);

        // Arguments
        for (var childDecl: node.getChildren().get(2).getChildren()) {
            Symbol visitResult = visit(childDecl);
            if (visitResult != null) {
                newMethod.addVariable(OSymbol.fromSymbol(visitResult, Origin.PARAMS));
            }
        }

        // Variable Declarations
        for (var childDecl: node.getChildren().get(3).getChildren()) {
            Symbol visitResult = visit(childDecl);
            if (visitResult != null) {
                newMethod.addVariable(OSymbol.fromSymbol(visitResult, Origin.LOCAL));
            }
        }

        methods.put(newMethod.getName(), newMethod);
        return null;
    }

    private Symbol visitVarDecl(JmmNode node, Object dummy) {
        List<JmmNode> varDecl = node.getChildren();
        boolean isArray = false;
        if (node.getJmmChild(0).getAttributes().contains("arr")) {
            isArray = true;
        }
        return new OSymbol(new Type(node.getChildren().get(0).get("image"), isArray),
                node.getChildren().get(1).get("image"),
                Origin.LOCAL
        );
    }

    public Map<String, Method> getMethods() {
        return methods;
    }
}
