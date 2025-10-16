package com.tobias.controleestoquevendas.controller;

import com.tobias.controleestoquevendas.dto.RegisterRequest;
import com.tobias.controleestoquevendas.model.User;
import com.tobias.controleestoquevendas.repository.UserRepository;
import com.tobias.controleestoquevendas.service.TokenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


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
    public ResponseEntity<?> login(@RequestBody User loginRequest) {

        try {
            // Tenta autenticar
            var authentication = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );

            // Se bem-sucedido, gera o token (lógica do tokenService não é alterada)
            var token = tokenService.generateToken(authentication);

            // Retorna o token com status 200 OK
            return ResponseEntity.ok(token);

        } catch (BadCredentialsException e) {
            // Captura a exceção quando username ou senha estão incorretos

            // Retorna 401 Unauthorized com uma mensagem de erro clara em formato JSON
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Credenciais inválidas. Verifique o nome de usuário e a senha."));

        } catch (Exception e) {
            // Captura outras possíveis exceções (ex: usuário não encontrado, se não for tratado no service)
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Falha na autenticação: " + e.getMessage()));
        }
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
