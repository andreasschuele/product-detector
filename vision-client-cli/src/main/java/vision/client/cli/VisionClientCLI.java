package vision.client.cli;

import vision.client.cli.commands.ModelCommand;
import vision.client.cli.converter.AuthenticationConverter;
import vision.client.cli.model.Authentication;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.PrintWriter;
import java.util.concurrent.Callable;


/**
 * The VisionClientCLI class contains the main() application entry point.
 */
@Command(
    name = "vision",
    mixinStandardHelpOptions = true,
    version = "1.0",
    description = "Detector client command line interface.",
    subcommands = {
        ModelCommand.class
})
public class VisionClientCLI implements Callable<Integer> {

    public static CommandLine COMMAND_LINE_INSTANCE;

    public static PrintWriter out;

    @Option(names = { "-v", "--verbose" }, defaultValue = "false")
    private boolean verbose = false;

    @Parameters(index = "0", description = "API URL")
    private String apiURL;

    @Option(names = { "-a", "--authenticate" }, defaultValue = "", converter = AuthenticationConverter.class)
    private Authentication authenticate;

    public boolean isVerbose() {
        return verbose;
    }

    public String getApiURL() {
        return apiURL;
    }

    public Authentication getAuthenticate() {
        return authenticate;
    }

    public static CommandLine getCommandLine() {
        return COMMAND_LINE_INSTANCE;
    }

    public static void setCommandLine(CommandLine commandLine) {
        COMMAND_LINE_INSTANCE = commandLine;
        out = commandLine.getOut();
    }

    @Override
    public Integer call() {
        return 0;
    }

    public static void main(String... args) {
        CommandLine commandLine = new CommandLine(new VisionClientCLI());
        commandLine.usage(System.out);

        setCommandLine(commandLine);

        int exitCode = commandLine.execute(args);
        System.exit(exitCode);
    }
}