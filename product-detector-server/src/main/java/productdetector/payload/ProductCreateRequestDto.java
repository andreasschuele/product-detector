package productdetector.payload;

import lombok.Data;

import java.util.List;

@Data
public class ProductCreateRequestDto {

    private String name;

    private String notes;

    private String mainImage;

}
