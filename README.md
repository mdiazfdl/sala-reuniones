# Sala de reuniones - API REST

API REST en Java 17 y Spring Boot 3.5 para crear y consultar salas de reuniones y gestionar sus reservas,
evitando solapamientos en la misma sala.

## Requisitos

- JDK 17 o superior (probado con JDK 25)
- Maven 3.9+
- PostgreSQL (opcional; por defecto se usa H2 en memoria)

## Cómo ejecutarla

```bash
# Compilar y ejecutar todos los tests
mvn verify

# Arrancar la aplicación con H2 en memoria (por defecto, puerto 8080)
mvn spring-boot:run
```

Usuario para las operaciones de escritura (configurable con variables de entorno `APP_USER` y `APP_PASSWORD`):

- Usuario: `admin`
- Contraseña: `admin123`

### Con PostgreSQL

Crea una base de datos `salas` con usuario `salas`, o ajusta las variables de conexión, y activa el perfil:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
# Variables opcionales: DB_URL, DB_USER, DB_PASSWORD
```

Flyway aplica las migraciones al arrancar en ambos motores.

## Swagger UI

Con la aplicación en marcha, la documentación interactiva está en:

- Swagger UI: http://localhost:8080/swagger-ui/index.html
- Especificación OpenAPI (JSON): http://localhost:8080/v3/api-docs

Para probar los endpoints de escritura, pulsa **Authorize** e introduce `admin` / `admin123`.

## Endpoints

| Método | Ruta | Auth | Descripción |
|---|---|---|---|
| GET | `/api/rooms` | No | Lista las salas |
| POST | `/api/rooms` | Basic | Crea una sala `{"name","capacity"}` (201) |
| GET | `/api/bookings` | No | Lista reservas paginadas. Filtros: `roomId`, `date` (yyyy-MM-dd). Paginación: `page`, `size` (máx. 100), `sort` |
| POST | `/api/bookings` | Basic | Crea una reserva `{"roomId","responsible","start","end"}` (201) |
| POST | `/api/bookings/{id}/cancel` | Basic | Cancela una reserva (200) |

Fechas en formato ISO local, sin zona horaria: `2030-01-10T09:00:00`.

Ejemplos:

```bash
curl -u admin:admin123 -H "Content-Type: application/json" \
  -d "{\"name\":\"Sala Aurora\",\"capacity\":8}" http://localhost:8080/api/rooms

curl -u admin:admin123 -H "Content-Type: application/json" \
  -d "{\"roomId\":1,\"responsible\":\"Ana\",\"start\":\"2030-01-10T09:00:00\",\"end\":\"2030-01-10T10:00:00\"}" \
  http://localhost:8080/api/bookings

