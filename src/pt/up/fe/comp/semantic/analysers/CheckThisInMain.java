package pt.up.fe.comp.semantic.analysers;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PostorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.List;

public class CheckThisInMain extends PostorderJmmVisitor<Object, Object> {
    List<Report> reports;

    public CheckThisInMain(List<Report> reports) {
        this.reports = reports;
        addVisit("This", this::visitThis);
    }

    private Object visitThis(JmmNode node, Object dummy) {
        reports.add(new Report(
            ReportType.ERROR,
            Stage.SEMANTIC,
            Integer.parseInt(node.get("line")),
            Integer.parseInt(node.get("column")),
            String.format("Used this in main")
        ));
        return null;
    }
}
