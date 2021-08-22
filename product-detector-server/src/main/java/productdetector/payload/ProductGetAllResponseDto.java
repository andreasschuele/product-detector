package productdetector.payload;

import lombok.Data;

import java.util.List;

@Data
public class ProductGetAllResponseDto {

    @Data
    public static class ProductDto {

        private String id;

        private String name;

        private String notes;

    }

    private List<ProductDto> products;

}
