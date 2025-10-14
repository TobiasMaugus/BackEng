package com.tobias.controleestoquevendas.service;

import io.jsonwebtoken.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TokenService {

    private static final String SECRET_KEY = "mysecretkeymysecretkeymysecretkeymysecretkeymymymy";
    private static final long EXPIRATION_TIME = 86400000; // 24 hours

    public String generateToken(Authentication authentication) {
        org.springframework.security.core.userdetails.User userPrincipal =
                (org.springframework.security.core.userdetails.User) authentication.getPrincipal();

        // Extrai roles como lista de strings
        List<String> roles = userPrincipal.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .toList();

        return Jwts.builder()
                .setSubject(authentication.getName())
                .claim("roles", roles) // ✅ salva como lista simples
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }


    public String validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token);
            return claims.getBody().getSubject();
        } catch (JwtException e) {
            return null;
        }
    }

    public Authentication getAuthentication(String token) {
        Jws<Claims> claims = Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token);

        String username = claims.getBody().getSubject();

        // ✅ Recupera as roles de forma segura
        Object rolesObj = claims.getBody().get("roles");
        List<String> roles = new ArrayList<>();

        if (rolesObj instanceof List<?>) {
            for (Object role : (List<?>) rolesObj) {
                if (role instanceof String) {
                    roles.add((String) role);
                } else if (role instanceof Map<?, ?> map && map.containsKey("authority")) {
                    roles.add((String) map.get("authority")); // caso venha como LinkedHashMap
                }
            }
        }

        // ✅ Cria authorities a partir das roles
        List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();

        return new UsernamePasswordAuthenticationToken(username, null, authorities);
    }

}
