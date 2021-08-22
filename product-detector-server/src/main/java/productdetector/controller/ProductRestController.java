package productdetector.controller;

import vision.client.VisionApiUtils;
import vision.client.generated.vision.client.invoker.ApiResponse;
import vision.client.generated.vision.client.model.*;
import io.swagger.v3.oas.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import productdetector.exception.ResourceNotFoundException;
import productdetector.model.Product;
import productdetector.model.ProductImage;
import productdetector.payload.*;
import productdetector.repository.ProductImageRepository;
import productdetector.repository.ProductRepository;
import productdetector.repository.UserRepository;
import productdetector.security.CurrentUser;
import productdetector.security.UserPrincipal;
import productdetector.service.ProductService;
import productdetector.service.VisionService;
import productdetector.util.Utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;


/**
 * The ProductRestController class implements product related REST endpoints.
 */
@RestController
@RequestMapping("/api/v1/product")
public class ProductRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductRestController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private VisionService visionService;

    @PostMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductCreateResponseDto> productCreate(@Parameter(hidden = true) @CurrentUser UserPrincipal currentUser,
                                                                  @RequestBody ProductCreateRequestDto productCreateRequestDto) {
        Product product = productService.createProduct(productCreateRequestDto.getName());

        ProductCreateResponseDto response = new ProductCreateResponseDto();

        response.setId(product.getId().toString());
        response.setName(product.getName());
        response.setNotes(product.getNotes());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ProductGetResponseDto> productGet(@Parameter(hidden = true) @CurrentUser UserPrincipal currentUser,
                                                            @PathVariable("id") Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        ProductGetResponseDto response = new ProductGetResponseDto();

        response.setId(product.getId().toString());
        response.setName(product.getName());
        response.setNotes(product.getNotes());

        if (product.getMainImage() != null && product.getMainImage().length != 0) {
            try {
                ByteArrayInputStream bis = new ByteArrayInputStream(product.getMainImage());
                BufferedImage bufferedImage = ImageIO.read(bis);
                response.setMainImage("data:image/jpeg;base64," + VisionApiUtils.imageToBase64String(bufferedImage, "jpg"));
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping()
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ProductGetAllResponseDto> productGetAll(@Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) {
        List<Product> products = productRepository.findAll();

        List<ProductGetAllResponseDto.ProductDto> responseProducts = new LinkedList<>();

        for (Product product : products) {
            ProductGetAllResponseDto.ProductDto responseProduct = new ProductGetAllResponseDto.ProductDto();

            responseProduct.setId(product.getId().toString());
            responseProduct.setName(product.getName());
            responseProduct.setNotes(product.getNotes());

            responseProducts.add(responseProduct);
        }

        ProductGetAllResponseDto response = new ProductGetAllResponseDto();

        responseProducts.sort(Comparator.comparing(ProductGetAllResponseDto.ProductDto::getName));

        response.setProducts(responseProducts);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductUpdateResponseDto> productUpdate(@Parameter(hidden = true) @CurrentUser UserPrincipal currentUser,
                                                                  @PathVariable("id") Long productId,
                                                                  @RequestBody ProductUpdateRequestDto productUpdateRequest) throws IOException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        if (productUpdateRequest.getName() != null) {
            product.setName(productUpdateRequest.getName());
        }

        if (productUpdateRequest.getNotes() != null) {
            product.setNotes(productUpdateRequest.getNotes());
        }

        if (productUpdateRequest.getActive() != null) {
            product.setActive(productUpdateRequest.getActive());
        }

        if (productUpdateRequest.getMainImage() != null) {
            String imageData = productUpdateRequest.getMainImage();

            if (imageData.contains("base64")) {
                imageData = imageData.substring(imageData.indexOf("base64") + 7);
            }

            BufferedImage bufferedImage = VisionApiUtils.baseToImage64String(imageData);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            ImageIO.write(bufferedImage, "jpg", bos );

            product.setMainImage(bos.toByteArray());
            product.setMainImageType("jpg");
        }

        if (productUpdateRequest.getName() != null
            || productUpdateRequest.getNotes() != null
            || productUpdateRequest.getActive() != null) {

            product = productRepository.save(product);
        }

        ProductUpdateResponseDto response = new ProductUpdateResponseDto();

        response.setId(product.getId().toString());
        response.setName(product.getName());
        response.setNotes(product.getNotes());

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity productDelete(@Parameter(hidden = true) @CurrentUser UserPrincipal currentUser,
                                        @PathVariable("id") Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        productImageRepository.deleteByProductId(product.getId());
        productImageRepository.flush();
        productRepository.delete(product);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/detect")
    @PreAuthorize("hasRole('USER')")
    @Transactional(readOnly = true)
    public ResponseEntity<ProductDetectResponseDto> detect(@Parameter(hidden = true) @CurrentUser UserPrincipal currentUser,
                                                           @RequestBody ProductDetectRequestDto productDetectRequest) throws Exception {
        ImageDto imageDto = productDetectRequest.getImage();

        String imageData = imageDto.getData();

        if (imageData.contains("base64")) {
            imageData = imageData.substring(imageData.indexOf("base64") + 7);
        }

        BufferedImage bufferedImage = VisionApiUtils.baseToImage64String(imageData);

        ModelDetectRequest modelDetectRequest = new ModelDetectRequest();

        modelDetectRequest.setImage(new Image()
                .format("jpg")
                .encoding("base64")
                .data(VisionApiUtils.imageToBase64String(bufferedImage, "jpg")));

        String modelId = visionService.getVisionModelId();

        ApiResponse<ModelDetectResponse> modelDetectResponse = visionService.newVisionApi().modelDetectWithHttpInfo(modelId, modelDetectRequest);

        if (modelDetectResponse.getStatusCode() != 200) {
            throw new Exception("Model detection failed (HTTP status != 200).");
        }

        if (modelDetectResponse.getData() == null
            || modelDetectResponse.getData().getDetectedObjects() == null) {
            return ResponseEntity.ok(Utils.touch(new ProductDetectResponseDto(), e -> {
            }));
        }


        List<ProductDetectResponseDto.ProductDto> products = new LinkedList<>();

        for (ModelDetectResponseDetectedObjects detect : modelDetectResponse.getData().getDetectedObjects()) {
            Optional<Product> productOptional = productRepository.findByVisionLabel(detect.getLabel());

            if (productOptional.isPresent() && productOptional.get().getActive()) {
                Product product = productOptional.get();
                products.add(Utils.touch(new ProductDetectResponseDto.ProductDto(), e -> {
                    e.setId(product.getId());
                    e.setName(product.getName());
                    e.setNotes(product.getNotes());
                    e.setProbability(BigDecimal.valueOf(detect.getProbability()));

                    if (product.getMainImage() != null && product.getMainImage().length != 0) {
                        try {
                            ByteArrayInputStream bis = new ByteArrayInputStream(product.getMainImage());
                            BufferedImage bi = ImageIO.read(bis);
                            e.setMainImage("data:image/jpeg;base64," + VisionApiUtils.imageToBase64String(bi, "jpg"));
                        } catch (Exception e1) {
                            LOGGER.error(e1.getMessage(), e1);
                        }
                    }
                }));
            }
        }

        products.sort(Comparator.comparing(ProductDetectResponseDto.ProductDto::getProbability).reversed());

        return ResponseEntity.ok(Utils.touch(new ProductDetectResponseDto(), e -> {
            e.setProducts(products);
        }));
    }

    @PostMapping("/{id}/add-example-data")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity productAddProductExampleData(@Parameter(hidden = true) @CurrentUser UserPrincipal currentUser,
                                                       @PathVariable("id") Long productId,
                                                       @RequestBody ProductAddExampleDataRequestDto productAddExampleDataRequestDto) throws Exception {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        String imageData = productAddExampleDataRequestDto.getProductImage();

        if (imageData.contains("base64")) {
            imageData = imageData.substring(imageData.indexOf("base64") + 7);
        }

        BufferedImage bufferedImage = VisionApiUtils.baseToImage64String(imageData);

        ModelDataCreateRequest modelDataCreateRequest = new ModelDataCreateRequest();

        modelDataCreateRequest.setImage(new Image()
                .format("jpg")
                .encoding("base64")
                .data(VisionApiUtils.imageToBase64String(bufferedImage, "jpg")));
        modelDataCreateRequest.setObjects(List.of(new ObjectInData().label(product.getVisionLabel()).boundingBox(new BoundingBox().x(0).y(0).width(bufferedImage.getWidth()).height(bufferedImage.getHeight()))));

        String modelId = visionService.getVisionModelId();

        ApiResponse<ModelDataCreateResponse> modelDataCreateResponse = visionService.newVisionApi().modelDataCreateWithHttpInfo(modelId, modelDataCreateRequest);

        if (modelDataCreateResponse.getStatusCode() != 200) {
            throw new Exception("Model detection failed (HTTP status != 200).");
        }

        ProductImage productImage = new ProductImage();

        productImage.setProductId(product.getId());
        productImage.setVisionDataId(modelDataCreateResponse.getData().getDataId());
        productImage.setCreatedAt(Instant.now());

        productImageRepository.save(productImage);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/train-all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductTrainAllResponseDto> trainAll(@Parameter(hidden = true) @CurrentUser UserPrincipal currentUser,
                                                               @RequestBody ProductTrainAllRequestDto productTrainAllRequestDto) throws Exception {
        List<Product> products = productRepository.findAll();

        if (products == null) {
            return ResponseEntity.notFound().build();
        }

        Set<String> visionLabels = new HashSet<>();

        for (Product product : products) {
            visionLabels.add(product.getVisionLabel());
        }

        String modelId = visionService.getVisionModelId();

        ModelTrainRequest modelTrainRequest = new ModelTrainRequest();

        modelTrainRequest.setSettings(new LinkedList<>());
        modelTrainRequest.setLabels(visionLabels.stream().collect(Collectors.toList()));

        ApiResponse<ModelTrainResponse> modelTrainResponse = visionService.newVisionApi().modelTrainWithHttpInfo(modelId, modelTrainRequest);

        if (modelTrainResponse.getStatusCode() != 200) {
            throw new Exception("Model detection failed (HTTP status != 200).");
        }

        return ResponseEntity.ok(Utils.touch(new ProductTrainAllResponseDto(), e -> {
            e.setTrainId(modelTrainResponse.getData().getTrainId());
        }));
    }

    @PostMapping("/demo-data-setup")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity demoDataSetup(@Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) throws Exception {
        productService.recreateProductDataFromVisionModel();

        return ResponseEntity.ok().build();
    }

}
