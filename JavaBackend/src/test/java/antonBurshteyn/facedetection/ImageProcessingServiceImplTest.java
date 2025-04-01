package antonBurshteyn.facedetection;

import antonBurshteyn.exception.*;
import antonBurshteyn.facedetection.repository.FaceParametersRepository;
import antonBurshteyn.facedetection.repository.ValidatedPhotoRepository;
import antonBurshteyn.facedetection.grpc.FaceDetectionProto;
import antonBurshteyn.facedetection.grpc.FaceValidationClient;
import antonBurshteyn.facedetection.entity.ValidatedPhoto;
import antonBurshteyn.facedetection.service.ImageProcessingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ImageProcessingServiceImplTest {

    @Mock
    private FaceValidationClient faceValidationClient;
    @Mock
    private ValidatedPhotoRepository validatedPhotoRepository;
    @Mock
    private FaceParametersRepository faceParametersRepository;
    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private ImageProcessingServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldStreamFileDataSuccessfully() throws Exception {
        byte[] expected = "image-data".getBytes();
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(expected));
        when(multipartFile.getSize()).thenReturn((long) expected.length);

        byte[] actual = service.streamFileData(multipartFile);

        assertArrayEquals(expected, actual);
    }

    @Test
    void shouldThrowPhotoProcessingExceptionWhenStreamingFails() throws Exception {
        when(multipartFile.getInputStream()).thenThrow(new IOException("Failed"));
        assertThrows(PhotoProcessingException.class, () -> service.streamFileData(multipartFile));
    }

    @Test
    void shouldSaveValidatedPhotoSuccessfully() {
        byte[] data = "data".getBytes();
        String name = "photo1";
        FaceDetectionProto.Face face = FaceDetectionProto.Face.newBuilder()
                .setX(1).setY(2).setWidth(3).setHeight(4).setConfidence(0.9f).build();

        when(validatedPhotoRepository.save(any(ValidatedPhoto.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> service.saveValidatedPhoto(data, name, face));
        verify(faceParametersRepository).save(any());
    }

    @Test
    void shouldThrowDatabaseExceptionWhenPhotoSaveFailsDueToConflict() {
        when(validatedPhotoRepository.save(any()))
                .thenThrow(new DataIntegrityViolationException("Conflict"));

        assertThrows(DatabaseException.class, () -> service.saveValidatedPhoto("data".getBytes(), "conflict", FaceDetectionProto.Face.getDefaultInstance()));
    }

    @Test
    void shouldThrowDatabaseConnectionExceptionWhenSaveFailsDueToConnection() {
        when(validatedPhotoRepository.save(any()))
                .thenThrow(new DataAccessException("DB error") {});

        assertThrows(DatabaseConnectionException.class, () -> service.saveValidatedPhoto("data".getBytes(), "db", FaceDetectionProto.Face.getDefaultInstance()));
    }

    @Test
    void shouldReturnTrueWhenValidFaceDetected() {
        FaceDetectionProto.FaceDetectionResponse response =
                FaceDetectionProto.FaceDetectionResponse.newBuilder()
                        .addFaces(FaceDetectionProto.Face.newBuilder().build())
                        .build();

        assertTrue(service.isValidFaceDetection(response));
    }

    @Test
    void shouldReturnFalseWhenNoFacesDetected() {
        FaceDetectionProto.FaceDetectionResponse response =
                FaceDetectionProto.FaceDetectionResponse.newBuilder().build();

        assertFalse(service.isValidFaceDetection(response));
    }

    @Test
    void shouldReturnTrueForValidImage() {
        when(multipartFile.getContentType()).thenReturn("image/jpeg");
        when(multipartFile.getOriginalFilename()).thenReturn("photo.jpg");
        when(multipartFile.getSize()).thenReturn(1024L);

        assertTrue(service.isValidImage(multipartFile));
    }

    @Test
    void shouldReturnFalseForInvalidMimeType() {
        when(multipartFile.getContentType()).thenReturn("text/plain");
        when(multipartFile.getOriginalFilename()).thenReturn("photo.jpg");
        when(multipartFile.getSize()).thenReturn(1024L);

        assertFalse(service.isValidImage(multipartFile));
    }

    @Test
    void shouldValidatePhotoSuccessfully() {
        byte[] data = "data".getBytes();
        FaceDetectionProto.FaceDetectionResponse response = FaceDetectionProto.FaceDetectionResponse.newBuilder().build();

        when(faceValidationClient.validatePhoto(data)).thenReturn(response);

        FaceDetectionProto.FaceDetectionResponse result = service.validatePhoto("name", data);

        assertEquals(response, result);
    }

    @Test
    void shouldThrowPhotoProcessingExceptionWhenValidationFails() {
        when(faceValidationClient.validatePhoto(any())).thenThrow(RuntimeException.class);
        assertThrows(PhotoProcessingException.class, () -> service.validatePhoto("name", "data".getBytes()));
    }

    @Test
    void shouldReturnTrueIfPhotoExists() {
        when(validatedPhotoRepository.existsByName("photo")).thenReturn(true);
        assertTrue(service.doesPhotoExist("photo"));
    }

    @Test
    void shouldThrowDatabaseConnectionExceptionWhenPhotoExistCheckFails() {
        when(validatedPhotoRepository.existsByName("photo"))
                .thenThrow(new DataAccessException("Error") {});
        assertThrows(DatabaseConnectionException.class, () -> service.doesPhotoExist("photo"));
    }
}
