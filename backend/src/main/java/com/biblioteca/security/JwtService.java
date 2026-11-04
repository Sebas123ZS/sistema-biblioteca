package com.biblioteca.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

/** Genera y valida los tokens JWT (firmados con HMAC-SHA256). */
@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final SecretKey clave;
    private final long expiracionMs;

    /**
     * La clave se lee de la variable JWT_SECRET (Base64, mínimo 256 bits).
     * No se guarda ninguna clave en el código: si no está configurada, se genera
     * una clave aleatoria al iniciar (los tokens emitidos dejan de valer al reiniciar).
     */
    public JwtService(@Value("${app.jwt.secret:}") String secreto,
                      @Value("${app.jwt.expiration-ms}") long expiracionMs) {
        if (secreto == null || secreto.isBlank()) {
            log.warn("JWT_SECRET no está configurado: se usará una clave aleatoria para esta ejecución");
            this.clave = Jwts.SIG.HS256.key().build();
        } else {
            try {
                this.clave = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secreto.trim()));
            } catch (RuntimeException e) {
                throw new IllegalStateException(
                        "JWT_SECRET debe ser un texto Base64 de al menos 32 bytes (ej.: openssl rand -base64 48)", e);
            }
        }
        this.expiracionMs = expiracionMs;
    }

    public String generarToken(UsuarioPrincipal usuario) {
        Date ahora = new Date();
        return Jwts.builder()
                .subject(usuario.getUsername())
                .claim("uid", usuario.getId())
                .claim("rol", usuario.getRol().name())
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + expiracionMs))
                .signWith(clave)
                .compact();
    }

    /** Devuelve el username del token o null si el token es inválido o expiró. */
    public String obtenerUsername(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(clave).build().parseSignedClaims(token).getPayload();
            return claims.getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    public long getExpiracionMs() {
        return expiracionMs;
    }
}
