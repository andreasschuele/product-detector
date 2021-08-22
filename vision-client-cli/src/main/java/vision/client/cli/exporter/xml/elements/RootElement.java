package vision.client.cli.exporter.xml.elements;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import javax.xml.bind.annotation.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement(name="data")
@XmlAccessorType(XmlAccessType.FIELD)
public class RootElement {

    @XmlElement
    private String file;

    @XmlElement
    private String date;

    @XmlElementWrapper
    @XmlElement(name="object")
    private List<ObjectElement> objects;

}