package vision.client.cli.model;

import lombok.Data;

@Data
public class BoundingBox {

    private int x;

    private int y;

    private int width;

    private int height;

}