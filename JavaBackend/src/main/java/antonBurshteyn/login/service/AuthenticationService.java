package antonBurshteyn.login.service;

import antonBurshteyn.login.auth.AuthenticationRequest;
import antonBurshteyn.login.auth.AuthenticationResponse;
import antonBurshteyn.login.auth.RegisterRequest;
import antonBurshteyn.login.registration.model.User;

public interface AuthenticationService {

    AuthenticationResponse register(RegisterRequest request);

    AuthenticationResponse authenticate(AuthenticationRequest request);
}
