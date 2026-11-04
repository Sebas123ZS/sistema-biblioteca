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

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Fase 5 (integración con base de datos): pruebas que recorren la API completa
 * sobre la base H2 con los datos de ejemplo. Verifican el CRUD, las consultas JPQL
 * y que las transacciones se reviertan cuando una regla de negocio falla.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PersistenciaIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    private String token() throws Exception {
        String json = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"admin\",\"password\":\"Admin123\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + mapper.readTree(json).get("token").asText();
    }

    private JsonNode getJson(String url, String token) throws Exception {
        String json = mvc.perform(get(url).header("Authorization", token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(json);
    }

    @Test
    @DisplayName("CRUD completo de categorías: crear (201), consultar, actualizar (200) y eliminar (204)")
    void crudCategoria() throws Exception {
        String token = token();

        // Create
        String creada = mvc.perform(post("/api/categorias").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Poesia de prueba\",\"descripcion\":\"Creada por la prueba\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.cantidadLibros").value(0))
                .andReturn().getResponse().getContentAsString();
        long id = mapper.readTree(creada).get("id").asLong();

        // Read
        mvc.perform(get("/api/categorias").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == %d)].nombre", id).value("Poesia de prueba"));

        // Update
        mvc.perform(put("/api/categorias/" + id).header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Poesia actualizada\",\"descripcion\":\"Editada\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Poesia actualizada"));

        // Delete
        mvc.perform(delete("/api/categorias/" + id).header("Authorization", token))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/categorias").header("Authorization", token))
                .andExpect(jsonPath("$[?(@.id == %d)]", id).isEmpty());
    }

    @Test
    @DisplayName("JPQL: cada categoría informa cuántos títulos activos tiene (las 10 de ejemplo tienen libros)")
    void conteoDeLibrosPorCategoria() throws Exception {
        JsonNode categorias = getJson("/api/categorias", token());
        long conLibros = 0;
        for (JsonNode c : categorias) {
            if (c.get("cantidadLibros").asLong() > 0) {
                conLibros++;
            }
        }
        assertThat(conLibros).isGreaterThanOrEqualTo(10);
    }

    @Test
    @DisplayName("JPQL: estado=ACTIVO,VENCIDO devuelve solo préstamos pendientes de devolución")
    void prestamosPendientes() throws Exception {
        mvc.perform(get("/api/prestamos?estado=ACTIVO,VENCIDO&size=50").header("Authorization", token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(greaterThan(0)))
                .andExpect(jsonPath("$.content[*].estado", everyItem(not(equalTo("DEVUELTO")))));
    }

    @Test
    @DisplayName("Transacción: si el préstamo falla (plazo > 60 días) se revierte el descuento del ejemplar")
    void prestamoFallidoNoAlteraElStock() throws Exception {
        String token = token();
        long libroId = getJson("/api/libros?q=soledad", token).get("content").get(0).get("id").asLong();
        long usuarioId = getJson("/api/usuarios?q=45678912", token).get("content").get(0).get("id").asLong();
        int antes = getJson("/api/libros/" + libroId + "/disponibilidad", token).get("cantidadDisponible").asInt();

        String cuerpo = String.format("{\"usuarioId\":%d,\"libroId\":%d,\"fechaVencimiento\":\"%s\"}",
                usuarioId, libroId, LocalDate.now().plusDays(90));
        mvc.perform(post("/api/prestamos").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje", containsString("60 días")));

        int despues = getJson("/api/libros/" + libroId + "/disponibilidad", token).get("cantidadDisponible").asInt();
        assertThat(despues).isEqualTo(antes);
    }

    @Test
    @DisplayName("RN-13: al suspender a un usuario se cancelan sus reservas abiertas")
    void suspenderCancelaReservas() throws Exception {
        String token = token();
        // Sofía Morales (DNI 49123456) tiene una reserva PENDIENTE de "El principito" en los datos de ejemplo.
        long usuarioId = getJson("/api/usuarios?q=49123456", token).get("content").get(0).get("id").asLong();
        mvc.perform(get("/api/reservas?estado=PENDIENTE&usuarioId=" + usuarioId).header("Authorization", token))
                .andExpect(jsonPath("$.content", hasSize(1)));

        mvc.perform(patch("/api/usuarios/" + usuarioId + "/estado").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"estado\":\"SUSPENDIDO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("SUSPENDIDO"));

        mvc.perform(get("/api/reservas?estado=PENDIENTE&usuarioId=" + usuarioId).header("Authorization", token))
                .andExpect(jsonPath("$.content", hasSize(0)));
        mvc.perform(get("/api/reservas?estado=CANCELADA&usuarioId=" + usuarioId).header("Authorization", token))
                .andExpect(jsonPath("$.content", hasSize(1)));

        // Se deja al usuario como estaba para no afectar otras pruebas.
        mvc.perform(patch("/api/usuarios/" + usuarioId + "/estado").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"estado\":\"ACTIVO\"}"))
                .andExpect(status().isOk());
    }
}
