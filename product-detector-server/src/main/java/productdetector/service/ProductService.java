package productdetector.service;

import productdetector.model.Product;
import productdetector.model.ProductImage;

import javax.validation.constraints.NotNull;
import java.awt.image.BufferedImage;
import java.util.Optional;


/**
 * The ProductService interface defines product related methods interfaces.
 */
public interface ProductService {

    /**
     * Creates a new product with given name and persists it in the database.
     *
     * @param name The product name.
     * @return Returns a Product entity.
     */
    Product createProduct(@NotNull String name);

    /**
     * Queries the database for a product by given product id.
     *
     * @param id The product id.
     * @return Returns a Product entity wrapped into an Optional object.
     */
    Optional<Product> findProductById(@NotNull Long id);

    /**
     * Updates and persists the given product entity.
     *
     * @param product The Product entity.
     * @return Returns an updated Product entity.
     */
    Product updateProduct(@NotNull Product product);

    /**
     * Updates and persists the main product image for the given product entity.
     *
     * @param product The Product entity.
     * @param bufferedImage The image.
     * @return Returns an updated Product entity.
     */
    Product updateProductMainImage(@NotNull Product product, @NotNull BufferedImage bufferedImage);

    /**
     * Recreates product data of the default vision model.
     * @throws Exception
     */
    void recreateProductDataFromVisionModel() throws Exception;

}