package antonBurshteyn.login.jwt;

import org.springframework.security.core.userdetails.UserDetails;
import io.jsonwebtoken.Claims;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

public interface JwtService {
    String extractUsername(String token);

    <T> T extractClaim(String token, Function<Claims, T> claimsResolver);

    String generateToken(UserDetails userDetails);

    String generateToken(Map<String, Object> extraClaims, UserDetails userDetails);

    String buildToken(Map<String, Object> extraClaims, UserDetails userDetails);

    Claims extractAllClaims(String token);

    Key getSignInKey();
}
