package medplan.controller;

import medplan.entity.User;
import medplan.repository.UserRepository;
import medplan.security.JwtService;
import medplan.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

 private final AuthService authService;
public AuthController(AuthService authService) {
    this.authService = authService;
}

    public record RegisterRequest(
        String fullName,
        String email,
        String password
    ) {}

    public record LoginRequest(
        String email,
        String password
    ) {}

    public record AuthResponse(
        Long id,
        String fullName,
        String email,
        String role,
        String token
    ) {}

    @PostMapping("/register")
public AuthResponse register(@RequestBody RegisterRequest request) {
    User saved = authService.register(
        request.fullName(), request.email(), request.password());

    return new AuthResponse(
        saved.getId(),
        saved.getFullName(),
        saved.getEmail(),
        saved.getRole().name(),
        authService.generateToken(saved.getEmail())
    );
}

@PostMapping("/login")
public AuthResponse login(@RequestBody LoginRequest request) {
    User user = authService.login(request.email(), request.password());

    return new AuthResponse(
        user.getId(),
        user.getFullName(),
        user.getEmail(),
        user.getRole().name(),
        authService.generateToken(user.getEmail())
    );
}
}





