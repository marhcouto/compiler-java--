package pt.up.fe.comp.extra;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Assert;
import org.junit.Test;

import pt.up.fe.comp.CpUtils;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jasmin.JasminEmitter;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.specs.util.SpecsIo;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CustomTests {


    static JasminResult getJasminResult(String filename) {
        return TestUtils.backend(SpecsIo.getResource("fixtures/public/cpf/extra/" + filename));
    }

    static JasminResult getJasminResultOpt(String filename) {
        Map<String, String> config = new HashMap<>();
        config.put("optimize", "true");
        return TestUtils.backend(SpecsIo.getResource("fixtures/public/cpf/5_optimizations/" + filename), config);
    }

    @Test
    public void testAll() {
        String expected = "Result: 3\nResult: 4\nResult: 2\nResult: 3";
        CpUtils.runJasmin(getJasminResult("AllaroundTest.jmm"), expected);
    }

    @Test
    public void testLimitStack() {
        String expected = "Result: 2\nResult: 3\nResult: 4";
        CpUtils.runJasmin(getJasminResult("LimitStack.jmm"), expected);
    }

    @Test
    public void bigifs(){
        String expected = "Result: 1\nResult: 1\nResult: 1";
        CpUtils.runJasmin(getJasminResult("BigIf.jmm"), expected);
    }

    @Test
    public void foldingAdd(){
        String expected = "Result: 4";
        JasminResult jasmin = getJasminResult("FoldingAdd.jmm");
        CpUtils.runJasmin(jasmin, expected);
        Assert.assertEquals(false, jasmin.getJasminCode().contains("iadd"));

    }

    @Test
    public void foldingMul(){
        String expected = "Result: 3";
        JasminResult jasmin = getJasminResult("FoldingMul.jmm");
        CpUtils.runJasmin(jasmin, expected);
        Assert.assertEquals(false, jasmin.getJasminCode().contains("imul"));

    }

    @Test
    public void foldingSub(){
        String expected = "Result: 2";
        JasminResult jasmin = getJasminResult("FoldingSub.jmm");
        CpUtils.runJasmin(jasmin, expected);
        Assert.assertEquals(false, jasmin.getJasminCode().contains("isub"));

    }

    @Test
    public void foldingDiv(){
        String expected = "Result: 2";
        JasminResult jasmin = getJasminResult("FoldingDiv.jmm");
        CpUtils.runJasmin(jasmin, expected);
        Assert.assertEquals(false, jasmin.getJasminCode().contains("idiv"));

    }

    /*
    Shifts
     */
    @Test
    public void testShiftLeft(){
        String expected = "40";
        JasminResult jasminResult = getJasminResult("ShiftLeft.jmm");
        CpUtils.runJasmin(jasminResult, expected);
        CpUtils.matches(jasminResult, "ishl");
    }

    @Test
    public void testShiftRight(){
        String expected = "2";
        JasminResult jasminResult = getJasminResult("ShiftRight.jmm");
        CpUtils.runJasmin(jasminResult, expected);
        CpUtils.matches(jasminResult, "ishr");
    }

    @Test
    public void testConstantFoldingAnd(){
        String expected = "0";
        JasminResult jasminResult = getJasminResult("ConstantFoldingAnd.jmm");
        CpUtils.runJasmin(jasminResult, expected);

    }

    @Test
    public void eliminationOfGotosOptimizationTest() {

        String filename = "while_template/WhileOpt.jmm";
        int expectedIf = 1;
        int expectedGoto = 0;

        JasminResult original = TestUtils.backend(SpecsIo.getResource("fixtures/public/cpf/5_optimizations/" + filename));
        JasminResult optimized = getJasminResultOpt(filename);

        CpUtils.assertNotEquals("Expected code to change with -o flag\n\nOriginal code:\n" + original.getJasminCode(),
                original.getJasminCode(), optimized.getJasminCode(),
                optimized);

        var ifOccurOpt = CpUtils.countOccurences(optimized, "if");
        var gotoOccurOpt = CpUtils.countOccurences(optimized, "goto");

        CpUtils.assertEquals("Expected exactly " + expectedIf + " if instruction", expectedIf, ifOccurOpt, optimized);
        CpUtils.assertEquals("Expected exactly " + expectedGoto + " goto instructions", expectedGoto, gotoOccurOpt,
                optimized);
    }

}
