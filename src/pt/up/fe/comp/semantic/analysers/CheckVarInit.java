package pt.up.fe.comp.semantic.analysers;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp.semantic.symbol_table.SymbolTable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CheckVarInit extends AJmmVisitor<String, Object> {
    Set<String> initVars;
    String curScope;
    List<Report> reports;

    SymbolTable symbolTable;

    public CheckVarInit(SymbolTable symbolTable, List<Report> reports) {
        this.reports = reports;
        this.symbolTable = symbolTable;
        addVisit("AsmOp", this::visitAsmOp);
        addVisit("ArrAccess", this::visitArrAccess);
        addVisit("VarName", this::visitVarName);
        addVisit("Start", this::visitStart);
        addVisit("ClassDeclaration", this::visitClassDeclaration);
        addVisit("MainMethodDeclaration", this::visitMainMethodDecl);
        addVisit("MethodDeclaration", this::visitMethodDecl);
        addVisit("MethodArgsList", this::visitArgsList);
        addVisit("Variable", (node, dummy) -> null);
        setDefaultVisit(this::defaultVisitor);
    }

    private String defaultVisitor(JmmNode node, Object scope) {
        for (var child: node.getChildren()) {
            visit(child);
        }
        return null;
    }

    private String visitStart(JmmNode node, Object dummy) {
        for (var child: node.getChildren()) {
            visit(child);
        }
        return null;
    }

    private String visitClassDeclaration(JmmNode node, Object dummy) {
        for (var child: node.getChildren()) {
            visit(child);
        }
        return null;
    }

    private String visitMainMethodDecl(JmmNode node, Object dummy) {
        curScope = "main";
        initVars = new HashSet<>();
        //Gets the name of String array argument
        initVars.add(node.getJmmChild(0).get("image"));
        visit(node.getJmmChild(1));
        return null;
    }

    private String visitMethodDecl(JmmNode node, Object dummy) {
        curScope = node.getJmmChild(1).get("image");
        initVars = new HashSet<>();
        visit(node.getJmmChild(2));
        visit(node.getJmmChild(3));
        return null;
    }

    private String visitAsmOp(JmmNode node, Object dummy) {
        JmmNode dest = node.getJmmChild(0);
        String destName;
        if (dest.getKind().equals("ArrAccess")) {
            destName = (String) visit(dest);
        } else {
            destName = dest.get("image");
        }
        visit(node.getJmmChild(1));
        initVars.add(destName);
        return null;
    }

    private String visitArrAccess(JmmNode node, Object dummy) {
        return node.getJmmChild(0).get("image");
    }

    private String visitVarName(JmmNode node, Object dummy) {
        String varName = node.get("image");
        if (symbolTable.hasSymbolInImportPath(varName) || symbolTable.getMethodScope(varName) != null) {
            return null;
        }
        if (!initVars.contains(varName)) {
            reports.add(new Report(
                    ReportType.WARNING,
                    Stage.SEMANTIC,
                    Integer.parseInt(node.get("line")),
                    Integer.parseInt(node.get("column")),
                    String.format("Variable '%s' was not initialized in scope '%s'", node.get("image"), curScope)
            ));
        }
        return null;
    }

    private String visitArgsList(JmmNode node, Object dummy) {
        for (var child: node.getChildren()) {
            //Initializes all arguments
            initVars.add(child.getJmmChild(1).get("image"));
        }
        return null;
    }
}
