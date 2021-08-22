package productdetector.payload;

import lombok.Data;

import java.util.List;

@Data
public class ProductUpdateRequestDto {

    private Boolean active;

    private String name;

    private String notes;

    private String mainImage;

}
