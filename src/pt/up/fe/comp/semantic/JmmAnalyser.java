package pt.up.fe.comp.semantic;

import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class JmmAnalyser extends AJmmVisitor<Object, Type> implements JmmAnalysis {

    private final List<Report> reports = new LinkedList<>();
    public JmmAnalyser() {
        addVisit("Start", this::visitStart);
        addVisit("MainMethodDeclaration", this::visitMainMethod);
        addVisit("MethodDeclaration", this::visitMethod);
        addVisit("MethodBody", this::visitMethodBody);
        addVisit("AsmOp", this::visitAssignmentOperation);

        addVisit("IntegerLiteral", this::visitIntegerLiteral);
        addVisit("True", this::visitBool);
        addVisit("False", this::visitBool);
    }

    public Type visitStart(JmmNode startNode, Object dummy) {
        for (var node: startNode.getChildren()) {
            visit(node);
        }
        return null;
    }

    public Type visitMainMethod(JmmNode method, Object dummy) {
        return null;
    }

    public Type visitMethod(JmmNode method, Object dummy) {
        return null;
    }

    public Type visitMethodBody(JmmNode body, Object dummy) {
        return null;
    }

    public Type visitAssignmentOperation(JmmNode asm, Object dummy) {
        String varName = asm.getJmmChild(0).get("image");

        return null;
    }

    public Type visitBool(JmmNode node, Object dummy) {
        return new Type(
            "BOOL",
             false
        );
    }

    public Type visitIntegerLiteral(JmmNode node, Object dummy) {
        return new Type(
            "INT",
            false
        );
    }

    @Override
    public JmmSemanticsResult semanticAnalysis(JmmParserResult parserResult) {
        SymbolTable symbolTable = new SymbolTableFactory().generateTable(parserResult.getRootNode(), reports);

        return new JmmSemanticsResult(parserResult, symbolTable, reports);
    }
}
