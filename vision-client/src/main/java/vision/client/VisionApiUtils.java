package vision.client;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;


/**
 * The VisionApiUtils class provides a couple of utility methods to convert image data to a base64 string and vice versa.
 */
public class VisionApiUtils {

    /**
     * Converts an image to a base64 string.
     *
     * @param image A RenderedImage object.
     * @param formatName The image data format.
     * @return Returns a base64 encoded string or throws an Exception.
     * @throws IOException
     */
    public static String imageToBase64String(RenderedImage image, String formatName) throws IOException {
        final ByteArrayOutputStream os = new ByteArrayOutputStream();

        ImageIO.write(image, formatName, os);

        return Base64.getEncoder().encodeToString(os.toByteArray());
    }

    /**
     * Converts a base64 string to an image.
     *
     * @param base64 Image data encoded as a base64 string.
     * @return Returns a BufferedImage object or throws an Exception.
     * @throws IOException
     */
    public static BufferedImage baseToImage64String(String base64) throws IOException {
        ByteArrayInputStream is = new ByteArrayInputStream(Base64.getDecoder().decode(base64));

        return ImageIO.read(is);
    }

}
