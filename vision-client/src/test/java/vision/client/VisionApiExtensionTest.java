package vision.client;

import vision.client.generated.vision.client.invoker.ApiException;
import vision.client.generated.vision.client.invoker.ApiResponse;
import vision.client.generated.vision.client.model.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * The VisionApiExtensionTest class contains a couple of tests to check client functionality.
 *
 * Note that these tests can only be executed with a running Vision API.
 */
public class VisionApiExtensionTest {

	private VisionApiExtension visionApi;

	@BeforeEach
	void setUp() throws Exception {
		visionApi = VisionApiFactory.newVisionApi("http://localhost:5080", "user", "user");
	}

	@Test
	public void testAuthentication() {
		ApiException thrown = Assertions.assertThrows(
				ApiException.class,
				() -> {
					VisionApiExtension detectorVisionApi = VisionApiFactory.newVisionApi("http://localhost:5080", "", "");

					detectorVisionApi.modelGetAll();
				},
				"Expected 'Unauthorized Access', but it didn't."
		);

		assertTrue(thrown.getMessage().contains("Unauthorized Access"));
	}

	@Test
	public void testModelGetAll() throws Exception {
		ModelCreateResponse modelCreateResponse = visionApi.modelCreate(modelCreateRequest());

		assertTrue(modelCreateResponse.getModelId() != null);

		ModelGetAllResponse modelGetAllResponse = visionApi.modelGetAll();

		assertTrue(modelGetAllResponse.getModels().stream().findAny().map(e -> e.getModelId() == modelCreateResponse.getModelId()).isPresent());

		ApiResponse<?> modelDeleteResponse = visionApi.modelDeleteWithHttpInfo(modelCreateResponse.getModelId());

		assertTrue(modelDeleteResponse.getStatusCode() == 200);
	}

	@Test
	public void testModelGet() throws Exception {
		ModelCreateResponse modelCreateResponse = visionApi.modelCreate(modelCreateRequest());

		assertTrue(modelCreateResponse.getModelId() != null);

		ModelGetResponse modelGetResponse = visionApi.modelGet(modelCreateResponse.getModelId());

		assertEquals(modelGetResponse.getModelId(), modelCreateResponse.getModelId());

		ApiResponse<?> modelDeleteResponse = visionApi.modelDeleteWithHttpInfo(modelCreateResponse.getModelId());

		assertTrue(modelDeleteResponse.getStatusCode() == 200);
	}

	@Test
	public void testModelCreateDelete() throws Exception {
		ModelCreateResponse modelCreateResponse = visionApi.modelCreate(modelCreateRequest());

		assertTrue(modelCreateResponse.getModelId() != null);

		ApiResponse<?> modelDeleteResponse = visionApi.modelDeleteWithHttpInfo(modelCreateResponse.getModelId());

		assertTrue(modelDeleteResponse.getStatusCode() == 200);
	}

	//@Test
	// This test can only be executed with a trained vision model.
	public void testModelDetect() throws Exception {
		ModelCreateResponse modelCreateResponse = visionApi.modelCreate(modelCreateRequest());

		assertTrue(modelCreateResponse.getModelId() != null);

		ModelDetectRequest modelDetectRequest = new ModelDetectRequest();

		Path resourceDirectory = Paths.get("src","test","resources", "test-image.jpg");

		String image = VisionApiUtils.imageToBase64String(ImageIO.read(resourceDirectory.toFile()), "jpg");

		modelDetectRequest.image(new Image()
				.fileName("test-image.jpg")
				.format("jpg")
				.encoding("base64")
				.data(image));

		ModelDetectResponse modelDetectResponse = visionApi.modelDetect(modelCreateResponse.getModelId(), modelDetectRequest);

		assertTrue(modelDetectResponse.getDetectedObjects() != null);
		assertTrue(modelDetectResponse.getDetectedObjects().size() == 0);

		ApiResponse<?> modelDeleteResponse = visionApi.modelDeleteWithHttpInfo(modelCreateResponse.getModelId());

		assertTrue(modelDeleteResponse.getStatusCode() == 200);
	}

	@Test
	public void testModelAddData() throws Exception {
		ModelCreateResponse modelCreateResponse = visionApi.modelCreate(modelCreateRequest());

		assertTrue(modelCreateResponse.getModelId() != null);

		ModelDataCreateRequest modelDataCreateRequest = new ModelDataCreateRequest();

		List<ObjectInData> objects = new LinkedList<>();

		objects.add(new ObjectInData().label("test 1").boundingBox(new BoundingBox().x(0).y(1).width(100).height(101)));
		objects.add(new ObjectInData().label("test 2").boundingBox(new BoundingBox().x(10).y(20).width(50).height(51)));

		Path resourceDirectory = Paths.get("src","test","resources", "test-image.jpg");

		String image = VisionApiUtils.imageToBase64String(ImageIO.read(resourceDirectory.toFile()), "jpg");

		modelDataCreateRequest.setImage(new Image()
				.fileName("test-image.jpg")
				.format("jpg")
				.encoding("base64")
				.data(image));
		modelDataCreateRequest.setObjects(objects);

		ApiResponse<?> modelDataCreateResponse = visionApi.modelDataCreateWithHttpInfo(modelCreateResponse.getModelId(), modelDataCreateRequest);

		assertTrue(modelDataCreateResponse.getStatusCode() == 200);

		ApiResponse<?> modelDeleteResponse = visionApi.modelDeleteWithHttpInfo(modelCreateResponse.getModelId());

		assertTrue(modelDeleteResponse.getStatusCode() == 200);
	}

	private ModelCreateRequest modelCreateRequest() {
		ModelCreateRequest modelCreateRequest = new ModelCreateRequest();

		modelCreateRequest.setModelName("unittest-"+UUID.randomUUID().toString());
		modelCreateRequest.setBaseModel("");

		return modelCreateRequest;
	}

}
