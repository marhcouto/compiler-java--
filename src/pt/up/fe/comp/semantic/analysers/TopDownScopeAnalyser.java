package pt.up.fe.comp.semantic.analysers;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.semantic.symbol_table.SymbolTable;

import java.util.List;

public abstract class TopDownScopeAnalyser extends AJmmVisitor<String, Boolean> implements SemanticRuleAnalyser {
    protected SymbolTable symbolTable;
    protected List<Report> reports;

    public TopDownScopeAnalyser(SymbolTable symbolTable, List<Report> reports) {
        this.symbolTable = symbolTable;
        this.reports = reports;
        addVisit("Start", this::visitStart);
        addVisit("ClassDeclaration", this::visitClassDeclaration);
        addVisit("MainMethodDeclaration", this::visitMainMethodDecl);
        addVisit("MethodDeclaration", this::visitMethodDecl);
        addVisit("Variable", (node, method) -> false);
        setDefaultVisit(this::defaultVisitor);
    }

    private Boolean defaultVisitor(JmmNode node, String scope) {
        if (scope == null) {
            return false;
        }
        for (var child: node.getChildren()) {
            visit(child, scope);
        }
        return true;
    }

    private Boolean visitStart(JmmNode node, String dummy) {
        for (var child: node.getChildren()) {
            visit(child);
        }
        return true;
    }

    private Boolean visitClassDeclaration(JmmNode node, String dummy) {
        for (var child: node.getChildren()) {
            visit(child);
        }
        return true;
    }

    private Boolean visitMainMethodDecl(JmmNode node, String dummy) {
        visit(node.getJmmChild(1), "main");
        return true;
    }

    private Boolean visitMethodDecl(JmmNode node, String dummy) {
        visit(node.getJmmChild(3), node.getJmmChild(1).get("image"));
        return true;
    }

    @Override
    public List<Report> getReports() {
        return reports;
    }
}
