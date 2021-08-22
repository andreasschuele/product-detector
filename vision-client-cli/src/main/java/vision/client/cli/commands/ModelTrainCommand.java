package vision.client.cli.commands;

import vision.client.VisionApiExtension;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;
import vision.client.cli.VisionClientCLI;
import vision.client.generated.vision.client.invoker.ApiResponse;
import vision.client.generated.vision.client.model.ModelTrainRequest;

import java.text.MessageFormat;
import java.util.concurrent.Callable;

@Command(name = "train")
public class ModelTrainCommand implements Callable<Integer> {

    @ParentCommand
    protected ModelCommand parentCommand;

    @Option(names = { "-s", "--settings" }, defaultValue = "")
    private String modelSettings;

    @Parameters(index = "0", description = "model name")
    private String modelName;

    @Override
    public Integer call() throws Exception {
        VisionApiExtension visionApi = CommandHelper.newVisionApi(parentCommand.getApiURL(), parentCommand.getAuthenticate());

        String modelId = visionApi.getModelIdByModelName(modelName);

        if (modelId == null) {
            VisionClientCLI.out.println(MessageFormat.format("Model ''{0}'' doesn't exist.", modelName));
            return 1;
        }

        VisionClientCLI.out.println(MessageFormat.format("Train model: {0}", modelName));

        ModelTrainRequest modelTrainRequest = new ModelTrainRequest();

        ApiResponse<?> apiResponse = visionApi.modelTrainWithHttpInfo(modelId, modelTrainRequest);

        if (apiResponse.getStatusCode() == 200) {
            System.out.println(MessageFormat.format("Model ''{0}'' training started ...", modelName));
        }

        return 0;
    }
}
