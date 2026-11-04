package com.biblioteca.service;

import com.biblioteca.dto.LoginRequest;
import com.biblioteca.dto.RegistroRequest;
import com.biblioteca.dto.UsuarioRequest;
import com.biblioteca.model.Usuario;
import com.biblioteca.model.enums.Rol;
import com.biblioteca.dto.LoginResponse;
import com.biblioteca.dto.UsuarioResponse;
import com.biblioteca.exception.RecursoNoEncontradoException;
import com.biblioteca.repository.UsuarioRepository;
import com.biblioteca.security.JwtService;
import com.biblioteca.security.SecurityUtils;
import com.biblioteca.security.UsuarioPrincipal;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final SecurityUtils securityUtils;
    private final UsuarioService usuarioService;

    public AuthService(AuthenticationManager authenticationManager, JwtService jwtService,
                       UsuarioRepository usuarioRepository, SecurityUtils securityUtils,
                       UsuarioService usuarioService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
        this.securityUtils = securityUtils;
        this.usuarioService = usuarioService;
    }

    /**
     * RF-19: un visitante crea su propia cuenta de LECTOR y queda con la sesión iniciada.
     * Nunca se puede crear un BIBLIOTECARIO por esta vía.
     */
    @Transactional
    public LoginResponse registrar(RegistroRequest r) {
        UsuarioRequest datos = new UsuarioRequest(r.nombres(), r.apellidos(), r.documento(), r.correo(),
                r.telefono(), r.direccion(), r.username(), r.password(), Rol.LECTOR);
        Usuario nuevo = usuarioService.crearEntidad(datos);
        String token = jwtService.generarToken(new UsuarioPrincipal(nuevo));
        return new LoginResponse(token, "Bearer", jwtService.getExpiracionMs(), UsuarioResponse.from(nuevo));
    }

    /** RF-13: valida credenciales (usuario o correo + contraseña) y emite un JWT. */
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.login().trim(), request.password()));
        UsuarioPrincipal principal = (UsuarioPrincipal) auth.getPrincipal();
        String token = jwtService.generarToken(principal);
        UsuarioResponse usuario = usuarioRepository.findById(principal.getId())
                .map(UsuarioResponse::from)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", principal.getId()));
        return new LoginResponse(token, "Bearer", jwtService.getExpiracionMs(), usuario);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse perfilActual() {
        Long id = securityUtils.usuarioActual().getId();
        return usuarioRepository.findById(id)
                .map(UsuarioResponse::from)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", id));
    }
}
