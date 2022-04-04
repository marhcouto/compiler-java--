import org.junit.Test;

import pt.up.fe.comp.TestUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class ExampleTest {

    @Test
    public void testExpression() {
        //TestUtils.mustFail(parseResult.getReports());
        try {
            File myFile = new File("test/fixtures/public/fail/syntactical/CompleteWhileTest.jmm");
            String myReader = new Scanner(myFile).useDelimiter("\\Z").next();
            System.out.println(myReader);
            var parserResult = TestUtils.parse(myReader);
            parserResult.getReports().get(0).getException().get().printStackTrace();
            //var analysisResult = TestUtils.analyse(parserResult);
        } catch (FileNotFoundException e) {
            System.out.println(e.toString());
        }
    }

}
