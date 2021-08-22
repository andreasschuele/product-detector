package vision.client;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;


/**
 * The VisionApiUtilsTest class contains a couple of tests to check the VisionApiUtils functionality.
 */
public class VisionApiUtilsTest {

	@Test
	public void testImageBase64Conversion() throws Exception {
		Path testImagePath = Paths.get("src","test","resources", "test-image.jpg");

		String base64 = VisionApiUtils.imageToBase64String(ImageIO.read(testImagePath.toFile()), "jpg");

		BufferedImage bufferedImage = VisionApiUtils.baseToImage64String(base64);

		assertNotNull(bufferedImage);
		assertEquals(1312, bufferedImage.getWidth());
		assertEquals(1276, bufferedImage.getHeight());
	}

}
