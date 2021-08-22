package vision.client.cli.exporter.xml.elements;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class ObjectElement {

    @XmlElement
    private String term;

    @XmlElement
    private float probability;

    @XmlElement(name="bounding-box")
    private BoundingBoxElement boundingBox;

}