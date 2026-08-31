package bff.cl.ms_bff.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@RestController
@RequestMapping("/bff")
public class UsuariosRolesController {

    @Value("${azure.functions.usuarios.url}")
    private String usuariosUrl;

    @Value("${azure.functions.roles.url}")
    private String rolesUrl;

    /*
     * ============================================================
     * GET - USUARIOS CON SU ROL
     * ============================================================
     *
     * GET /bff/usuarios-con-roles
     *
     * El BFF consulta dos Azure Functions:
     *
     * usuariosJava -> obtiene los usuarios
     * rolesJava -> obtiene los roles
     *
     * Luego el BFF combina ambas respuestas.
     *
     * Las Azure Functions y Oracle NO necesitan modificarse.
     *
     * ============================================================
     */
    @GetMapping("/usuarios-con-roles")
    public ResponseEntity<String> usuariosConRoles() {

        RestClient client = RestClient.create();

        // Obtener usuarios
        String usuarios = client.get()
                .uri(usuariosUrl)
                .retrieve()
                .body(String.class);

        // Obtener roles
        String roles = client.get()
                .uri(rolesUrl)
                .retrieve()
                .body(String.class);

        /*
         * Por ahora devolvemos ambas respuestas para validar
         * correctamente la comunicación entre el BFF y las
         * dos Azure Functions.
         */
        String respuesta = """
                {
                    "usuarios": %s,
                    "roles": %s
                }
                """.formatted(usuarios, roles);

        return ResponseEntity.ok(respuesta);
    }
}
