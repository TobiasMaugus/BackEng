package com.tobias.controleestoquevendas.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class    User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, name = "nome", nullable = false)
    private String username;

    @Column(nullable = false, name = "senha")
    private String password;

    @Column(nullable = false, name = "role")
    private String role; // GERENTE ou VENDEDOR

    public void setRole(String role) {
        this.role = role.toUpperCase();
    }

}
