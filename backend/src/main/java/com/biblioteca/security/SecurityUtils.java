package com.biblioteca.security;

import com.biblioteca.exception.AccesoDenegadoException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/** Acceso al usuario que hizo la petición actual. */
@Component
public class SecurityUtils {

    public UsuarioPrincipal usuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UsuarioPrincipal principal)) {
            throw new AccesoDenegadoException("No hay una sesión activa");
        }
        return principal;
    }

    public boolean esBibliotecario() {
        return usuarioActual().esBibliotecario();
    }

    /** Un LECTOR solo puede acceder a sus propios datos; el BIBLIOTECARIO a todos. */
    public void verificarPropietario(Long usuarioId) {
        UsuarioPrincipal actual = usuarioActual();
        if (!actual.esBibliotecario() && !actual.getId().equals(usuarioId)) {
            throw new AccesoDenegadoException("Solo puede consultar su propia información");
        }
    }
}
