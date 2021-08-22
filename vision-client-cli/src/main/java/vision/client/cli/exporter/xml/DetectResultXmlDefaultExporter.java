package vision.client.cli.exporter.xml;

import vision.client.cli.exporter.DetectResultExporter;
import vision.client.cli.exporter.xml.elements.BoundingBoxElement;
import vision.client.cli.exporter.xml.elements.ObjectElement;
import vision.client.cli.exporter.xml.elements.RootElement;
import vision.client.generated.vision.client.model.ModelDetectResponse;
import vision.client.generated.vision.client.model.ModelDetectResponseDetectedObjects;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

public class DetectResultXmlDefaultExporter implements DetectResultExporter {

    @Override
    public void export(String imageDescriptor, ModelDetectResponse modelDetectResponse, File outputFile) throws Exception {
        if (modelDetectResponse == null
                || modelDetectResponse.getDetectedObjects() == null
                || outputFile == null) {
            return;
        }

        RootElement rootElement = prepareRootElement(imageDescriptor, modelDetectResponse);

        JAXBContext contextObj = JAXBContext.newInstance(RootElement.class);

        Marshaller marshallerObj = contextObj.createMarshaller();
        marshallerObj.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshallerObj.marshal(rootElement, new FileOutputStream(outputFile));
    }

    private RootElement prepareRootElement(String imageDescriptor, ModelDetectResponse modelDetectResponse) {
        LinkedList<ObjectElement> objects=new LinkedList<>();

        for (ModelDetectResponseDetectedObjects detectedObject : modelDetectResponse.getDetectedObjects()) {
            ObjectElement objectElement = new ObjectElement();

            objectElement.setTerm(detectedObject.getLabel());
            objectElement.setProbability(detectedObject.getProbability());

            if (detectedObject.getBoundingBox() != null) {
                BoundingBoxElement boundingBoxElement = new BoundingBoxElement();

                boundingBoxElement.setX(detectedObject.getBoundingBox().getX());
                boundingBoxElement.setY(detectedObject.getBoundingBox().getY());
                boundingBoxElement.setWidth(detectedObject.getBoundingBox().getWidth());
                boundingBoxElement.setHeight(detectedObject.getBoundingBox().getHeight());

                objectElement.setBoundingBox(boundingBoxElement);
            }

            objects.add(objectElement);
        }

        RootElement rootElement = new RootElement();

        rootElement.setFile(imageDescriptor);

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        String dateString = format.format( new Date()   );

        rootElement.setDate(dateString);
        rootElement.setObjects(objects);
        return rootElement;
    }

}
