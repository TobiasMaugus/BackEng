package com.tobias.controleestoquevendas.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank(message = "O nome de usuário é obrigatório.")
    @Size(min = 3, message = "O username deve ter pelo menos 3 caracteres")
    public String username;

    @NotBlank(message = "A senha de usuário é obrigatória.")
    @Size(min = 3, message = "A senha deve ter pelo menos 3 caracteres")
    public String password;

    @Pattern(regexp = "^(?i)(VENDEDOR|GERENTE)$",
            message = "A role deve ser 'VENDEDOR' ou 'GERENTE'.")
    @NotBlank(message = "A role de usuário é obrigatória.")
    public String role;
}
