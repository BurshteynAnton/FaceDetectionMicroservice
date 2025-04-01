package antonBurshteyn.facedetection;

import antonBurshteyn.exception.*;
import antonBurshteyn.login.auth.*;
import antonBurshteyn.login.controller.AuthenticationController;
import antonBurshteyn.login.service.AuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthenticationControllerTest {

    private MockMvc mockMvc;
    private AuthenticationService authService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        authService = mock(AuthenticationService.class);
        AuthenticationController controller = new AuthenticationController(authService);
        objectMapper = new ObjectMapper();

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldRegisterUserSuccessfully() throws Exception {
        RegisterRequest request = new RegisterRequest("Anton", "Burshteyn", "anton@example.com", "StrongPass123!");
        AuthenticationResponse response = AuthenticationResponse.builder()
                .accessToken("mock-token")
                .build();
        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.access_token").value("mock-token"));
    }

    @Test
    void shouldReturnConflictWhenUserAlreadyExists() throws Exception {
        RegisterRequest request = new RegisterRequest("Anton", "Burshteyn", "anton@example.com", "StrongPass123!");

        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new UserAlreadyExistsException("User already exists"));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturnBadRequestWhenValidationFails() throws Exception {
        RegisterRequest request = new RegisterRequest("", "Burshteyn", "invalid-email", "123");

        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(UserValidationExceptions.firstName());

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldAuthenticateUserSuccessfully() throws Exception {
        AuthenticationRequest request = new AuthenticationRequest("anton@example.com", "StrongPass123!");
        AuthenticationResponse response = AuthenticationResponse.builder()
                .accessToken("mock-token")
                .build();
        when(authService.authenticate(any(AuthenticationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value("mock-token"));
    }

    @Test
    void shouldReturnNotFoundWhenUserDoesNotExist() throws Exception {
        AuthenticationRequest request = new AuthenticationRequest("unknown@example.com", "password");

        when(authService.authenticate(any(AuthenticationRequest.class)))
                .thenThrow(new UsernameNotFoundException("User not found"));

        mockMvc.perform(post("/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnUnauthorizedWhenCredentialsAreInvalid() throws Exception {
        AuthenticationRequest request = new AuthenticationRequest("anton@example.com", "wrong");

        when(authService.authenticate(any(AuthenticationRequest.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
