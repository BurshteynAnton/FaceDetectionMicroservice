package antonBurshteyn.facedetection.service;

import antonBurshteyn.dto.PhotoValidationResponseDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface PhotoService {

    CompletableFuture<PhotoValidationResponseDto> uploadPhotoAsync(MultipartFile file, String name);

    /**
     * Загружает и проверяет фотографию
     *
     * @param file файл фотографии
     * @param name название фотографии
     * @return объект с результатом проверки
     */
    PhotoValidationResponseDto uploadPhoto(MultipartFile file, String name);

    /**
     * Получает фотографию по имени
     *
//     * @param name название фотографии
     * @return данные фотографии в виде массива байтов
     */
    String getPhotoById(Long id);

    /**
     * Получает список всех названий фотографий
     *
     * @return список названий фотографий
     */
    List<String> getAllPhotos();

    /**
     * Удаляет фотографию по идентификатору
     *
     * @param id идентификатор фотографии
     */
    void deletePhotoById(Long id);
}