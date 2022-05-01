package wiest.median.calculator.file;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.File;

public class LocalFileTest {
    static final String TEST_DATA_DIR = "./testDataDir";

    @BeforeAll
    static void prepare() {
        File testDataDir = new File(TEST_DATA_DIR);
        if (!testDataDir.exists()) {
            testDataDir.mkdir();
        }
    }

    @AfterAll
    static void cleanUp() {
        final File folder = new File(TEST_DATA_DIR);
        for (var file : folder.listFiles()) {
            if (file.getName().startsWith("Median_")) {
                file.delete();
            }
        }
    }

}
