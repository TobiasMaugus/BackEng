package com.tobias.controleestoquevendas.controller;

import com.tobias.controleestoquevendas.dto.RegisterRequest;
import com.tobias.controleestoquevendas.model.User;
import com.tobias.controleestoquevendas.repository.UserRepository;
import com.tobias.controleestoquevendas.service.TokenService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


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

    private Map<String, String> formatarErros(BindingResult bindingResult) {
        // Retorna um Map onde a chave é o nome do campo e o valor é a mensagem de erro.
        return bindingResult.getFieldErrors().stream()
                .collect(Collectors.toMap(
                        // Chave: Nome do campo com erro (ex: "nome", "cpf", "quantidade")
                        fieldError -> fieldError.getField(),

                        // Valor: Mensagem de erro (ex: "O nome é obrigatório")
                        fieldError -> fieldError.getDefaultMessage(),

                        // Merger (caso um campo tenha múltiplas anotações que falharam):
                        // Mantém a primeira mensagem de erro encontrada.
                        (existing, replacement) -> existing
                ));
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
    public ResponseEntity<?> register(@RequestBody @Valid RegisterRequest req, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(formatarErros(bindingResult));
        }
        // Verifica se o username já existe
        if (userRepository.existsByUsername(req.username)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Erro: nome de usuário já existe");
        }

        // Cria novo usuário
        User user = new User();
        user.setUsername(req.username);
        user.setPassword(passwordEncoder.encode(req.password));
        user.setRole(req.role.toUpperCase());

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

    @GetMapping("/role")
    public ResponseEntity<String> getRoleUsuarioLogado() {

        // 1. Obtém o objeto Authentication do contexto de segurança
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            // Caso o usuário não esteja autenticado (embora o Spring Security deve bloquear)
            return ResponseEntity.status(401).body("Usuário não autenticado.");
        }

        // 2. Obtém as permissões (autoridades) do usuário.
        // No seu caso, a ROLE é a principal autoridade.
        String role = authentication.getAuthorities().stream()
                // Pega a primeira autoridade encontrada (que deve ser a ROLE, ex: GERENTE, VENDEDOR)
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                // Se por algum motivo não tiver, retorna uma string padrão
                .orElse("ROLE_NAO_ENCONTRADA");

        return ResponseEntity.ok(role);
    }
}
