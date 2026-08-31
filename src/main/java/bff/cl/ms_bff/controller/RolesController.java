package bff.cl.ms_bff.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.util.Map;

@RestController
@RequestMapping("/bff/roles")
public class RolesController {

    /*
     * ============================================================
     * BFF - CRUD DE ROLES
     * ============================================================
     *
     * El BFF recibe la petición del cliente y delega la operación
     * a la Azure Function rolesJava.
     *
     * IMPORTANTE:
     *
     * La URL NO está escrita directamente en el controlador.
     *
     * Spring la obtiene desde:
     *
     * application.properties -> LOCAL
     * application-ec2.properties -> EC2
     *
     * Esto permite mantener el mismo código del BFF en ambos
     * escenarios.
     *
     * LOCAL:
     * http://localhost:7071/api/rolesJava
     *
     * EC2:
     * https://fn-veterinaria-citas-java.azurewebsites.net/api/rolesJava
     *
     * ============================================================
     */

    /*
     * URL de la Azure Function.
     *
     * Spring buscará esta propiedad en el archivo de configuración
     * correspondiente al perfil activo.
     */
    @Value("${azure.functions.roles.url}")
    private String rolesUrl;

    /*
     * ============================================================
     * GET - LISTAR ROLES
     * ============================================================
     *
     * Cliente
     * ↓
     * BFF /bff/roles
     * ↓
     * Azure Function rolesJava
     * ↓
     * Oracle
     */
    @GetMapping
    public ResponseEntity<String> listarRoles() {

        RestClient client = RestClient.create();

        String respuesta = client.get()
                .uri(rolesUrl)
                .retrieve()
                .body(String.class);

        return ResponseEntity.ok(respuesta);
    }

    /*
     * ============================================================
     * POST - CREAR ROL
     * ============================================================
     */
    @PostMapping
    public ResponseEntity<String> crearRol(
            @RequestBody Map<String, Object> body) {

        RestClient client = RestClient.create();

        String json = """
                {
                    "nombre": "%s"
                }
                """.formatted(body.get("nombre"));

        String respuesta = client.post()
                .uri(rolesUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(json)
                .retrieve()
                .body(String.class);

        return ResponseEntity.ok(respuesta);
    }

    /*
     * ============================================================
     * PUT - ACTUALIZAR ROL
     * ============================================================
     */
    @PutMapping
    public ResponseEntity<String> actualizarRol(
            @RequestBody Map<String, Object> body) {

        RestClient client = RestClient.create();

        String json = """
                {
                    "id": %s,
                    "nombre": "%s"
                }
                """.formatted(
                body.get("id"),
                body.get("nombre"));

        String respuesta = client.put()
                .uri(rolesUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(json)
                .retrieve()
                .body(String.class);

        return ResponseEntity.ok(respuesta);
    }

    /*
     * ============================================================
     * DELETE - ELIMINAR ROL
     * ============================================================
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminarRol(
            @PathVariable Long id) {

        RestClient client = RestClient.create();

        /*
         * Para DELETE agregamos el ID al final de la URL.
         *
         * Ejemplo:
         *
         * https://fn-veterinaria-citas-java.azurewebsites.net
         * /api/rolesJava/3
         */
        String url = rolesUrl + "?id=" + id;

        String respuesta = client.delete()
                .uri(url)
                .retrieve()
                .body(String.class);

        return ResponseEntity.ok(respuesta);
    }
}
