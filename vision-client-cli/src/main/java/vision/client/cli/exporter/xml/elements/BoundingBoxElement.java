package vision.client.cli.exporter.xml.elements;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement(name = "object")
@XmlAccessorType(XmlAccessType.FIELD)
public class BoundingBoxElement {

    @XmlElement
    private int x;

    @XmlElement
    private int y;

    @XmlElement
    private int width;

    @XmlElement
    private int height;

}