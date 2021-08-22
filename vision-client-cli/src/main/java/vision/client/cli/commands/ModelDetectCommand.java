package vision.client.cli.commands;

import vision.client.cli.VisionClientCLI;
import vision.client.cli.exporter.DetectResultExporter;
import vision.client.cli.exporter.DetectResultExporterFactory;
import vision.client.VisionApiExtension;
import vision.client.VisionApiUtils;
import vision.client.generated.vision.client.invoker.ApiResponse;
import vision.client.generated.vision.client.model.Image;
import vision.client.generated.vision.client.model.ModelDetectRequest;
import vision.client.generated.vision.client.model.ModelDetectResponse;
import vision.client.generated.vision.client.model.ModelDetectResponseDetectedObjects;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.MessageFormat;
import java.util.concurrent.Callable;

@Command(name = "detect")
public class ModelDetectCommand implements Callable<Integer> {

    @ParentCommand
    protected ModelCommand parentCommand;

    @Option(names = { "-o", "--output" })
    private File outputFile;

    @Option(names = { "-ot", "--output-type" }, defaultValue = "xml")
    private String outputType;

    @Parameters(index = "0", description = "model name")
    private String modelName;

    @Parameters(index = "1", description = "image file path")
    private File imageFile;

    @Override
    public Integer call() throws Exception {
        VisionApiExtension detectorVisionApi = CommandHelper.newVisionApi(parentCommand.getApiURL(), parentCommand.getAuthenticate());

        String modelId = detectorVisionApi.getModelIdByModelName(modelName);

        if (modelId == null) {
            VisionClientCLI.out.println(MessageFormat.format("Model ''{0}'' doesn't exist.", modelName));
            return 1;
        }

        VisionClientCLI.out.println(MessageFormat.format("Detect by model: {0}", modelName));

        String imageFormat = null;

        if (imageFile.getName().endsWith(".jpg")) {
            imageFormat = "jpg";
        } else if (imageFile.getName().endsWith(".png")) {
            imageFormat = "png";
        } else {
            VisionClientCLI.out.println("Image file type not support.");
            VisionClientCLI.out.println("Supported file types: *.jpg *.png");
            return 1;
        }

        BufferedImage bufferedImage = ImageIO.read(imageFile);

        ModelDetectRequest modelDetectRequest = new ModelDetectRequest();

        modelDetectRequest.setImage(new Image()
                .fileName(imageFile.getName())
                .format(imageFormat)
                .encoding("base64")
                .data(VisionApiUtils.imageToBase64String(bufferedImage, imageFormat)));

        ApiResponse<ModelDetectResponse> modelDetectResponse = detectorVisionApi.modelDetectWithHttpInfo(modelId, modelDetectRequest);

        if (modelDetectResponse.getStatusCode() != 200) {
            VisionClientCLI.out.println("Status != 200");
            return 1;
        }

        if (modelDetectResponse.getData() == null
            || modelDetectResponse.getData().getDetectedObjects() == null) {
            VisionClientCLI.out.println("No results.");
            return 0;
        }

        printDetectedResults(modelDetectResponse.getData());
        writeDetectedResultsToXml(imageFile.getName(), modelDetectResponse.getData(), outputFile, outputType);

        return 0;
    }

    private void printDetectedResults(ModelDetectResponse modelDetectResponse) {
        VisionClientCLI.out.println(MessageFormat.format("{0} object detected.", modelDetectResponse.getDetectedObjects().size()));

        for (ModelDetectResponseDetectedObjects detectedObject : modelDetectResponse.getDetectedObjects()) {
            VisionClientCLI.out.println(detectedObject.toString());
        }
    }

    private void writeDetectedResultsToXml(String imageDescriptor, ModelDetectResponse modelDetectResponse, File outputFile, String outputType) {
        if (outputFile == null || outputType.length() == 0) {
            return;
        }

        try {
            DetectResultExporter exporter = DetectResultExporterFactory.newExporter(outputType);

            exporter.export(imageDescriptor, modelDetectResponse, outputFile);
        } catch (Exception e) {
            e.printStackTrace(VisionClientCLI.out);
        }
    }
}
