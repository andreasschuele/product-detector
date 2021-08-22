package productdetector.payload;

import lombok.Data;

@Data
public class ImageDto {

    private String fileName;

    private String format;

    private String dataEncoding;

    private String data;

}
