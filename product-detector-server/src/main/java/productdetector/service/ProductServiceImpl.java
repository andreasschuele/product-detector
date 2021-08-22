package productdetector.service;

import vision.client.generated.vision.client.invoker.ApiResponse;
import vision.client.generated.vision.client.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import productdetector.model.Product;
import productdetector.model.ProductImage;
import productdetector.repository.ProductImageRepository;
import productdetector.repository.ProductRepository;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;


/**
 * This ProductServiceImpl service class implements the ProductService interface.
 */
@Service
public class ProductServiceImpl implements ProductService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductServiceImpl.class);

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    @Autowired
    private VisionService visionService;

    @Value("${vision.demo.baseModelName}")
    private String visionDemoBaseModelName;

    /**
     * @see productdetector.service.ProductService#createProduct(String) 
     */
    @Override
    @Transactional
    public Product createProduct(String name) {
        return createProduct(name, "lbl_" + name);
    }

    @Transactional
    public Product createProduct(String name, String label) {
        Optional<Product> productOptional = productRepository.findByName(name);

        if (productOptional.isPresent()) {
            throw new RuntimeException("Product with the same name already exists.");
        }

        Product product = new Product();

        product.setName(name);
        product.setActive(Boolean.TRUE);
        product.setVisionLabel(label);

        return productRepository.save(product);
    }

    /**
     * @see productdetector.service.ProductService#findProductById(Long) 
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Product> findProductById(Long id) {
        return productRepository.findById(id);
    }

    /**
     * @see productdetector.service.ProductService#updateProduct(Product) 
     */
    @Override
    @Transactional
    public Product updateProduct(Product product) {
        Optional<Product> productOptional = productRepository.findById(product.getId());

        if (productOptional.isEmpty()) {
            throw new RuntimeException("Product doesn't exist.");
        }

        return productRepository.save(product);
    }

    /**
     * @see productdetector.service.ProductService#updateProductMainImage(Product, BufferedImage)
     */
    @Override
    @Transactional
    public Product updateProductMainImage(Product product, BufferedImage bufferedImage) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            ImageIO.write(bufferedImage, "jpg", bos );

            product.setMainImage(bos.toByteArray());
            product.setMainImageType("jpg");
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeException("Can't process product image.", e);
        }

        return updateProduct(product);
    }

    /**
     * @see productdetector.service.ProductService#recreateProductDataFromVisionModel()
     */
    @Override
    @Transactional
    public void recreateProductDataFromVisionModel() throws Exception {
        try {
            ApiResponse<?> modelDeleteResponse = visionService.newVisionApi().modelDeleteWithHttpInfo(visionService.getVisionModelId());
        } catch (Exception e) {
            LOGGER.warn("Can't delete vision model.");
        }

        productImageRepository.deleteAll();
        productImageRepository.flush();
        productRepository.deleteAll();
        productRepository.flush();

        ModelCreateRequest modelCreateRequest = new ModelCreateRequest();

        modelCreateRequest.setModelName(visionService.getVisionModelName());
        modelCreateRequest.setBaseModel(visionDemoBaseModelName);

        ApiResponse<?> modelCreateResponse = visionService.newVisionApi().modelCreateWithHttpInfo(modelCreateRequest);

        if (modelCreateResponse.getStatusCode() != 200) {
            throw new Exception("Can't create model.");
        }

        ApiResponse<ModelDataGetAllResponse> modelDataGetAllResponse = visionService.newVisionApi().modelDataGetAllWithHttpInfo(visionService.getVisionModelId());

        HashMap<String, List<String>> productData = new HashMap<>();

        for (ModelDataGetAllResponseData modelData : modelDataGetAllResponse.getData().getData()) {
            for (ObjectInData objectInData : modelData.getObjects()) {
                if (!productData.containsKey(objectInData.getLabel())) {
                    productData.put(objectInData.getLabel(), new LinkedList<>());
                }

                productData.get(objectInData.getLabel()).add(modelData.getDataId());
            }
        }

        for (Map.Entry<String, List<String>> e : productData.entrySet()) {
            final Product product = createProduct(e.getKey(), e.getKey());

            List<ProductImage> productImages = e.getValue().stream().map(dataId -> {
                ProductImage productImage = new ProductImage();

                productImage.setProductId(product.getId());
                productImage.setVisionDataId(dataId);
                productImage.setCreatedAt(Instant.now());

                return productImage;
            }).collect(Collectors.toList());

            productImageRepository.saveAll(productImages);

            if (!productImages.isEmpty()) {
                String visionDataId = productImages.get(0).getVisionDataId();

                ApiResponse<ModelDataGetResponse> dataGetResponse = visionService.newVisionApi().modelDataGetWithHttpInfo(visionService.getVisionModelId(), visionDataId, "false");

                ByteArrayInputStream is = new ByteArrayInputStream(Base64.getDecoder().decode(dataGetResponse.getData().getImage().getData().getBytes(StandardCharsets.UTF_8)));

                BufferedImage bufferedImage = ImageIO.read(is);

                if (product.getMainImage() == null || product.getMainImage().length == 0) {
                    updateProductMainImage(product, bufferedImage);
                }
            }
        }
    }

}