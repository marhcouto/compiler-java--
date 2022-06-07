package pt.up.fe.comp.extra;

import org.junit.Test;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.specs.util.SpecsIo;

import static org.junit.Assert.assertEquals;

public class Semantic {
    static JasminResult getJasminResult(String filename) {
        return TestUtils.backend(SpecsIo.getResource("fixtures/public/cpf/extra/" + filename));
    }

    static JmmSemanticsResult getSemanticsResult(String filename) {
        return TestUtils.analyse(SpecsIo.getResource("fixtures/public/cpf/extra/" + filename));
    }

    static JmmSemanticsResult test(String filename, boolean fail) {
        var semantics = getSemanticsResult(filename);
        if (fail) {
            TestUtils.mustFail(semantics.getReports());
        } else {
            TestUtils.noErrors(semantics.getReports());
        }
        return semantics;
    }

    @Test
    public void testAssignedBeforeUsed() {
        JmmSemanticsResult result = TestUtils.analyse(SpecsIo.getResource("fixtures/public/fail/semantic/varNotInit.jmm"));
        assertEquals(1, result.getReports().size());
    }

    @Test
    public void aDoesNotExtendB() {
        test("cp2_error.jmm", true);
    }
}
