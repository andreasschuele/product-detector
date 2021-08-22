package productdetector.payload;

import lombok.Data;

import java.util.List;

@Data
public class ProductCreateResponseDto {

    private String id;

    private String name;

    private String notes;

}
