package antonBurshteyn.configuration;

import antonBurshteyn.login.registration.repository.UserRepository;
import antonBurshteyn.enums.Role;
import antonBurshteyn.login.registration.model.User;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(AdminInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${DEFAULT_ADMIN_PASSWORD}")
    private String adminPassword;


    @Override
    public void run(String... args) {
        String adminEmail = "admin@example.com";

        if (userRepository.existsByEmail(adminEmail)) {
            logger.info("Admin already exists: {}", adminEmail);
            return;
        }

        User admin = User.builder()
                .firstname("Admin")
                .lastname("Account")
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .role(Role.ADMIN)
                .build();

        userRepository.save(admin);
        logger.info("Admin account created: {}", adminEmail);
    }
}
