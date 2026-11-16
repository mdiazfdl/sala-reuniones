package com.salas.reuniones;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test de integración: aplicación completa (H2 + Flyway + seguridad) probada con MockMvc.
 * Cada test se ejecuta en una transacción que se revierte al terminar.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BookingApiIntegrationTest {

    private static final String ADMIN_USER = "admin";
    private static final String ADMIN_PASSWORD = "admin123";

    @Autowired
    private MockMvc mvc;

    @Test
    void consultarEsPublicoPeroCrearRequiereAutenticacion() throws Exception {
        mvc.perform(get("/api/rooms")).andExpect(status().isOk());

        mvc.perform(post("/api/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Sala Anonima","capacity":4}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void flujoCompletoCrearSolaparCancelarYListar() throws Exception {
        long roomId = createRoom("Sala Integracion", 6);

        // Reserva inicial 09:00-10:00
        long first = createBooking(roomId, "Ana", "2030-01-10T09:00:00", "2030-01-10T10:00:00");

        // Solapada 09:30-11:00 -> 409
        mvc.perform(post("/api/bookings")
                        .with(httpBasic(ADMIN_USER, ADMIN_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingJson(roomId, "Luis", "2030-01-10T09:30:00", "2030-01-10T11:00:00")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));

        // Contigua 10:00-11:00 -> 201
        createBooking(roomId, "Eva", "2030-01-10T10:00:00", "2030-01-10T11:00:00");

        // Cancelar la primera libera su hueco
        mvc.perform(post("/api/bookings/" + first + "/cancel")
                        .with(httpBasic(ADMIN_USER, ADMIN_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        createBooking(roomId, "Luis", "2030-01-10T09:30:00", "2030-01-10T10:00:00");

        // Filtro por sala y fecha, con paginación
        mvc.perform(get("/api/bookings")
                        .param("roomId", String.valueOf(roomId))
                        .param("date", "2030-01-10")
                        .param("size", "2")
                        .param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    void validacionDevuelve400ConDetalleDeCampos() throws Exception {
        mvc.perform(post("/api/bookings")
                        .with(httpBasic(ADMIN_USER, ADMIN_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"roomId":1,"responsible":"","start":"2030-01-10T10:00:00","end":"2030-01-10T09:00:00"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Datos no válidos"))
                .andExpect(jsonPath("$.errors[?(@.field == 'responsible')]").exists())
                .andExpect(jsonPath("$.errors[?(@.field == 'end')]").exists());
    }

    private long createRoom(String name, int capacity) throws Exception {
        MvcResult result = mvc.perform(post("/api/rooms")
                        .with(httpBasic(ADMIN_USER, ADMIN_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\",\"capacity\":" + capacity + "}"))
                .andExpect(status().isCreated())
                .andReturn();
        return extractId(result);
    }

    private long createBooking(long roomId, String responsible, String start, String end) throws Exception {
        MvcResult result = mvc.perform(post("/api/bookings")
                        .with(httpBasic(ADMIN_USER, ADMIN_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingJson(roomId, responsible, start, end)))
                .andExpect(status().isCreated())
                .andReturn();
        return extractId(result);
    }

    private static String bookingJson(long roomId, String responsible, String start, String end) {
        return "{\"roomId\":" + roomId + ",\"responsible\":\"" + responsible
                + "\",\"start\":\"" + start + "\",\"end\":\"" + end + "\"}";
    }

    private static long extractId(MvcResult result) throws Exception {
        String body = result.getResponse().getContentAsString();
        // Extrae el primer "id" numérico del JSON de respuesta.
        var matcher = java.util.regex.Pattern.compile("\"id\":(\\d+)").matcher(body);
        if (!matcher.find()) {
            throw new IllegalStateException("La respuesta no contiene id: " + body);
        }
        return Long.parseLong(matcher.group(1));
    }
}
