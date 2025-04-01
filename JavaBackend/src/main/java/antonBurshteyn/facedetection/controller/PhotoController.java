package antonBurshteyn.facedetection.controller;

import antonBurshteyn.dto.PhotoValidationResponseDto;
import antonBurshteyn.facedetection.service.PhotoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/photos")
@Tag(name = "Photo Management", description = "Photo Management API")
public class PhotoController {

    private final PhotoService photoService;

    @Operation(summary = "Upload and validate a photo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Photo uploaded and validated",
                    content = @Content(schema = @Schema(implementation = PhotoValidationResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid file or input", content = @Content),
            @ApiResponse(responseCode = "409", description = "Photo with this name already exists", content = @Content),
            @ApiResponse(responseCode = "500", description = "Photo processing error", content = @Content),
            @ApiResponse(responseCode = "503", description = "Database unavailable", content = @Content)
    })
    @PostMapping("/upload")
    public ResponseEntity<PhotoValidationResponseDto> uploadPhoto(
            @RequestParam("file") MultipartFile file,
            @RequestParam("name") String name) {
        return ResponseEntity.ok(photoService.uploadPhoto(file, name));
    }

    @Operation(summary = "Get a photo by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Photo found",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "Invalid photo ID", content = @Content),
            @ApiResponse(responseCode = "404", description = "Photo not found", content = @Content),
            @ApiResponse(responseCode = "503", description = "Database error", content = @Content)
    })
    @GetMapping("/search/{id}")
//    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> getPhotoById(@PathVariable Long id) {
        return ResponseEntity.ok().body(photoService.getPhotoById(id));
    }

    @Operation(summary = "Get all photos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Photos retrieved successfully",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "204", description = "No photos found", content = @Content),
            @ApiResponse(responseCode = "503", description = "Database error", content = @Content)
    })
    @GetMapping("/list")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<String>> getAllPhotos() {
        return ResponseEntity.ok(photoService.getAllPhotos());
    }

    @Operation(summary = "Delete a photo by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Photo deleted successfully", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid ID", content = @Content),
            @ApiResponse(responseCode = "403", description = "Access denied", content = @Content),
            @ApiResponse(responseCode = "404", description = "Photo not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error deleting photo", content = @Content)
    })
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePhotoById(@PathVariable Long id) {
        photoService.deletePhotoById(id);
        return ResponseEntity.noContent().build();
    }
}
