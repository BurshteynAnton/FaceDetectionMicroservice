package antonBurshteyn.facedetection.service;

import antonBurshteyn.dto.PhotoValidationResponseDto;
import antonBurshteyn.exception.*;
import antonBurshteyn.facedetection.repository.ValidatedPhotoRepository;
import antonBurshteyn.enums.PhotoValidationStatus;
import antonBurshteyn.facedetection.entity.ValidatedPhoto;
import antonBurshteyn.facedetection.grpc.FaceDetectionProto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.Query;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class PhotoServiceImpl implements PhotoService {

    private final ValidatedPhotoRepository validatedPhotoRepository;
    private final ImageProcessingService imageProcessingService;
    private static final Logger logger = LoggerFactory.getLogger(PhotoServiceImpl.class);

    @Async
    public CompletableFuture<PhotoValidationResponseDto> uploadPhotoAsync(MultipartFile file, String name) {
        return CompletableFuture.supplyAsync(() -> uploadPhoto(file, name));
    }

    @Override
    @Transactional
    public PhotoValidationResponseDto uploadPhoto(MultipartFile file, String name) {
        if (file == null || name == null || name.isBlank()) {
            throw new BadRequestException("Invalid input: file or name is missing");
        }

        byte[] photoData = readPhotoFile(file);
        FaceDetectionProto.FaceDetectionResponse response = imageProcessingService.validatePhoto(name, photoData);

        int faceCount = response.getFacesCount();
        if (faceCount != 1) {
            imageProcessingService.logInvalidFaceDetection(name, response);
            throw new InvalidFaceCountException(
                    faceCount == 0
                            ? "No faces detected in the photo"
                            : "Multiple faces detected in the photo: " + faceCount
            );
        }

        imageProcessingService.saveValidatedPhoto(photoData, name, response.getFaces(0));
        return new PhotoValidationResponseDto(name, PhotoValidationStatus.SUCCESS, "Photo validated and saved successfully");
    }


    private byte[] readPhotoFile(MultipartFile file) {
        try {
            return imageProcessingService.streamFileData(file);
        } catch (IOException e) {
            String filename = file != null ? file.getOriginalFilename() : "unknown";
            logger.error("Failed to read photo file: {}", filename, e);
            throw new PhotoProcessingException("Error reading the photo file: " + filename, e);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public String getPhotoById(Long id) {
        return validatedPhotoRepository.findById(id)
                .map(ValidatedPhoto::getName)
                .orElseThrow(() -> new PhotoNotFoundException("Photo not found with id: " + id));
    }

    @Transactional(readOnly = true)
    @Query("SELECT p.name FROM ValidatedPhoto p ORDER BY p.id DESC LIMIT :limit")
    @Override
    public List<String> getAllPhotos() {
        List<String> photos = validatedPhotoRepository.findAll()
                .stream()
                .map(ValidatedPhoto::getName)
                .toList();

        if (photos.isEmpty()) {
            throw new NoPhotosFoundException("No photos found in the system");
        }
        return photos;
    }

    @Transactional
    @Override
    public void deletePhotoById(Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getAuthorities().stream()
                .noneMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"))) {
            throw new AccessDeniedException("Access denied: Only admins can delete photos");
        }
        if (id == null || id < 0) {
            throw new InvalidPhotoIdException("Photo ID must be a positive number");
        }
        validatedPhotoRepository.deleteById(id);
    }
}




