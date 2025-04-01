package antonBurshteyn.facedetection;

import antonBurshteyn.facedetection.grpc.FaceDetectionProto;
import antonBurshteyn.facedetection.grpc.FaceDetectionServiceGrpc;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.*;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class FaceDetectionGrpcClientTest {

    private static Server grpcServer;
    private static final int PORT = 50099;

    private MockMultipartFile validJpegFile;
    private MockMultipartFile invalidFile;
    private TestImageProcessingService imageProcessingService;

    @BeforeAll
    static void startGrpcServer() throws IOException {
        grpcServer = ServerBuilder.forPort(PORT)
                .addService(new TestFaceDetectionService())
                .build()
                .start();
        System.out.println("Test gRPC server started on port " + PORT);
    }

    @AfterAll
    static void stopGrpcServer() throws InterruptedException {
        if (grpcServer != null) {
            grpcServer.shutdown().awaitTermination(5, TimeUnit.SECONDS);
            System.out.println("Test gRPC server shut down");
        }
    }

    @BeforeEach
    void setUp() {
        imageProcessingService = new TestImageProcessingService("localhost", PORT);

        byte[] validImageContent = new byte[1024];
        validJpegFile = new MockMultipartFile("file", "test.jpg", "image/jpeg", validImageContent);
        invalidFile = new MockMultipartFile("file", "test.txt", "text/plain", "This is not an image".getBytes());
    }

    @Test
    void shouldValidateImageFile() {
        assertTrue(imageProcessingService.isValidImage(validJpegFile));
        assertFalse(imageProcessingService.isValidImage(invalidFile));
    }

    @Test
    void shouldStreamFileDataCorrectly() throws IOException {
        byte[] data = imageProcessingService.streamFileData(validJpegFile);
        assertEquals(validJpegFile.getSize(), data.length);
    }

    @Test
    void shouldIntegrateWithGrpcServer() throws IOException {
        byte[] photoData = imageProcessingService.streamFileData(validJpegFile);
        FaceDetectionProto.FaceDetectionResponse response = imageProcessingService.validatePhoto(photoData);

        assertNotNull(response);
        assertEquals(1, response.getFacesCount());
        assertEquals(10, response.getFaces(0).getX());
        assertEquals(20, response.getFaces(0).getY());
    }

    static class TestImageProcessingService {
        private final FaceDetectionServiceGrpc.FaceDetectionServiceBlockingStub stub;

        public TestImageProcessingService(String host, int port) {
            var channel = io.grpc.ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
            this.stub = FaceDetectionServiceGrpc.newBlockingStub(channel);
        }

        public boolean isValidImage(MockMultipartFile file) {
            String type = file.getContentType();
            return type != null && (type.equals("image/jpeg") || type.equals("image/png"));
        }

        public byte[] streamFileData(MockMultipartFile file) throws IOException {
            return file.getBytes();
        }

        public FaceDetectionProto.FaceDetectionResponse validatePhoto(byte[] data) {
            FaceDetectionProto.ImageRequest request = FaceDetectionProto.ImageRequest.newBuilder()
                    .setImage(com.google.protobuf.ByteString.copyFrom(data))
                    .build();
            return stub.detectFaces(request);
        }
    }

    static class TestFaceDetectionService extends FaceDetectionServiceGrpc.FaceDetectionServiceImplBase {
        @Override
        public void detectFaces(FaceDetectionProto.ImageRequest request,
                                StreamObserver<FaceDetectionProto.FaceDetectionResponse> responseObserver) {

            byte[] imageData = request.getImage().toByteArray();
            String imageString = new String(imageData);

            FaceDetectionProto.FaceDetectionResponse.Builder responseBuilder =
                    FaceDetectionProto.FaceDetectionResponse.newBuilder();

            if (imageString.contains("no-faces")) {
                // No faces
            } else if (imageString.contains("multiple-faces")) {
                responseBuilder.addFaces(createTestFace(10, 20, 100, 100, 0.95f));
                responseBuilder.addFaces(createTestFace(150, 20, 100, 100, 0.9f));
                responseBuilder.addFaces(createTestFace(280, 20, 100, 100, 0.85f));
            } else {
                responseBuilder.addFaces(createTestFace(10, 20, 100, 100, 0.95f));
            }

            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
        }

        private FaceDetectionProto.Face createTestFace(int x, int y, int width, int height, float confidence) {
            return FaceDetectionProto.Face.newBuilder()
                    .setX(x).setY(y)
                    .setWidth(width).setHeight(height)
                    .setConfidence(confidence)
                    .build();
        }
    }
}
