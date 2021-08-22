package productdetector.service;

import vision.client.VisionApiExtension;
import vision.client.VisionApiFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


/**
 * This VisionServiceImpl service class implements the VisionService interface.
 */
@Service
public class VisionServiceImpl implements VisionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(VisionServiceImpl.class);

    private static VisionApiExtension VISION_API = null;

    @Value("${vision.api.url}")
    private String visionApiUrl;

    @Value("${vision.api.authorization.user}")
    private String visionApiAuthorizationUser;

    @Value("${vision.api.authorization.password}")
    private String visionApiAuthorizationPassword;

    @Value("${vision.modelName}")
    private String visionModelName;

    /**
     * @see productdetector.service.VisionService#newVisionApi()
     */
    @Override
    public VisionApiExtension newVisionApi() {
        try {
            return VisionApiFactory.newVisionApi(visionApiUrl, visionApiAuthorizationUser, visionApiAuthorizationPassword);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

        return null;
    }

    /**
     * @see productdetector.service.VisionService#getVisionModelId()
     */
    @Override
    public String getVisionModelId() {
        try {
            return newVisionApi().getModelIdByModelName(visionModelName);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

        return null;
    }

    /**
     * @see productdetector.service.VisionService#getVisionModelName()
     */
    @Override
    public String getVisionModelName() {
        return visionModelName;
    }

}