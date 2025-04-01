import cv2
import numpy as np
import grpc
import sys
import os
import logging
from dotenv import load_dotenv
from concurrent import futures
from generated import face_detection_pb2_grpc, face_detection_pb2

load_dotenv()

logging.basicConfig(level=logging.INFO, format='[gRPC server] %(message)s')
logger = logging.getLogger(__name__)

sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))

class FaceDetectionService(face_detection_pb2_grpc.FaceDetectionServiceServicer):
    def __init__(self):
        self.face_cascade = cv2.CascadeClassifier('model/haarcascade_default.xml')
        logger.info("Model loaded successfully.")

    def DetectFaces(self, request, context):
        try:
            logger.info("Received image for processing.")
            nparr = np.frombuffer(request.image, np.uint8)
            img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
            gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)

            detections = self.face_cascade.detectMultiScale(
                gray, scaleFactor=1.2, minNeighbors=5, minSize=(20, 20)
            )

            response = face_detection_pb2.FaceDetectionResponse()
            for x, y, w, h in detections:
                response.faces.add(x=x, y=y, width=w, height=h, confidence=0.95)

            logger.info(f"{len(response.faces)} face(s) detected.")
            return response
        except Exception as e:
            logger.error(f"Error during face detection: {e}")
            context.set_details(str(e))
            context.set_code(grpc.StatusCode.INTERNAL)
            return face_detection_pb2.FaceDetectionResponse()

def serve():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    face_detection_pb2_grpc.add_FaceDetectionServiceServicer_to_server(
        FaceDetectionService(), server
    )

    host = os.getenv("GRPC_HOST", "0.0.0.0")
    port = os.getenv("GRPC_PORT", "50051")
    address = f"{host}:{port}"

    server.add_insecure_port(address)
    logger.info(f"gRPC server started on {address}")
    server.start()
    server.wait_for_termination()

if __name__ == "__main__":
    serve()
