import org.junit.Test;
import pt.up.fe.comp.JasminGenerator;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jasmin.JasminEmitter;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.specs.util.SpecsIo;

import java.util.Collections;

public class JasminTest {

    @Test
    public void testFac() {

        OllirResult ollirResult = new OllirResult(SpecsIo.getResource("fixtures/public/ollir/Fac.ollir"), Collections.emptyMap());
        var jasminGen = new JasminEmitter();
        var jasminResult = jasminGen.toJasmin(ollirResult);


    }

    @Test
    public void testMyClass1() {
        //var ollirResult = TestUtils.optimize(SpecsIo.getResource("fixtures/public/HelloWorld.jmm"));
        //TestUtils.noErrors(ollirResult.getReports());

        OllirResult ollirResult = new OllirResult(SpecsIo.getResource("fixtures/public/ollir/myclass1.ollir"), Collections.emptyMap());
        var jasminGen = new JasminEmitter();
        var jasminResult = jasminGen.toJasmin(ollirResult);

    }

    @Test
    public void testMyClass2() {
        //var ollirResult = TestUtils.optimize(SpecsIo.getResource("fixtures/public/HelloWorld.jmm"));
        //TestUtils.noErrors(ollirResult.getReports());

        OllirResult ollirResult = new OllirResult(SpecsIo.getResource("fixtures/public/ollir/myclass2.ollir"), Collections.emptyMap());
        var jasminGen = new JasminEmitter();
        var jasminResult = jasminGen.toJasmin(ollirResult);

    }

    @Test
    public void testMyClass3() {
        //var ollirResult = TestUtils.optimize(SpecsIo.getResource("fixtures/public/HelloWorld.jmm"));
        //TestUtils.noErrors(ollirResult.getReports());

        OllirResult ollirResult = new OllirResult(SpecsIo.getResource("fixtures/public/ollir/myclass3.ollir"), Collections.emptyMap());
        var jasminGen = new JasminEmitter();
        var jasminResult = jasminGen.toJasmin(ollirResult);

    }

    @Test
    public void testMyClass4() {
        //var ollirResult = TestUtils.optimize(SpecsIo.getResource("fixtures/public/HelloWorld.jmm"));
        //TestUtils.noErrors(ollirResult.getReports());

        OllirResult ollirResult = new OllirResult(SpecsIo.getResource("fixtures/public/ollir/myclass4.ollir"), Collections.emptyMap());
        var jasminGen = new JasminEmitter();
        var jasminResult = jasminGen.toJasmin(ollirResult);

    }

    @Test
    public void test2() {
        OllirResult ollirResult = new OllirResult(SpecsIo.getResource("fixtures/public/ollir/teste.ollir"), Collections.emptyMap());
        var jasminGen = new JasminEmitter();
        var jasminResult = jasminGen.toJasmin(ollirResult);
    }

    @Test
    public void testArith() {
        OllirResult ollirResult = new OllirResult(SpecsIo.getResource("fixtures/public/cp2/OllirToJasminArithmetics.ollir"), Collections.emptyMap());
        var jasminGen = new JasminEmitter();
        var jasminResult = jasminGen.toJasmin(ollirResult);
    }

    @Test
    public void testBasic() {
        OllirResult ollirResult = new OllirResult(SpecsIo.getResource("fixtures/public/cp2/OllirToJasminBasic.ollir"), Collections.emptyMap());
        var jasminGen = new JasminEmitter();
        var jasminResult = jasminGen.toJasmin(ollirResult);
    }

    @Test
    public void testFields() {
        OllirResult ollirResult = new OllirResult(SpecsIo.getResource("fixtures/public/cp2/OllirToJasminFields.ollir"), Collections.emptyMap());
        var jasminGen = new JasminEmitter();
        var jasminResult = jasminGen.toJasmin(ollirResult);
    }

    @Test
    public void testInvoke() {
        OllirResult ollirResult = new OllirResult(SpecsIo.getResource("fixtures/public/cp2/OllirToJasminInvoke.ollir"), Collections.emptyMap());
        var jasminGen = new JasminEmitter();
        var jasminResult = jasminGen.toJasmin(ollirResult);
    }


    @Test
    public void test() {
        var jasminResult = TestUtils.backend(SpecsIo.getResource("fixtures/public/testing/Example.jmm"));
        TestUtils.noErrors(jasminResult);

    }

}
