package productdetector.service;

import vision.client.VisionApiExtension;


/**
 * The VisionService interface defines Vision API related methods interfaces.
 */
public interface VisionService {

    /**
     * Creates a new VisionApiExtension object.
     *
     * @return Returns a VisionApiExtension object.
     */
    VisionApiExtension newVisionApi();

    /**
     * Gets the default vision model id.
     *
     * @return Returns the default vision model id.
     */
    String getVisionModelId();

    /**
     * Get the default vision model name.
     *
     * @return Returns the default vision model name.
     */
    String getVisionModelName();

}