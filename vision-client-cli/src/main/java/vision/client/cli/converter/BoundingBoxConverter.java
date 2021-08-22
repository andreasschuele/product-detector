package vision.client.cli.converter;

import vision.client.cli.model.BoundingBox;
import picocli.CommandLine;

public class BoundingBoxConverter implements CommandLine.ITypeConverter<BoundingBox> {

    public BoundingBox convert(String value) throws Exception {
        String[] boxValuesAsString = value.split(",");

        BoundingBox boundingBox = new BoundingBox();

        boundingBox.setX(Integer.parseInt(boxValuesAsString[0]));
        boundingBox.setY(Integer.parseInt(boxValuesAsString[1]));
        boundingBox.setWidth(Integer.parseInt(boxValuesAsString[2]));
        boundingBox.setHeight(Integer.parseInt(boxValuesAsString[3]));

        return boundingBox;
    }

}