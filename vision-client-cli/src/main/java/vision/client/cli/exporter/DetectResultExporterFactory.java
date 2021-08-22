package vision.client.cli.exporter;

import vision.client.cli.exporter.xml.DetectResultXmlDefaultExporter;

import java.text.MessageFormat;

public class DetectResultExporterFactory {

    public static DetectResultExporter newExporter(String outputType) throws Exception {
        if ("xml".equals(outputType)) {
            return new DetectResultXmlDefaultExporter();
        }

        throw new Exception(MessageFormat.format("Detect result output type ''{0}'' not supported.", outputType));
    }

}
