package antonBurshteyn.facedetection.grpc;

import antonBurshteyn.exception.*;
import antonBurshteyn.facedetection.grpc.FaceDetectionProto.ImageRequest;
import antonBurshteyn.facedetection.grpc.FaceDetectionProto.FaceDetectionResponse;
import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class FaceValidationClient {

    private static final Logger logger = LoggerFactory.getLogger(FaceValidationClient.class);

    @Value("${GRPC_SERVER_HOST}")
    private String host;

    @Value("${GRPC_SERVER_PORT}")
    private int port;

    private ManagedChannel channel;
    private FaceDetectionServiceGrpc.FaceDetectionServiceBlockingStub stub;

    @PostConstruct
    public void init() {
        channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .keepAliveTime(30, TimeUnit.SECONDS)
                .enableRetry()
                .maxRetryAttempts(2)
                .build();

        stub = FaceDetectionServiceGrpc.newBlockingStub(channel);
        logger.info("gRPC client initialized for {}:{}", host, port);
    }

    public FaceDetectionResponse validatePhoto(byte[] imageData) {
        try {
            logger.debug("Sending image for face detection, size: {} bytes", imageData.length);

            ImageRequest request = ImageRequest.newBuilder()
                    .setImage(ByteString.copyFrom(imageData))
                    .build();

            FaceDetectionResponse response = stub
                    .withDeadlineAfter(5, TimeUnit.SECONDS)
                    .detectFaces(request);

            logger.debug("Face detection completed. Found {} faces", response.getFacesCount());
            return response;

        } catch (StatusRuntimeException e) {
            logger.error("Face detection failed: {}", e.getStatus().getCode(), e);
            throw new FaceValidationException("Face validation service error: " + e.getStatus().getCode(), e);
        }
    }

    @PreDestroy
    public void shutdown() {
        if (channel != null && !channel.isShutdown()) {
            channel.shutdown();
        }
    }
}
