package vision.client;

import vision.client.generated.vision.client.invoker.ApiClient;

import java.net.http.HttpRequest;
import java.util.Base64;


/**
 * The VisionApiFactory class supports the creation of a VisionApiExtension object.
 */
public class VisionApiFactory {

    /**
     * Creates a new VisionApiExtension object based on passed parameters.
     *
     * @param baseApiUrl The Vision API base URL.
     * @param username The Vision API username.
     * @param password The Vision API password.
     * @return Returns a VisionApiExtension object or throws an Exception.
     * @throws Exception
     */
    public static VisionApiExtension newVisionApi(String baseApiUrl, String username, String password) throws Exception {
        ApiClient apiClient = new ApiClient();
        apiClient.updateBaseUri(baseApiUrl + "/api/v1");

        final String authorizationHeader = "Base " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes("UTF-8"));

        apiClient.setRequestInterceptor((HttpRequest.Builder httpRequestBuilder) -> {
            httpRequestBuilder.setHeader("Authorization", authorizationHeader);
        });

        return new VisionApiExtension(apiClient);
    }

}
