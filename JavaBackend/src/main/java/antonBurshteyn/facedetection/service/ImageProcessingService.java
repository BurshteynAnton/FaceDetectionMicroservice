package antonBurshteyn.facedetection.service;

import antonBurshteyn.facedetection.grpc.FaceDetectionProto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ImageProcessingService {
    boolean isValidImage(MultipartFile file);

    byte[] streamFileData(MultipartFile file) throws IOException;

    void saveValidatedPhoto(byte[] photoData, String name, FaceDetectionProto.Face face);

    boolean doesPhotoExist(String name);

    boolean isValidFaceDetection(FaceDetectionProto.FaceDetectionResponse response);

    void logInvalidFaceDetection(String name, FaceDetectionProto.FaceDetectionResponse response);

    FaceDetectionProto.FaceDetectionResponse validatePhoto(String name, byte[] photoData);
}
