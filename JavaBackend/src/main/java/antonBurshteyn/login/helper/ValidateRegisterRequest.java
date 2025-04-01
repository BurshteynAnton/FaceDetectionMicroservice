package antonBurshteyn.login.helper;

import antonBurshteyn.login.auth.RegisterRequest;

public interface ValidateRegisterRequest {
    void validateFirstName(String firstName);

    void validateLastName(String lastName);

    void isEmailValid(String email);

    void isValidPassword(String password);

    void checkCredentials(RegisterRequest registerRequest);
}