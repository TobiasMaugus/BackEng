package com.tobias.controleestoquevendas.controller;

import com.tobias.controleestoquevendas.dto.RegisterRequest;
import com.tobias.controleestoquevendas.model.User;
import com.tobias.controleestoquevendas.repository.UserRepository;
import com.tobias.controleestoquevendas.service.TokenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authManager, TokenService tokenService, UserRepository userRepository,
                          BCryptPasswordEncoder passwordEncoder) {
        this.authManager = authManager;
        this.tokenService = tokenService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody User loginRequest) {
        var authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );
        var token = tokenService.generateToken(authentication);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest req) {
        // Verifica se o username já existe
        if (userRepository.existsByUsername(req.username)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Erro: nome de usuário já existe");
        }

        // Cria novo usuário
        User user = new User();
        user.setUsername(req.username);
        user.setPassword(passwordEncoder.encode(req.password));
        user.setRole(req.role != null ? req.role.toUpperCase() : "VENDEDOR");

        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Usuário registrado com sucesso");
    }




    @GetMapping("/users")
    public List<String> listUsersRoles() {
        return userRepository.findAll()
                .stream()
                .map(u -> u.getUsername() + " -> " + u.getRole())
                .toList();
    }

}
