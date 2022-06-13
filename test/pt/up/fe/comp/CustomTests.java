package pt.up.fe.comp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

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
}
