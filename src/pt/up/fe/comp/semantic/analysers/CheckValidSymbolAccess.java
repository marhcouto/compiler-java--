package pt.up.fe.comp.semantic.analysers;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp.semantic.Constants;
import pt.up.fe.comp.semantic.symbol_table.SymbolTable;

import java.util.List;

public class CheckValidSymbolAccess extends TopDownScopeAnalyser {
    public CheckValidSymbolAccess(SymbolTable symbolTable, List<Report> reports) {
        super(symbolTable, reports);
        addVisit("VarName", this::visitVarName);
        addVisit("Variable", this::visitVariable);
        addVisit("ArrAccess", this::visitArrayAccess);
    }

    private Boolean visitArrayAccess(JmmNode node, String scope) {
        //Node is at the table
        if (visit(node.getJmmChild(0), scope)) {
            Symbol symbol = symbolTable.getSymbol(scope, node.getJmmChild(0).get("image"));
            if (symbol != null && !symbol.getType().isArray() && !symbol.getType().getName().equals(Constants.ANY_TYPE)) {
                reports.add(new Report(
                    ReportType.ERROR,
                    Stage.SEMANTIC,
                    Integer.parseInt(node.get("line")),
                    Integer.parseInt(node.get("column")),
                    String.format("Tried to access '%s' as an array but it's type is '%s'", node.getJmmChild(0).get("image"), symbol.getType().toString())
                ));
            }
        }
        return false;
    }

    private Boolean visitVariable(JmmNode node, String scope) {
        String varType = node.getJmmChild(0).get("image");
        if (Constants.primitives.contains(varType) || varType.equals(symbolTable.getClassName()) || symbolTable.hasSymbolInImportPath(varType)) {
            return true;
        }
        reports.add(new Report(
                ReportType.ERROR,
                Stage.SEMANTIC,
                Integer.parseInt(node.get("line")),
                Integer.parseInt(node.get("column")),
                String.format("Type '%s' not defined", varType)
        ));
        return false;
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
}
