package antonBurshteyn.facedetection.repository;
import antonBurshteyn.facedetection.entity.ValidatedPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ValidatedPhotoRepository extends JpaRepository <ValidatedPhoto, Long> {

    boolean existsByName(String name);

    Optional<ValidatedPhoto> findByName(String name);

    @Modifying
    @Query("DELETE FROM ValidatedPhoto p WHERE p.id = :id")
    void deleteByPhotoId(@Param("id") Long id);

}
