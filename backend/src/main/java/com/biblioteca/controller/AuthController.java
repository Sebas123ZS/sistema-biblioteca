package com.biblioteca.controller;

import com.biblioteca.dto.LoginRequest;
import com.biblioteca.dto.LoginResponse;
import com.biblioteca.dto.RegistroRequest;
import com.biblioteca.dto.UsuarioResponse;
import com.biblioteca.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** RF-13: iniciar sesión (público). */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /** RF-19: autorregistro de lectores (público). Devuelve el token para entrar directo. */
    @PostMapping("/registro")
    public ResponseEntity<LoginResponse> registro(@Valid @RequestBody RegistroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrar(request));
    }

    /** Datos del usuario dueño del token. */
    @GetMapping("/me")
    public ResponseEntity<UsuarioResponse> me() {
        return ResponseEntity.ok(authService.perfilActual());
    }
}
