package antonBurshteyn.facedetection;

import antonBurshteyn.enums.Role;
import antonBurshteyn.login.auth.*;
import antonBurshteyn.login.helper.ValidateRegisterRequest;
import antonBurshteyn.login.jwt.JwtService;
import antonBurshteyn.login.registration.repository.UserRepository;
import antonBurshteyn.login.registration.model.User;
import antonBurshteyn.login.service.AuthenticationServiceImpl;
import antonBurshteyn.login.token.TokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class AuthenticationServiceImplTest {

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private ValidateRegisterRequest validateRegisterRequest;

    @Mock
    private TokenService tokenService;

    @Test
    void shouldRegisterUserSuccessfully() {
        RegisterRequest request = new RegisterRequest("John", "Doe", "john@example.com", "Password123!");
        User user = User.builder()
                .firstname("John")
                .lastname("Doe")
                .email("john@example.com")
                .password("encodedPassword")
                .role(Role.USER)
                .build();

        when(passwordEncoder.encode("Password123!")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");
        AuthenticationResponse response = authenticationService.register(request);
        assertNotNull(response);
        assertEquals("jwt-token", response.getAccessToken());
        verify(validateRegisterRequest).checkCredentials(request);
        verify(tokenService).saveUserToken(user, "jwt-token");
    }

    @Test
    void shouldThrowDatabaseExceptionOnDuplicateRegistration() {
        RegisterRequest request = new RegisterRequest("John", "Doe", "john@example.com", "Password123!");
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate entry"));
        assertThrows(RuntimeException.class, () -> authenticationService.register(request));
    }

    @Test
    void shouldAuthenticateUserSuccessfully() {
        AuthenticationRequest request = new AuthenticationRequest("john@example.com", "Password123!");
        User user = User.builder()
                .email("john@example.com")
                .password("encodedPassword")
                .role(Role.USER)
                .build();

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwt-token");
        AuthenticationResponse response = authenticationService.authenticate(request);
        assertNotNull(response);
        assertEquals("jwt-token", response.getAccessToken());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenService).saveUserToken(user, "jwt-token");
    }

    @Test
    void shouldThrowExceptionIfUserNotFoundDuringAuthentication() {
        AuthenticationRequest request = new AuthenticationRequest("missing@example.com", "Password123!");
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> authenticationService.authenticate(request));
    }

    @Test
    void shouldThrowExceptionIfBadCredentialsDuringAuthentication() {
        AuthenticationRequest request = new AuthenticationRequest("john@example.com", "wrongPassword");
        User user = User.builder()
                .email("john@example.com")
                .password("encodedPassword")
                .role(Role.USER)
                .build();

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        doThrow(BadCredentialsException.class)
                .when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        assertThrows(BadCredentialsException.class, () -> authenticationService.authenticate(request));
    }
}
