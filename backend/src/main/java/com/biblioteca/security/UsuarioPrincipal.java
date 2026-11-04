package com.biblioteca.security;

import com.biblioteca.model.Usuario;
import com.biblioteca.model.enums.EstadoUsuario;
import com.biblioteca.model.enums.Rol;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/** Usuario autenticado que Spring Security guarda en el contexto. */
public class UsuarioPrincipal implements UserDetails {

    private final Long id;
    private final String username;
    private final String password;
    private final Rol rol;
    private final EstadoUsuario estado;

    public UsuarioPrincipal(Usuario u) {
        this.id = u.getId();
        this.username = u.getUsername();
        this.password = u.getPassword();
        this.rol = u.getRol();
        this.estado = u.getEstado();
    }

    public Long getId() { return id; }
    public Rol getRol() { return rol; }
    public boolean esBibliotecario() { return rol == Rol.BIBLIOTECARIO; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol.name()));
    }

    @Override public String getPassword() { return password; }
    @Override public String getUsername() { return username; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }

    /** Un usuario INACTIVO no puede iniciar sesión. */
    @Override
    public boolean isEnabled() {
        return estado != EstadoUsuario.INACTIVO;
    }
}
