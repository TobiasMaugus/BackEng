package com.tobias.controleestoquevendas.service;

import com.tobias.controleestoquevendas.model.User;
import com.tobias.controleestoquevendas.repository.UserRepository;
import com.tobias.controleestoquevendas.security.CustomUserDetails;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository repository;

    public UserDetailsServiceImpl(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = repository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Retorna o objeto customizado que contém o ID
        return new CustomUserDetails(user);
    }
}