curl "http://localhost:8080/api/bookings?roomId=1&date=2030-01-10&page=0&size=20&sort=start,asc"
```

### Códigos de respuesta y errores

Los errores siguen ProblemDetail (RFC 7807). Ejemplos:

- `400` datos inválidos, con la lista `errors` (campo y mensaje), fecha de fin anterior al inicio, reserva en el pasado, JSON mal formado o parámetro de tipo incorrecto.
- `401` falta autenticación en una operación de escritura.
- `404` sala o reserva inexistente.
- `409` la sala ya está reservada en ese horario, nombre de sala duplicado, o reserva ya cancelada.

## Decisiones de diseño

- **Solapamiento**: dos intervalos `[inicio, fin)` se solapan si `inicio_A < fin_B` y `inicio_B < fin_A`.
  Una reserva que termina a las 10:00 no choca con otra que empieza a las 10:00. Solo cuentan las reservas `ACTIVE`.
- **Concurrencia**: al crear una reserva se bloquea la fila de la sala (`SELECT ... FOR UPDATE`) dentro de la
  transacción. Así, dos peticiones simultáneas para la misma sala se procesan en serie y la segunda ve la primera.
  Esto no depende de un motor concreto, aunque no he podido probarlo con carga concurrente real.
- **Cancelación lógica**: cancelar cambia el estado a `CANCELLED` en vez de borrar el registro, para conservar el historial.
  Cancelar dos veces devuelve 409.
- **Filtro por fecha**: una reserva aparece en un día si se solapa con él, así que una reserva nocturna
  (23:00-01:00) aparece en los dos días.
- **Seguridad**: consultas públicas y escrituras con HTTP Basic. La API es sin estado: sin sesiones ni CSRF.
  Se eligió Basic en lugar de JWT por simplicidad; el usuario se define en configuración, no hay tabla de usuarios.
- **Paginación**: respuesta propia `{content, page, size, totalElements, totalPages}` en lugar de la serialización de `PageImpl`,
  que Spring no garantiza estable.
- **Esquema**: migraciones Flyway (`V1__crear_salas.sql`, `V2__crear_reservas.sql`) con Hibernate en modo `validate`,
  de forma que el esquema lo define SQL y no JPA.
- **Validación de negocio en el DTO**: la regla fin > inicio se expresa con `@AssertTrue`; el error se devuelve en el campo `end`.

## Tests

```bash
mvn test
```

- `BookingOverlapTest` (unitario): reglas de solapamiento, incluido el caso de extremos contiguos.
- `RoomServiceTest` (unitario con Mockito): creación de salas y rechazo de nombres duplicados.
- `BookingApiIntegrationTest` (integración con MockMvc sobre H2 y Flyway): seguridad, flujo completo
  crear / solapar (409) / cancelar / listar con filtros y paginación, y validación 400.

## Qué haría con más tiempo

- Probar con PostgreSQL real con Testcontainers, y añadir un test de concurrencia (dos hilos reservando el mismo hueco).
- Restringir el listado por estado (`ACTIVE`/`CANCELLED`) y ordenar solo por campos permitidos (hoy un `sort` inválido puede dar error sin mensaje claro).
- Autenticación con JWT y roles (p. ej. solo administradores crean salas), y una tabla de usuarios con contraseñas cifradas en lugar de un usuario fijo.
- Verificar que una reserva cabe en la capacidad de la sala (campo `capacity`, hoy solo se guarda).
- Zonas horarias (`OffsetDateTime`) en lugar de hora local sin zona.
- Documentación OpenAPI/Swagger, y un endpoint GET por id de sala y reserva.
- Límite de tasa en las escrituras y logs de auditoría con el usuario que hizo cada cambio.

## Uso de la IA

Usé un asistente de IA (OpenCode) como compañero de desarrollo a lo largo del proyecto.

**Qué le pedí**
- Un plan por pasos con un commit por cada fase, y un push tras cada commit.
- La estructura inicial del proyecto Maven, las entidades, las migraciones, los endpoints, la configuración de seguridad,
  el manejo de errores, el listado paginado y los tests.

**Qué acepté tal cual**
- Estructura por paquetes de dominio (`room`, `booking`, `common`, `config`).
- Migraciones SQL escritas a mano, con sintaxis estándar pensada para H2 y PostgreSQL (solo probadas en H2).
- Detección de solapamiento en la consulta derivada del repositorio y el bloqueo pesimista de la sala.
- Tests con MockMvc sobre la aplicación completa.

**Qué corregí o comprobé**
- Tras el primer arranque, las escrituras devolvían 401 aunque las credenciales eran correctas. La causa era
  la configuración por defecto de Spring Security (CSRF y sesión). Lo corregí con una cadena de seguridad sin estado.
- Los errores 400 de parámetros se convertían en 401 porque Spring reenvía a `/error`, y esa ruta quedaba protegida.
  Lo corregí permitiendo `/error` y añadiendo un manejador para tipos de parámetro incorrectos.
- No di por bueno el código solo por compilar: lo arranqué y probé cada endpoint con curl (incluidos casos límite como
  reservas contiguas, reservas nocturnas y cancelaciones dobles) antes de hacer el commit.
- Algunos errores de mis pruebas eran del entorno, no del código (p. ej. comillas en PowerShell); los identifiqué
  leyendo el log del servidor antes de tocar el código.
- Quedan sin verificar: el perfil PostgreSQL y el comportamiento bajo concurrencia real. Están marcados como pendientes arriba.
