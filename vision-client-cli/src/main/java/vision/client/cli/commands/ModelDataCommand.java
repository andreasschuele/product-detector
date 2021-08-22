package vision.client.cli.commands;

import vision.client.cli.model.Authentication;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

import java.util.concurrent.Callable;

@Command(
    name = "data",
    subcommands = {
        ModelDataAddCommand.class,
        ModelDataRemoveCommand.class
})
public class ModelDataCommand implements Callable<Integer> {

    @ParentCommand
    private ModelCommand parentCommand;

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