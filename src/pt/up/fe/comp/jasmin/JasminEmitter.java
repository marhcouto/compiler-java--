package pt.up.fe.comp.jasmin;

import org.specs.comp.ollir.ClassUnit;
import pt.up.fe.comp.jmm.jasmin.JasminBackend;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;

import java.io.File;
import java.util.Collections;

public class JasminEmitter implements JasminBackend {

    @Override
    public JasminResult toJasmin(OllirResult ollirResult) {
        String jasminCode = new OllirToJasmin(ollirResult.getOllirClass()).getCode();

        var jasminResult = new JasminResult(jasminCode);
        System.out.println("1");
        File outputDir = new File("test/fixtures/public/testing");
        System.out.println("2");
        jasminResult.compile(outputDir);
        System.out.println("3");

        return new JasminResult(ollirResult, jasminCode, Collections.emptyList());

    }
}
