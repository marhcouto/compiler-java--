package pt.up.fe.comp.extra;

import org.junit.Test;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.specs.util.SpecsIo;

import static org.junit.Assert.assertEquals;

public class SemanticAnalysisTest {

    private static void mustFail(String javaCode) {
        JmmParserResult parserResult = TestUtils.parse(javaCode);
        JmmSemanticsResult semanticsResult = TestUtils.analyse(parserResult);
        TestUtils.mustFail(semanticsResult.getReports());
    }

    private static void noErrors(String javaCode) {
        JmmParserResult parserResult = TestUtils.parse(javaCode);
        JmmSemanticsResult semanticsResult = TestUtils.analyse(parserResult);
        TestUtils.noErrors(semanticsResult.getReports());
    }

    @Test
    public void arrIndexNotInt() {
        mustFail(SpecsIo.getResource("fixtures/public/fail/semantic/arr_index_not_int.jmm"));
    }

    @Test
    public void arrSizeNotInt() {
        mustFail(SpecsIo.getResource("fixtures/public/fail/semantic/arr_size_not_int.jmm"));
    }

    @Test
    public void badArguments() {
        mustFail(SpecsIo.getResource("fixtures/public/fail/semantic/badArguments.jmm"));
    }

    @Test
    public void binOpIncomp() {
        mustFail(SpecsIo.getResource("fixtures/public/fail/semantic/binop_incomp.jmm"));
    }

    @Test
    public void funcNotFound() {
        mustFail(SpecsIo.getResource("fixtures/public/fail/semantic/funcNotFound.jmm"));
    }

    @Test
    public void simpleLength() {
        mustFail(SpecsIo.getResource("fixtures/public/fail/semantic/simple_length.jmm"));
    }

    @Test
    public void varExpIncomp() {
        mustFail(SpecsIo.getResource("fixtures/public/fail/semantic/var_exp_incomp.jmm"));
    }

    @Test
    public void varLitIncomp() {
        mustFail(SpecsIo.getResource("fixtures/public/fail/semantic/var_lit_incomp.jmm"));
    }

    @Test
    public void varUndef() {
        mustFail(SpecsIo.getResource("fixtures/public/fail/semantic/var_undef.jmm"));
    }

    @Test
    public void varNotInit() {
        // Should only raise WARNING
        JmmParserResult parserResult = TestUtils.parse(SpecsIo.getResource("fixtures/public/fail/semantic/varNotInit.jmm"));
        JmmSemanticsResult semanticsResult = TestUtils.analyse(parserResult);
        assertEquals(1, semanticsResult.getReports().size());
        assertEquals(ReportType.WARNING, semanticsResult.getReports().get(0).getType());
    }

    @Test
    public void missType() {
        mustFail(SpecsIo.getResource("fixtures/public/fail/semantic/extra/miss_type.jmm"));
    }

    @Test
    public void helloWorld() {
        noErrors(SpecsIo.getResource("fixtures/public/HelloWorld.jmm"));
    }

    @Test
    public void findMaximum() {
        noErrors(SpecsIo.getResource("fixtures/public/FindMaximum.jmm"));
    }

    @Test
    public void lazysort() {
        noErrors(SpecsIo.getResource("fixtures/public/Lazysort.jmm"));
    }

    @Test
    public void life() {
        noErrors(SpecsIo.getResource("fixtures/public/Life.jmm"));
    }

    @Test
    public void quickSort() {
        noErrors(SpecsIo.getResource("fixtures/public/QuickSort.jmm"));
    }

    @Test
    public void simple() {
        noErrors(SpecsIo.getResource("fixtures/public/Simple.jmm"));
    }

    @Test
    public void ticTacToe() {
        noErrors(SpecsIo.getResource("fixtures/public/TicTacToe.jmm"));
    }

    @Test
    public void whileAndIf() {
        noErrors(SpecsIo.getResource("fixtures/public/WhileAndIf.jmm"));
    }

    @Test
    public void aDoesNotExtendB() {
        var semantics = TestUtils.analyse(SpecsIo.getResource("fixtures/public/cpf/extra/cp2_error.jmm"));
        TestUtils.mustFail(semantics.getReports());
    }
}
