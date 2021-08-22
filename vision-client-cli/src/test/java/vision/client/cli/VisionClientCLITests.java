package vision.client.cli;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * The VisionClientCLITests class contains a couple of tests to check the VisionClientCLI functionality.
 */
public class VisionClientCLITests {

    private static String API_URL = "http://localhost:5080";

    private static String API_AUTH = "user:user";

    private VisionClientCLI visionClientCLI;

    private StringWriter sw;

    private PrintWriter pw;

    private CommandLine cmd;

    @BeforeEach
    void setUp() throws Exception {
        visionClientCLI = new VisionClientCLI();
        sw = new StringWriter();
        pw = new PrintWriter(sw, true);
        cmd = new CommandLine(visionClientCLI);
        cmd.setOut(pw);
        visionClientCLI.setCommandLine(cmd);
    }

    @Test
    public void testVersion() throws Exception {
        int exitCode = cmd.execute("vision", "-V");
        assertEquals(0, exitCode);
        assertEquals("1.0\r\n", sw.toString());
    }

    @Test
    public void testModelCreateDeleteCommand() throws Exception {
        int exitCode = cmd.execute("--authenticate", API_AUTH, API_URL, "model", "create", "modelName", "baseModel");

        assertEquals(0, exitCode);
        assertTrue(sw.toString().contains("Model created:"));

        String modelId = match(sw.toString(), "Model created: ([a-z0-9]+)", 1);

        assertTrue(modelId != null);

        exitCode = cmd.execute("--authenticate", API_AUTH, API_URL, "model", "delete", "modelName");

        assertEquals(0, exitCode);
    }

    //@Test
    public void testModelDataAddAndRemoveCommand() throws Exception {
        Path resourceDirectory = Paths.get("src","test","resources", "test-image.jpg");

        int exitCode = cmd.execute("--authenticate", API_AUTH, API_URL, "model", "data", "add", "test1", resourceDirectory.toAbsolutePath().toString(),"label");

        String dataId = match(sw.toString(), "Added width data id: ([a-z0-9]+)", 1);

        assertEquals(0, exitCode);
        assertTrue(sw.toString().contains("Added"));

        exitCode = cmd.execute("--authenticate", API_AUTH, API_URL, "model", "data", "remove", "test1", dataId);

        assertEquals(0, exitCode);
    }

    //@Test
    public void testModelDetectCommand() throws Exception {
        Path resourceDirectory = Paths.get("src","test","resources", "test-image.jpg");

        int exitCode = cmd.execute("--authenticate", API_AUTH, API_URL, "model", "detect", "test1", resourceDirectory.toAbsolutePath().toString());

        System.out.println(sw.toString());

        assertEquals(0, exitCode);
    }

    private String match(String in, String regex, int groupIdx) {
        Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(in);
        matcher.find();
        return matcher.group(groupIdx);
    }

}
