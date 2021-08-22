package vision.client.cli.commands;

import vision.client.cli.VisionClientCLI;
import vision.client.cli.converter.BoundingBoxConverter;
import vision.client.cli.model.BoundingBox;
import vision.client.VisionApiExtension;
import vision.client.VisionApiUtils;
import vision.client.generated.vision.client.invoker.ApiResponse;
import vision.client.generated.vision.client.model.*;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "add")
public class ModelDataAddCommand implements Callable<Integer> {

    @ParentCommand
    protected ModelDataCommand parentCommand;

    @Parameters(index = "0", description = "model name")
    private String modelName;

    @Parameters(index = "1", description = "image file path")
    private File imageFile;

    @Parameters(index = "2", description = "label")
    private String label;

    @CommandLine.Option(names = { "-bb", "--boundingBox" }, converter = BoundingBoxConverter.class)
    private BoundingBox boundingBox;

    @Override
    public Integer call() throws Exception {
        VisionApiExtension visionApi = CommandHelper.newVisionApi(parentCommand.getApiURL(), parentCommand.getAuthenticate());

        String modelId = visionApi.getModelIdByModelName(modelName);

        ModelDataCreateRequest modelDataCreateRequest = new ModelDataCreateRequest();

        vision.client.generated.vision.client.model.BoundingBox bb = null;

        if (boundingBox != null) {
            bb = new vision.client.generated.vision.client.model.BoundingBox()
                    .x(boundingBox.getX())
                    .y(boundingBox.getY())
                    .width(boundingBox.getWidth())
                    .height(boundingBox.getHeight());
        }

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

        modelDataCreateRequest.setImage(new Image()
                .fileName(imageFile.getName())
                .format(imageFormat)
                .encoding("base64")
                .data(VisionApiUtils.imageToBase64String(bufferedImage, imageFormat)));
        modelDataCreateRequest.setObjects(List.of(new ObjectInData().label(label).boundingBox(bb)));

        ApiResponse<ModelDataCreateResponse> modelDataCreateResponse = visionApi.modelDataCreateWithHttpInfo(modelId, modelDataCreateRequest);

        if (modelDataCreateResponse.getStatusCode() != 200) {
            VisionClientCLI.out.println("Failed.");
            return 1;
        }

        VisionClientCLI.out.println("Added width data id: " + modelDataCreateResponse.getData().getDataId());
        return 0;
    }

}
