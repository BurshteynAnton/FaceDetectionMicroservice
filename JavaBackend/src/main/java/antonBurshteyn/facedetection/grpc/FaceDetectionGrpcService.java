//package antonBurshteyn.facedetection.grpc;
//
//import antonBurshteyn.facedetection.grpc.FaceDetectionProto.ImageRequest;
//import antonBurshteyn.facedetection.grpc.FaceDetectionProto.FaceDetectionResponse;
//import io.grpc.stub.StreamObserver;
//import net.devh.boot.grpc.server.service.GrpcService;
//
//@GrpcService
//public class FaceDetectionGrpcService extends FaceDetectionServiceGrpc.FaceDetectionServiceImplBase {
//
//    @Override
//    public void detectFaces(ImageRequest request, StreamObserver<FaceDetectionResponse> responseObserver) {
//        try {
//            boolean isValid = request.getImage().size() > 100;
//            FaceDetectionResponse.Builder responseBuilder = FaceDetectionResponse.newBuilder();
//            if (isValid) {
//                responseBuilder.addFaces(
//                        FaceDetectionProto.Face.newBuilder()
//                                .setX(10)
//                                .setY(20)
//                                .setWidth(100)
//                                .setHeight(200)
//                                .setConfidence(0.95f)
//                                .build()
//                );
//            }
//
//            responseObserver.onNext(responseBuilder.build());
//            responseObserver.onCompleted();
//        } catch (Exception e) {
//            responseObserver.onError(e);
//        }
//    }
//}