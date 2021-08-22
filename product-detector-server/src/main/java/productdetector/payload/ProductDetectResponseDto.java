package productdetector.payload;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductDetectResponseDto {

    @Data
    public static class ProductDto {

        private Long id;

        private String name;

        private String notes;

        private BigDecimal probability;

        private String mainImage;

    }

    private List<ProductDto> products;

}
