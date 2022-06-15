package pt.up.fe.comp.extra;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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


}
