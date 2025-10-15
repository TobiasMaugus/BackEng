package com.tobias.controleestoquevendas.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        // Define o status HTTP 401 Unauthorized
        response.setStatus(HttpStatus.UNAUTHORIZED.value());

        // Define o tipo de conteúdo da resposta como JSON
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        // Cria e escreve o corpo da resposta JSON customizada
        String jsonResponse = String.format(
                "{\"status\": 401, \"error\": \"Unauthorized\", \"message\": \"Acesso negado. Você precisa se autenticar (fazer login) para acessar o recurso: %s %s\"}",
                request.getMethod(),
                request.getRequestURI()
        );

        response.getWriter().write(jsonResponse);
    }
}