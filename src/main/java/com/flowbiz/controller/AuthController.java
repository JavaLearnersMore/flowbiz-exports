package com.flowbiz.controller;

import com.flowbiz.model.User;
import com.flowbiz.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository users;
    private final PasswordEncoder encoder;

    public AuthController(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    public record RegisterRequest(
            @NotBlank String name,
            @NotBlank @Email String email,
            String phone,
            String company,
            @NotBlank @Size(min = 6, message = "Password must be at least 6 characters") String password) {}

    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {}

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req, HttpSession session) {
        String email = req.email().trim().toLowerCase();
        if (users.existsByEmail(email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "This email is already registered. Please login."));
        }
        User user = new User();
        user.setName(req.name().trim());
        user.setEmail(email);
        user.setPhone(req.phone());
        user.setCompany(req.company());
        user.setPasswordHash(encoder.encode(req.password()));
        users.save(user); // saved in MySQL -> "users" table

        session.setAttribute("userId", user.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Registration successful", "name", user.getName()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req, HttpSession session) {
        Optional<User> found = users.findByEmail(req.email().trim().toLowerCase());
        if (found.isEmpty() || !encoder.matches(req.password(), found.get().getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid email or password."));
        }
        session.setAttribute("userId", found.get().getId());
        return ResponseEntity.ok(Map.of("message", "Login successful", "name", found.get().getName()));
    }

    @PostMapping("/logout")
    public Map<String, String> logout(HttpSession session) {
        session.invalidate();
        return Map.of("message", "Logged out");
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpSession session) {
        Object id = session.getAttribute("userId");
        if (id == null) {
            return ResponseEntity.ok(Map.of("loggedIn", false));
        }
        return users.findById((Long) id)
                .<ResponseEntity<?>>map(u -> ResponseEntity.ok(Map.of(
                        "loggedIn", true, "name", u.getName(), "email", u.getEmail())))
                .orElseGet(() -> ResponseEntity.ok(Map.of("loggedIn", false)));
    }
}
