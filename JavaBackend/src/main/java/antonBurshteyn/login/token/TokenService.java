package antonBurshteyn.login.token;

import antonBurshteyn.login.registration.model.User;

public interface TokenService {

    void saveUserToken(User user, String jwtToken);
}
