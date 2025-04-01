package antonBurshteyn.login.helper;

import antonBurshteyn.exception.UserAlreadyExistsException;
import antonBurshteyn.login.auth.RegisterRequest;
import antonBurshteyn.exception.UserValidationExceptions;
import antonBurshteyn.login.registration.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.passay.*;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class ValidateRegisterRequestImpl implements ValidateRegisterRequest{

    private final UserRepository userRepository;

    public void validateFirstName(String firstName) {
        if (firstName == null || firstName.isBlank()) {
            throw UserValidationExceptions.firstName();
        }
    }

    public void validateLastName(String lastName) {
        if (lastName == null || lastName.isBlank()) {
            throw UserValidationExceptions.lastName();
        }
    }

    public void isEmailValid(String email) {
        boolean isValid = email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
        if (!isValid) {
            throw UserValidationExceptions.email(email);
        }
    }

    public void isValidPassword(String password) {
        PasswordValidator validator = new PasswordValidator(Arrays.asList(
                new LengthRule(8, 30),
                new CharacterRule(EnglishCharacterData.UpperCase, 1),
                new CharacterRule(EnglishCharacterData.LowerCase, 1),
                new CharacterRule(EnglishCharacterData.Digit, 1),
                new CharacterRule(EnglishCharacterData.Special, 1),
                new WhitespaceRule()
        ));

        RuleResult result = validator.validate(new PasswordData(password));
        if (!result.isValid()) {
            String errorMessage = String.join(", ", validator.getMessages(result));
            throw UserValidationExceptions.password(errorMessage);
        }
    }

    public void checkCredentials(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new UserAlreadyExistsException("User with email " + registerRequest.getEmail() + " already exists");
        }

        validateFirstName(registerRequest.getFirstName());
        validateLastName(registerRequest.getLastName());
        isEmailValid(registerRequest.getEmail());
        isValidPassword(registerRequest.getPassword());
    }

}
