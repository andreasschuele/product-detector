package productdetector.payload;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiResponseDto {

    private Boolean success;

    private String message;

}
