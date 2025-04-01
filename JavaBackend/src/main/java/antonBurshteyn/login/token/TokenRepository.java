package antonBurshteyn.login.token;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, String> {

    @Query("""
    select t from Token t
    where t.user.id = :id and t.expired = false
    """)
    List<Token> findAllValidTokenByUser(String id);


    Optional<Token> findByToken(String token);
}