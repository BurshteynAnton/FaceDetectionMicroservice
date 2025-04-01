package antonBurshteyn.login.service;

import antonBurshteyn.exception.*;
import antonBurshteyn.login.auth.*;
import antonBurshteyn.login.helper.ValidateRegisterRequest;
import antonBurshteyn.login.jwt.JwtService;
import antonBurshteyn.login.registration.repository.UserRepository;
import antonBurshteyn.enums.Role;
import antonBurshteyn.login.registration.model.User;
import antonBurshteyn.login.token.TokenService;
import antonBurshteyn.util.ServiceUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final ValidateRegisterRequest validateRegisterRequest;
    private final TokenService tokenService;

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationServiceImpl.class);

    @Override
    @Transactional
    public AuthenticationResponse register(RegisterRequest request) {
        String userEmail = request.getEmail();
        Instant start = Instant.now();

        return ServiceUtils.withUserContext(() -> {
            logger.info("Starting registration for user: {}", userEmail);

            validateRegisterRequest.checkCredentials(request);
            try {
                var user = User.builder()
                        .firstname(request.getFirstName())
                        .lastname(request.getLastName())
                        .email(userEmail)
                        .password(passwordEncoder.encode(request.getPassword()))
                        .role(Role.USER)
                        .build();

                var savedUser = userRepository.save(user);
                var jwtToken = jwtService.generateToken(savedUser);
                tokenService.saveUserToken(user, jwtToken);
                ServiceUtils.logDuration(logger, start, "User {} registered", userEmail);
                return AuthenticationResponse.builder().accessToken(jwtToken).build();

            } catch (DataIntegrityViolationException e) {
                throw new DatabaseException(userEmail, e);
            } catch (QueryTimeoutException | CannotAcquireLockException e) {
                throw new DatabaseConnectionException(userEmail, e);
            } catch (Exception e) {
                logger.error("Unexpected error registering user {}", userEmail, e);
                throw new InternalServerErrorException("Registration failed", e);
            }
        }, userEmail);
    }

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        String userEmail = request.getEmail();
        Instant start = Instant.now();

        return ServiceUtils.withUserContext(() -> {
            logger.info("Authenticating user: {}", userEmail);
            try {
                var user = userRepository.findByEmail(userEmail)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userEmail));
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(userEmail, request.getPassword()));

                var jwtToken = jwtService.generateToken(user);
                tokenService.saveUserToken(user, jwtToken);
                ServiceUtils.logDuration(logger, start, "User {} authenticated", userEmail);

                return AuthenticationResponse.builder().accessToken(jwtToken).build();
            } catch (BadCredentialsException | UsernameNotFoundException e) {
                throw e;
            } catch (Exception e) {
                throw new InternalServerErrorException("Authentication failed", e);
            }
        }, userEmail);
    }
}
