package vision.client.cli.commands;

import vision.client.cli.VisionClientCLI;
import vision.client.VisionApiExtension;
import vision.client.generated.vision.client.model.ModelGetAllResponse;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

import java.text.MessageFormat;
import java.util.concurrent.Callable;

@Command(name = "list")
public class ModelListCommand implements Callable<Integer> {

    @ParentCommand
    protected ModelCommand parentCommand;

    @Override
    public Integer call() throws Exception {
        VisionApiExtension visionApi = CommandHelper.newVisionApi(parentCommand.getApiURL(), parentCommand.getAuthenticate());

        ModelGetAllResponse modelGetAllResponse = visionApi.modelGetAll();

        if (modelGetAllResponse != null && modelGetAllResponse.getModels() != null) {
            modelGetAllResponse.getModels().forEach((model -> {
                VisionClientCLI.out.println(MessageFormat.format("Model: {0}", model.getModelName()));
            }));
        }

        return 0;
    }

}
