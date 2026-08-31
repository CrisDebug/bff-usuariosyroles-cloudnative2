package bff.cl.ms_bff.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.util.Map;

@RestController
@RequestMapping("/bff/usuarios")
public class UsuariosController {

        /*
         * ============================================================
         * BFF - CRUD DE USUARIOS
         * ============================================================
         *
         * El BFF recibe la solicitud del cliente y la delega
         * a la Azure Function usuariosJava.
         *
         * IMPORTANTE:
         *
         * La URL de la Function NO está escrita directamente aquí.
         *
         * Spring la obtiene desde:
         *
         * application.properties
         * -> LOCAL
         *
         * application-ec2.properties
         * -> EC2 / Azure
         *
         * De esta forma mantenemos el mismo código del BFF
         * para ambos ambientes.
         *
         * LOCAL:
         * http://localhost:7071/api/usuariosJava
         *
         * EC2 / Azure:
         * https://fn-veterinaria-citas-java.azurewebsites.net/api/usuariosJava
         *
         * ============================================================
         */

        /*
         * URL de la Azure Function usuariosJava.
         *
         * Spring obtiene automáticamente el valor desde
         * el archivo de configuración correspondiente al
         * perfil activo.
         */
        @Value("${azure.functions.usuarios.url}")
        private String usuariosUrl;

        /*
         * ============================================================
         * GET - LISTAR USUARIOS
         * ============================================================
         *
         * GET /bff/usuarios
         *
         * Cliente
         * ↓
         * BFF
         * ↓
         * Azure Function usuariosJava
         * ↓
         * Oracle
         */
        @GetMapping
        public ResponseEntity<String> listarUsuarios() {

                RestClient client = RestClient.create();

                String respuesta = client.get()
                                .uri(usuariosUrl)
                                .retrieve()
                                .body(String.class);

                return ResponseEntity.ok(respuesta);
        }

        /*
         * ============================================================
         * POST - CREAR USUARIO
         * ============================================================
         *
         * POST /bff/usuarios
         *
         * Body:
         *
         * {
         * "nombre": "Maria Lopez",
         * "email": "maria.lopez@test.cl",
         * "password": "123456",
         * "rolId": 1
         * }
         */
        @PostMapping
        public ResponseEntity<String> crearUsuario(
                        @RequestBody Map<String, Object> body) {

                RestClient client = RestClient.create();

                String json = """
                                {
                                    "nombre": "%s",
                                    "email": "%s",
                                    "password": "%s",
                                    "rolId": %s
                                }
                                """.formatted(
                                body.get("nombre"),
                                body.get("email"),
                                body.get("password"),
                                body.get("rolId"));

                String respuesta = client.post()
                                .uri(usuariosUrl)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(json)
                                .retrieve()
                                .body(String.class);

                return ResponseEntity.ok(respuesta);
        }

        /*
         * ============================================================
         * PUT - ACTUALIZAR USUARIO
         * ============================================================
         *
         * PUT /bff/usuarios
         *
         * Body:
         *
         * {
         * "id": 2,
         * "nombre": "Maria Lopez Actualizada",
         * "email": "maria.actualizada@test.cl",
         * "password": "123456",
         * "rolId": 1
         * }
         */
        @PutMapping
        public ResponseEntity<String> actualizarUsuario(
                        @RequestBody Map<String, Object> body) {

                RestClient client = RestClient.create();

                String json = """
                                {
                                    "id": %s,
                                    "nombre": "%s",
                                    "email": "%s",
                                    "password": "%s",
                                    "rolId": %s
                                }
                                """.formatted(
                                body.get("id"),
                                body.get("nombre"),
                                body.get("email"),
                                body.get("password"),
                                body.get("rolId"));

                String respuesta = client.put()
                                .uri(usuariosUrl)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(json)
                                .retrieve()
                                .body(String.class);

                return ResponseEntity.ok(respuesta);
        }

        /*
         * ============================================================
         * DELETE - ELIMINAR USUARIO
         * ============================================================
         *
         * DELETE /bff/usuarios/{id}
         *
         * El BFF transforma:
         *
         * /bff/usuarios/2
         *
         * en:
         *
         * /api/usuariosJava?id=2
         *
         * ============================================================
         */
        @DeleteMapping("/{id}")
        public ResponseEntity<String> eliminarUsuario(
                        @PathVariable Long id) {

                RestClient client = RestClient.create();

                /*
                 * La URL base viene desde la configuración.
                 *
                 * Ejemplo:
                 *
                 * https://fn-veterinaria-citas-java.azurewebsites.net
                 * /api/usuariosJava?id=2
                 */
                String url = usuariosUrl + "?id=" + id;

                String respuesta = client.delete()
                                .uri(url)
                                .retrieve()
                                .body(String.class);

                return ResponseEntity.ok(respuesta);
        }
}
