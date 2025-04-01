package antonBurshteyn.facedetection;

import antonBurshteyn.dto.PhotoValidationResponseDto;
import antonBurshteyn.exception.*;
import antonBurshteyn.facedetection.repository.ValidatedPhotoRepository;
import antonBurshteyn.facedetection.grpc.FaceDetectionProto;
import antonBurshteyn.facedetection.entity.ValidatedPhoto;
import antonBurshteyn.facedetection.service.ImageProcessingService;
import antonBurshteyn.facedetection.service.PhotoServiceImpl;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PhotoServiceImplTest {

    @Mock
    private ValidatedPhotoRepository validatedPhotoRepository;
    @Mock
    private ImageProcessingService imageProcessingService;
    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private PhotoServiceImpl photoService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldUploadPhotoSuccessfully() throws IOException {
        byte[] photoData = "image".getBytes();
        String name = "photo.jpg";

        when(multipartFile.getOriginalFilename()).thenReturn(name);
        when(imageProcessingService.streamFileData(multipartFile)).thenReturn(photoData);

        var face = FaceDetectionProto.Face.newBuilder().setX(1).setY(1).setWidth(1).setHeight(1).setConfidence(0.9f).build();
        var response = FaceDetectionProto.FaceDetectionResponse.newBuilder().addFaces(face).build();

        when(imageProcessingService.validatePhoto(name, photoData)).thenReturn(response);

        PhotoValidationResponseDto result = photoService.uploadPhoto(multipartFile, name);

        assertEquals("photo.jpg", result.getName());
        assertEquals("Photo validated and saved successfully", result.getMessage());
    }

    @Test
    void shouldThrowBadRequestWhenNameIsBlank() {
        assertThrows(BadRequestException.class, () -> photoService.uploadPhoto(multipartFile, "  "));
    }

    @Test
    void shouldThrowInvalidFaceCountWhenZeroFaces() throws IOException {
        when(multipartFile.getOriginalFilename()).thenReturn("photo.jpg");
        when(imageProcessingService.streamFileData(multipartFile)).thenReturn("img".getBytes());

        var response = FaceDetectionProto.FaceDetectionResponse.newBuilder().build();
        when(imageProcessingService.validatePhoto(any(), any())).thenReturn(response);

        assertThrows(InvalidFaceCountException.class, () -> photoService.uploadPhoto(multipartFile, "photo.jpg"));
    }

    @Test
    void shouldThrowInvalidFaceCountWhenMultipleFaces() throws IOException {
        var face1 = FaceDetectionProto.Face.newBuilder().build();
        var face2 = FaceDetectionProto.Face.newBuilder().build();

        when(multipartFile.getOriginalFilename()).thenReturn("photo.jpg");
        when(imageProcessingService.streamFileData(multipartFile)).thenReturn("img".getBytes());
        when(imageProcessingService.validatePhoto(any(), any()))
                .thenReturn(FaceDetectionProto.FaceDetectionResponse.newBuilder()
                        .addFaces(face1).addFaces(face2).build());

        assertThrows(InvalidFaceCountException.class, () -> photoService.uploadPhoto(multipartFile, "photo.jpg"));
    }

    @Test
    void shouldThrowPhotoProcessingExceptionOnIOException() throws IOException {
        when(multipartFile.getOriginalFilename()).thenReturn("photo.jpg");
        when(imageProcessingService.streamFileData(multipartFile))
                .thenThrow(new IOException("IO failed"));

        assertThrows(PhotoProcessingException.class, () -> photoService.uploadPhoto(multipartFile, "photo.jpg"));
    }

    @Test
    void shouldGetPhotoByIdSuccessfully() {
        ValidatedPhoto photo = new ValidatedPhoto();
        photo.setName("photo.jpg");

        when(validatedPhotoRepository.findById(1L)).thenReturn(Optional.of(photo));

        assertEquals("photo.jpg", photoService.getPhotoById(1L));
    }

    @Test
    void shouldThrowPhotoNotFoundExceptionWhenPhotoMissing() {
        when(validatedPhotoRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(PhotoNotFoundException.class, () -> photoService.getPhotoById(1L));
    }

    @Test
    void shouldGetAllPhotosSuccessfully() {
        ValidatedPhoto photo = new ValidatedPhoto();
        photo.setName("photo.jpg");

        when(validatedPhotoRepository.findAll()).thenReturn(List.of(photo));

        List<String> result = photoService.getAllPhotos();
        assertEquals(List.of("photo.jpg"), result);
    }

    @Test
    void shouldThrowNoPhotosFoundExceptionWhenListIsEmpty() {
        when(validatedPhotoRepository.findAll()).thenReturn(Collections.emptyList());
        assertThrows(NoPhotosFoundException.class, () -> photoService.getAllPhotos());
    }

    @Test
    void shouldDeletePhotoByIdWithAdminRole() {
        Authentication auth = mock(Authentication.class);
        when(auth.getAuthorities()).thenReturn((Collection) List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertDoesNotThrow(() -> photoService.deletePhotoById(1L));
        verify(validatedPhotoRepository).deleteById(1L);
    }

    @Test
    void shouldThrowAccessDeniedWhenNoRolePresent() {
        Authentication auth = mock(Authentication.class);
        when(auth.getAuthorities()).thenReturn(Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThrows(AccessDeniedException.class, () -> photoService.deletePhotoById(1L));
    }

    @Test
    void shouldThrowInvalidPhotoIdExceptionWhenIdIsNegative() {
        Authentication auth = mock(Authentication.class);
        when(auth.getAuthorities()).thenReturn((Collection) List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThrows(InvalidPhotoIdException.class, () -> photoService.deletePhotoById(-5L));
    }
}
