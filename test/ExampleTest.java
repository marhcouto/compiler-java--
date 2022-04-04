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
            File myFile = new File("test/fixtures/public/bug2.jmm");
            String myReader = new Scanner(myFile).useDelimiter("\\Z").next();
            var parserResult = TestUtils.parse(myReader);
            parserResult.getReports().get(0).getException().get().printStackTrace();
            //var analysisResult = TestUtils.analyse(parserResult);
        } catch (FileNotFoundException e) {
            System.out.println(e.toString());
        }
    }

}
