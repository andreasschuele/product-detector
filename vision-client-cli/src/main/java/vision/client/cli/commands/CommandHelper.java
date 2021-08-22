package vision.client.cli.commands;

import vision.client.cli.model.Authentication;
import vision.client.VisionApiExtension;
import vision.client.VisionApiFactory;

public class CommandHelper {

    public static VisionApiExtension newVisionApi(String url, Authentication authentication) throws Exception {
        VisionApiExtension visionApi = null;

        if (authentication != null) {
            visionApi = VisionApiFactory.newVisionApi(url, authentication.getUsername(), authentication.getPassword());
        } else {
            visionApi = VisionApiFactory.newVisionApi(url, null, null);
        }

        return visionApi;
    }

}