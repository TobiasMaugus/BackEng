package com.tobias.controleestoquevendas.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {

        // Define o status HTTP 403 Forbidden
        response.setStatus(HttpStatus.FORBIDDEN.value());

        // Define o tipo de conteúdo da resposta como JSON
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        // Cria e escreve o corpo da resposta JSON customizada
        String jsonResponse = String.format(
                "{\"status\": 403, \"error\": \"Forbidden\", \"message\": \"Você não possui a permissão necessária (ROLE) para acessar este recurso. Ação: %s %s\"}",
                request.getMethod(),
                request.getRequestURI()
        );

        response.getWriter().write(jsonResponse);
    }
}