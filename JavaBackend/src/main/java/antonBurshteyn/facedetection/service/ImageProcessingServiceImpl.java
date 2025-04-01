package antonBurshteyn.facedetection.service;

import antonBurshteyn.exception.*;
import antonBurshteyn.facedetection.repository.*;
import antonBurshteyn.facedetection.grpc.*;
import antonBurshteyn.facedetection.entity.*;
import antonBurshteyn.util.ServiceUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ImageProcessingServiceImpl implements ImageProcessingService {

    private final FaceValidationClient faceValidationClient;
    private final ValidatedPhotoRepository validatedPhotoRepository;
    private final FaceParametersRepository faceParametersRepository;
    private static final Logger logger = LoggerFactory.getLogger(ImageProcessingServiceImpl.class);
    private static final int BUFFER_SIZE = 8192;

    @Override
    public byte[] streamFileData(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream();
             ByteArrayOutputStream photoData = new ByteArrayOutputStream(Math.toIntExact(file.getSize()))) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                photoData.write(buffer, 0, bytesRead);
            }
            return photoData.toByteArray();
        } catch (IOException e) {
            throw new PhotoProcessingException("Error reading photo file", e);
        }
    }

    @Override
    @Transactional
    public void saveValidatedPhoto(byte[] photoData, String name, FaceDetectionProto.Face face) {
        String userEmail = ServiceUtils.getCurrentUserEmail();
        try (MDC.MDCCloseable ignored = MDC.putCloseable("userEmail", userEmail)) {
            try {
                var validatedPhoto = new ValidatedPhoto();
                validatedPhoto.setName(name);
                validatedPhoto.setData(photoData);
                validatedPhoto.setValidatedAt(LocalDateTime.now());
                validatedPhoto = validatedPhotoRepository.save(validatedPhoto);

                var faceParameters = new FaceParameters();
                faceParameters.setX(face.getX());
                faceParameters.setY(face.getY());
                faceParameters.setWidth(face.getWidth());
                faceParameters.setHeight(face.getHeight());
                faceParameters.setConfidence(face.getConfidence());
                faceParameters.setValidatedPhoto(validatedPhoto);

                faceParametersRepository.save(faceParameters);
                logger.info("Photo saved: {}", name);
            } catch (DataIntegrityViolationException e) {
                throw new DatabaseException("A photo with this name already exists: " + name, e);
            } catch (DataAccessException e) {
                throw new DatabaseConnectionException("Database error while saving photo", e);
            }
        }
    }

    @Override
    public boolean isValidFaceDetection(FaceDetectionProto.FaceDetectionResponse response) {
        return response != null && response.getFacesCount() == 1;
    }

    @Override
    public void logInvalidFaceDetection(String name, FaceDetectionProto.FaceDetectionResponse response) {
        String userEmail = ServiceUtils.getCurrentUserEmail();
        try (MDC.MDCCloseable ignored = MDC.putCloseable("userEmail", userEmail)) {
            if (response.getFacesCount() == 0) {
                logger.warn("No faces detected: {}", name);
            } else {
                logger.warn("Multiple faces detected in {}: {} faces", name, response.getFacesCount());
            }
        }
    }

    @Override
    public boolean isValidImage(MultipartFile file) {
        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();
        long maxSize = 5L * 1024 * 1024;

        if (file.getSize() > maxSize) {
            logger.warn("File too large: {} bytes", file.getSize());
            return false;
        }
        if (contentType == null || originalFilename == null) {
            logger.warn("Missing content type or filename");
            return false;
        }

        boolean isValidType = contentType.matches("image/(jpeg|png)");
        boolean isValidExt = originalFilename.toLowerCase().matches(".*\\.(jpg|jpeg|png)");

        if (!isValidType || !isValidExt) {
            logger.warn("Unsupported file type or extension: {}, {}", contentType, originalFilename);
        }

        return isValidType && isValidExt;
    }

    @Override
    @Cacheable(value = "photoValidations", key = "#name")
    public FaceDetectionProto.FaceDetectionResponse validatePhoto(String name, byte[] photoData) {
        try {
            return faceValidationClient.validatePhoto(photoData);
        } catch (Exception e) {
            throw new PhotoProcessingException("Failed to validate photo", e);
        }
    }

    @Override
    @Cacheable(value = "photoExistsCache", key = "#name")
    public boolean doesPhotoExist(String name) {
        try {
            return validatedPhotoRepository.existsByName(name);
        } catch (DataAccessException e) {
            throw new DatabaseConnectionException("Failed to check photo existence", e);
        }
    }
}
