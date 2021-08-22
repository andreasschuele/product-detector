package vision.client.cli.commands;

import vision.client.cli.VisionClientCLI;
import vision.client.VisionApiExtension;
import vision.client.generated.vision.client.model.ModelCreateRequest;
import vision.client.generated.vision.client.model.ModelCreateResponse;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

import java.text.MessageFormat;
import java.util.concurrent.Callable;

@Command(name = "create")
public class ModelCreateCommand implements Callable<Integer> {

    @ParentCommand
    protected ModelCommand parentCommand;

    @Parameters(index = "0", description = "model name")
    private String modelName;

    @Parameters(index = "1", description = "base model")
    private String baseModel;

    @Option(names = { "-s", "--settings" }, defaultValue = "")
    private String modelSettings;

    @Override
    public Integer call() throws Exception {
        VisionApiExtension visionApi = CommandHelper.newVisionApi(parentCommand.getApiURL(), parentCommand.getAuthenticate());

        VisionClientCLI.out.println(MessageFormat.format("Create model ''{0}'' with base model ''{1}''.", modelName, baseModel));

        ModelCreateRequest modelCreateRequest = new ModelCreateRequest();

        modelCreateRequest.setModelName(modelName);
        modelCreateRequest.setBaseModel(baseModel);

        ModelCreateResponse modelCreateResponse = visionApi.modelCreate(modelCreateRequest);

        if (modelCreateResponse.getModelId() != null) {
            VisionClientCLI.out.println("Model created: " + modelCreateResponse.getModelId());
        } else {
            VisionClientCLI.out.println("Model not created.");
        }

        return 0;
    }

}
