package vision.client.cli.commands;

import vision.client.cli.VisionClientCLI;
import vision.client.cli.model.Authentication;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

import java.util.concurrent.Callable;

@Command(
    name = "model",
    subcommands = {
        ModelCreateCommand.class,
        ModelListCommand.class,
        ModelDeleteCommand.class,
        ModelTrainCommand.class,
        ModelDetectCommand.class,
        ModelDataCommand.class
})
public class ModelCommand implements Callable<Integer> {

    @ParentCommand
    private VisionClientCLI parentCommand;

    public String getApiURL() {
        return parentCommand.getApiURL();
    }

    public Authentication getAuthenticate() {
        return parentCommand.getAuthenticate();
    }

    @Override
    public Integer call() {
        return 0;
    }

}