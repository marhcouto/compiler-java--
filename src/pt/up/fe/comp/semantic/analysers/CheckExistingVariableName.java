package pt.up.fe.comp.semantic.analysers;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp.semantic.symbol_table.SymbolTable;

import java.util.List;

public class CheckExistingVariableName extends TopDownScopeAnalyser {
    public CheckExistingVariableName(SymbolTable symbolTable, List<Report> reports) {
        super(symbolTable, reports);
        addVisit("VarName", this::visitVarName);
    }

    private Boolean visitVarName(JmmNode node, String scope) {
        if (!symbolTable.hasSymbol(scope, node.get("image"))) {
            reports.add(new Report(
                ReportType.ERROR,
                Stage.SEMANTIC,
                Integer.parseInt(node.get("line")),
                Integer.parseInt(node.get("column")),
                "Variable " + node.get("image") + " wasn't found in this scope"
            ));
            return false;
        }
        return true;
    }

    @Override
    public List<Report> getReports() {
        return reports;
    }
}
