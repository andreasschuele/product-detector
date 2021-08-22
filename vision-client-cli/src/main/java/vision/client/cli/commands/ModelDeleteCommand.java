package vision.client.cli.commands;

import vision.client.cli.VisionClientCLI;
import vision.client.VisionApiExtension;
import vision.client.generated.vision.client.invoker.ApiResponse;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

import java.text.MessageFormat;
import java.util.concurrent.Callable;

@Command(name = "delete")
public
class ModelDeleteCommand implements Callable<Integer> {

    @ParentCommand
    protected ModelCommand parentCommand;

    @Parameters(index = "0", description = "model name")
    private String modelName;

    @Override
    public Integer call() throws Exception {
        VisionApiExtension visionApi = CommandHelper.newVisionApi(parentCommand.getApiURL(), parentCommand.getAuthenticate());

        String modelId = visionApi.getModelIdByModelName(modelName);

        VisionClientCLI.out.println(MessageFormat.format("Delete model ''{0}'' with id: {1}", modelName, modelId));

        ApiResponse<?> modelDeleteResponse = visionApi.modelDeleteWithHttpInfo(modelId);

        if (modelDeleteResponse.getStatusCode() == 200) {
            VisionClientCLI.out.println("Deleted.");
        } else {
            VisionClientCLI.out.println("Deletion failed.");
        }

        return 0;
    }
}
