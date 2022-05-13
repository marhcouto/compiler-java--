
/**
 * Copyright 2021 SPeCS.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License. under the License.
 */

import org.junit.Test;
import pt.up.fe.comp.JasminGenerator;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.specs.util.SpecsIo;

import java.util.Collections;

public class OptimizeTest {

    @Test
    public void testHelloWorld() {
        //var ollirResult = TestUtils.optimize(SpecsIo.getResource("fixtures/public/HelloWorld.jmm"));
        //TestUtils.noErrors(ollirResult.getReports());

        OllirResult ollirResult = new OllirResult(SpecsIo.getResource("fixtures/public/ollir/Fac.ollir"), Collections.emptyMap());
        var jasminGen = new JasminGenerator();
        var jasminResult = jasminGen.toJasmin(ollirResult);


    }

    @Test
    public void testMyClass2() {
        //var ollirResult = TestUtils.optimize(SpecsIo.getResource("fixtures/public/HelloWorld.jmm"));
        //TestUtils.noErrors(ollirResult.getReports());

        OllirResult ollirResult = new OllirResult(SpecsIo.getResource("fixtures/public/ollir/myclass2.ollir"), Collections.emptyMap());
        var jasminGen = new JasminGenerator();
        var jasminResult = jasminGen.toJasmin(ollirResult);

    }

    @Test
    public void testMyClass3() {
        //var ollirResult = TestUtils.optimize(SpecsIo.getResource("fixtures/public/HelloWorld.jmm"));
        //TestUtils.noErrors(ollirResult.getReports());

        OllirResult ollirResult = new OllirResult(SpecsIo.getResource("fixtures/public/ollir/myclass3.ollir"), Collections.emptyMap());
        var jasminGen = new JasminGenerator();
        var jasminResult = jasminGen.toJasmin(ollirResult);

    }

    @Test
    public void testMyClass4() {
        //var ollirResult = TestUtils.optimize(SpecsIo.getResource("fixtures/public/HelloWorld.jmm"));
        //TestUtils.noErrors(ollirResult.getReports());

        OllirResult ollirResult = new OllirResult(SpecsIo.getResource("fixtures/public/ollir/myclass4.ollir"), Collections.emptyMap());
        var jasminGen = new JasminGenerator();
        var jasminResult = jasminGen.toJasmin(ollirResult);


    }
}
