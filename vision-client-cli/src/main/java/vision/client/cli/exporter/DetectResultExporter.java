package vision.client.cli.exporter;

import vision.client.generated.vision.client.model.ModelDetectResponse;

import java.io.File;

public interface DetectResultExporter {

    void export(String imageDescriptor, ModelDetectResponse modelDetectResponse, File outputFile) throws Exception;

}
