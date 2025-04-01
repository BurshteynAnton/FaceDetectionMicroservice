package antonBurshteyn.facedetection;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import antonBurshteyn.dto.PhotoValidationResponseDto;
import antonBurshteyn.exception.GlobalExceptionHandler;
import antonBurshteyn.exception.PhotoNotFoundException;
import antonBurshteyn.facedetection.controller.PhotoController;
import antonBurshteyn.facedetection.service.PhotoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

class PhotoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PhotoService photoService;

    @InjectMocks
    private PhotoController photoController;

    @BeforeEach
    void setup() {
        photoService = mock(PhotoService.class);
        photoController = new PhotoController(photoService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(photoController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldUploadPhotoSuccessfully() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "mock-data".getBytes());
        PhotoValidationResponseDto responseDto = new PhotoValidationResponseDto("Test", null, "Uploaded");
        when(photoService.uploadPhoto(any(), eq("Test"))).thenReturn(responseDto);

        mockMvc.perform(multipart("/photos/upload")
                        .file(file)
                        .param("name", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test"))
                .andExpect(jsonPath("$.message").value("Uploaded"));
    }

    @Test
    @DisplayName("🔍 Get photo by ID")
    void shouldGetPhotoById() throws Exception {
        when(photoService.getPhotoById(1L)).thenReturn("photo1.jpg");

        mockMvc.perform(get("/photos/search/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("photo1.jpg"));
    }

    @Test
    void shouldGetAllPhotos() throws Exception {
        when(photoService.getAllPhotos()).thenReturn(List.of("one.jpg", "two.jpg"));

        mockMvc.perform(get("/photos/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("one.jpg"))
                .andExpect(jsonPath("$[1]").value("two.jpg"))
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldDeletePhoto() throws Exception {
        mockMvc.perform(delete("/photos/delete/1"))
                .andExpect(status().isNoContent());

        verify(photoService).deletePhotoById(1L);
    }

    @Test
    void shouldReturnBadRequestWhenFileIsInvalid() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[0]);

        when(photoService.uploadPhoto(any(), eq("Empty")))
                .thenThrow(new IllegalArgumentException("File cannot be empty"));

        mockMvc.perform(multipart("/photos/upload")
                        .file(emptyFile)
                        .param("name", "Empty"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnNotFoundWhenPhotoDoesNotExist() throws Exception {
        when(photoService.getPhotoById(99L))
                .thenThrow(new PhotoNotFoundException("Photo not found"));

        mockMvc.perform(get("/photos/search/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnEmptyListWhenNoPhotosExist() throws Exception {
        when(photoService.getAllPhotos()).thenReturn(List.of());

        mockMvc.perform(get("/photos/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}
