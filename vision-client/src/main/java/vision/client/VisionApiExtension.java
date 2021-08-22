package vision.client;

import vision.client.generated.vision.client.api.VisionApi;
import vision.client.generated.vision.client.invoker.ApiClient;
import vision.client.generated.vision.client.invoker.ApiResponse;
import vision.client.generated.vision.client.model.ModelGetAllResponse;
import vision.client.generated.vision.client.model.ModelGetAllResponseModels;

import java.text.MessageFormat;
import java.util.Optional;


/**
 * The VisionApiExtension extends the auto-generated VisionApi and adds further functionality.
 */
public class VisionApiExtension extends VisionApi {

    /**
     * Constructor.
     *
     * @param apiClient An ApiClient object.
     */
    VisionApiExtension(ApiClient apiClient) {
        super(apiClient);
    }

    /**
     * Queries the Vision API to retrieve the vision model id for a vision model.
     *
     * @param modelName The vision model name.
     * @return Returns the vision model id or throws an exception.
     * @throws Exception
     */
    public String getModelIdByModelName(String modelName) throws Exception {
        ApiResponse<ModelGetAllResponse> modelGetAllResponse = modelGetAllWithHttpInfo();

        if (modelGetAllResponse.getStatusCode() != 200) {
            throw new Exception("Model identification failed (HTTP status != 200).");
        } else if (modelGetAllResponse == null || modelGetAllResponse.getData().getModels() == null) {
            throw new Exception(MessageFormat.format("Can't find model ''{0}''.", modelName));
        }

        Optional<ModelGetAllResponseModels> modelGetAllResponseModelsOptional = modelGetAllResponse.getData().getModels().stream()
                .filter(e -> modelName.equals(e.getModelName()))
                .findAny();

        if (modelGetAllResponseModelsOptional.isEmpty()) {
            throw new Exception(MessageFormat.format("Can't find model ''{0}''.", modelName));
        }

        return modelGetAllResponseModelsOptional.get().getModelId();
    }

}
