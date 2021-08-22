package productdetector.payload;

import lombok.Data;

import java.util.List;

@Data
public class ProductGetResponseDto {

    private String id;

    private String name;

    private String notes;

    private String mainImage;

}
