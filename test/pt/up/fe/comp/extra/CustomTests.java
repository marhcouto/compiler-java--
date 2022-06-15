package pt.up.fe.comp.extra;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Assert;
import org.junit.Test;

import pt.up.fe.comp.CpUtils;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.specs.util.SpecsIo;

public class CustomTests {


    static JasminResult getJasminResult(String filename) {
        return TestUtils.backend(SpecsIo.getResource("fixtures/public/cpf/extra/" + filename));
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

//    @Test
//    public void TicTacToe() {
//        var result = getOllirResult("TicTacToe.jmm");
//        System.out.println(result.getOllirCode());
//    }
}
