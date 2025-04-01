package antonBurshteyn.login.registration.repository;

import antonBurshteyn.login.registration.model.User;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public interface UserRepository extends JpaRepository <User, Integer> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
