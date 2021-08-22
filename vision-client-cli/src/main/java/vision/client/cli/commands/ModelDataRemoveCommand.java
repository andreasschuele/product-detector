package vision.client.cli.commands;

import vision.client.cli.VisionClientCLI;
import vision.client.VisionApiExtension;
import vision.client.generated.vision.client.invoker.ApiResponse;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

import java.util.concurrent.Callable;

@Command(name = "remove")
public class ModelDataRemoveCommand implements Callable<Integer> {

    @ParentCommand
    protected ModelDataCommand parentCommand;

    @Parameters(index = "0", description = "model name")
    private String modelName;

    @Parameters(index = "1", description = "data id")
    private String dataId;

    @Override
    public Integer call() throws Exception {
        VisionApiExtension visionApi = CommandHelper.newVisionApi(parentCommand.getApiURL(), parentCommand.getAuthenticate());

        String modelId = visionApi.getModelIdByModelName(modelName);

        ApiResponse<?> modelDataDeleteResponse = visionApi.modelDataDeleteWithHttpInfo(modelId, dataId);

        if (modelDataDeleteResponse.getStatusCode() != 200) {
            VisionClientCLI.out.println("Deletion failed.");
            return 1;
        }

        VisionClientCLI.out.println("Deleted.");
        return 0;
    }

}
