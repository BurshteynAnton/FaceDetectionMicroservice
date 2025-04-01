package antonBurshteyn.login.token;

import antonBurshteyn.enums.TokenType;
import antonBurshteyn.login.registration.model.User;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final TokenRepository tokenRepository;
    private static final Logger logger = LoggerFactory.getLogger(TokenServiceImpl.class);

    @Async
    @Override
    public void saveUserToken(User user, String jwtToken) {
        try {
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .build();
        tokenRepository.save(token);
        logger.debug("Token saved for user: {}", user.getEmail());
    } catch (Exception e) {
        logger.error("Failed to save token for user: {}", user.getEmail(), e);
    }
    }
}
