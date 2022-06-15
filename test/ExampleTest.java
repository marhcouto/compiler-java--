import org.junit.Test;

import pt.up.fe.comp.TestUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class ExampleTest {

    @Test
    public void testExpression() {
        try {
            File myFile = new File("test/fixtures/public/Fac.jmm");
            String myReader = new Scanner(myFile).useDelimiter("\\Z").next();
            var parserResult = TestUtils.parse(myReader);
            TestUtils.analyse(parserResult);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}
