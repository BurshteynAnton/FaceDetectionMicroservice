package antonBurshteyn.facedetection.repository;

import antonBurshteyn.facedetection.entity.FaceParameters;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FaceParametersRepository extends JpaRepository<FaceParameters, Long> {
}
