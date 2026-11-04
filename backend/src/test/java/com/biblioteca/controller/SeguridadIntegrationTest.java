package com.biblioteca.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba de integración: levanta toda la aplicación con H2 y los datos de ejemplo,
 * y verifica autenticación JWT y permisos por rol.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SeguridadIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    private String token(String usuario, String clave) throws Exception {
        String json = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"" + usuario + "\",\"password\":\"" + clave + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode nodo = mapper.readTree(json);
        return nodo.get("token").asText();
    }

    @Test
    @DisplayName("RF-13: login correcto devuelve un JWT y el rol")
    void loginCorrecto() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"admin\",\"password\":\"Admin123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.usuario.rol").value("BIBLIOTECARIO"));
    }

    @Test
    @DisplayName("Login con contraseña incorrecta responde 401")
    void loginIncorrecto() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"admin\",\"password\":\"otra\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Usuario o contraseña incorrectos"));
    }

    @Test
    @DisplayName("Sin token no se puede consultar el catálogo (401)")
    void sinToken() throws Exception {
        mvc.perform(get("/api/libros")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("RF-03: con token se consulta el catálogo")
    void catalogoConToken() throws Exception {
        mvc.perform(get("/api/libros").header("Authorization", "Bearer " + token("jperez", "Lector123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").isNumber());
    }

    @Test
    @DisplayName("RF-14: un LECTOR no puede registrar libros (403)")
    void lectorNoRegistraLibros() throws Exception {
        mvc.perform(post("/api/libros")
                        .header("Authorization", "Bearer " + token("jperez", "Lector123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isbn\":\"978-1234567890\",\"titulo\":\"Prueba\",\"categoriaId\":1,"
                                + "\"autorIds\":[1],\"cantidadTotal\":1}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Validación: un libro sin datos responde 400 con el detalle por campo")
    void validacionLibro() throws Exception {
        mvc.perform(post("/api/libros")
                        .header("Authorization", "Bearer " + token("admin", "Admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.titulo").exists());
    }

    @Test
    @DisplayName("RF-16: el panel solo lo ve el bibliotecario")
    void dashboardSoloBibliotecario() throws Exception {
        mvc.perform(get("/api/dashboard").header("Authorization", "Bearer " + token("admin", "Admin123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTitulos").value(15));
        mvc.perform(get("/api/dashboard").header("Authorization", "Bearer " + token("jperez", "Lector123")))
                .andExpect(status().isForbidden());
    }
    @Test
    @DisplayName("RF-19: un visitante se registra como LECTOR y recibe su token")
    void registroDeLector() throws Exception {
        String json = "{\"nombres\":\"Rosa\",\"apellidos\":\"Quispe\",\"documento\":\"72345678\","
                + "\"correo\":\"rosa.quispe@correo.com\",\"username\":\"rquispe\",\"password\":\"Lector123\"}";
        mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.usuario.rol").value("LECTOR"));
        // El mismo documento/usuario no se puede registrar dos veces
        mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isConflict());
    }
}
