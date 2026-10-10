
package bff.cl.ms_bff.graphql;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class UsuariosRolesGraphQLController {

        @Value("${azure.functions.usuarios.graphql.url}")
        private String usuariosGraphQLUrl;

        @Value("${azure.functions.roles.graphql.url}")
        private String rolesGraphQLUrl;

        private final RestClient client = RestClient.create();

        // ==================== CONSULTAS DE USUARIOS ====================

        @QueryMapping
        public List<Map<String, Object>> usuarios() {
                Map<?, ?> respuesta = ejecutarGraphQL(
                                usuariosGraphQLUrl,
                                "{ usuarios { id nombre email rolId rol } }");

                Object data = obtenerData(respuesta).get("usuarios");

                return data instanceof List<?> lista
                                ? convertirLista(lista)
                                : List.of();
        }

        @QueryMapping
        public Map<String, Object> usuario(@Argument Long id) {
                String query = "{ usuario(id: " + id
                                + ") { id nombre email rolId rol } }";

                Map<?, ?> respuesta = ejecutarGraphQL(usuariosGraphQLUrl, query);
                Object data = obtenerData(respuesta).get("usuario");

                return data instanceof Map<?, ?> mapa
                                ? convertirMapa(mapa)
                                : null;
        }

        // ==================== CONSULTAS DE ROLES ====================

        @QueryMapping
        public List<Map<String, Object>> roles() {
                Map<?, ?> respuesta = ejecutarGraphQL(
                                rolesGraphQLUrl,
                                "{ roles { id nombre } }");

                Object data = obtenerData(respuesta).get("roles");

                return data instanceof List<?> lista
                                ? convertirLista(lista)
                                : List.of();
        }

        @QueryMapping
        public Map<String, Object> rol(@Argument Long id) {
                String query = "{ rol(id: " + id + ") { id nombre } }";

                Map<?, ?> respuesta = ejecutarGraphQL(rolesGraphQLUrl, query);
                Object data = obtenerData(respuesta).get("rol");

                return data instanceof Map<?, ?> mapa
                                ? convertirMapa(mapa)
                                : null;
        }

        // ==================== MUTACIONES DE USUARIOS ====================

        @MutationMapping
        public Map<String, Object> crearUsuario(
                        @Argument String nombre,
                        @Argument String email,
                        @Argument String password,
                        @Argument Long rolId) {

                String query = """
                                mutation {
                                  crearUsuario(
                                    nombre: %s,
                                    email: %s,
                                    password: %s,
                                    rolId: %d
                                  ) {
                                    id nombre email rolId rol
                                  }
                                }
                                """.formatted(
                                graphqlString(nombre),
                                graphqlString(email),
                                graphqlString(password),
                                rolId);

                return obtenerObjeto(
                                ejecutarGraphQL(usuariosGraphQLUrl, query),
                                "crearUsuario");
        }

        @MutationMapping
        public Map<String, Object> actualizarUsuario(
                        @Argument Long id,
                        @Argument String nombre,
                        @Argument String email,
                        @Argument String password,
                        @Argument Long rolId) {

                String query = """
                                mutation {
                                  actualizarUsuario(
                                    id: %d,
                                    nombre: %s,
                                    email: %s,
                                    password: %s,
                                    rolId: %d
                                  ) {
                                    id nombre email rolId rol
                                  }
                                }
                                """.formatted(
                                id,
                                graphqlString(nombre),
                                graphqlString(email),
                                graphqlString(password),
                                rolId);

                return obtenerObjeto(
                                ejecutarGraphQL(usuariosGraphQLUrl, query),
                                "actualizarUsuario");
        }

        @MutationMapping
        public Boolean eliminarUsuario(@Argument Long id) {
                String query = """
                                mutation {
                                  eliminarUsuario(id: %d)
                                }
                                """.formatted(id);

                return obtenerBooleano(
                                ejecutarGraphQL(usuariosGraphQLUrl, query),
                                "eliminarUsuario");
        }

        // ==================== MUTACIONES DE ROLES ====================

        @MutationMapping
        public Map<String, Object> crearRol(@Argument String nombre) {
                String query = """
                                mutation {
                                  crearRol(nombre: %s) {
                                    id nombre
                                  }
                                }
                                """.formatted(graphqlString(nombre));

                return obtenerObjeto(
                                ejecutarGraphQL(rolesGraphQLUrl, query),
                                "crearRol");
        }

        @MutationMapping
        public Map<String, Object> actualizarRol(
                        @Argument Long id,
                        @Argument String nombre) {

                String query = """
                                mutation {
                                  actualizarRol(id: %d, nombre: %s) {
                                    id nombre
                                  }
                                }
                                """.formatted(id, graphqlString(nombre));

                return obtenerObjeto(
                                ejecutarGraphQL(rolesGraphQLUrl, query),
                                "actualizarRol");
        }

        @MutationMapping
        public Boolean eliminarRol(@Argument Long id) {
                String query = """
                                mutation {
                                  eliminarRol(id: %d)
                                }
                                """.formatted(id);

                return obtenerBooleano(
                                ejecutarGraphQL(rolesGraphQLUrl, query),
                                "eliminarRol");
        }

        // ==================== MÉTODOS AUXILIARES ====================

        private Map<?, ?> ejecutarGraphQL(String url, String query) {

                // Compacta la consulta sin modificar los saltos escapados dentro de Strings.
                String consultaCompacta = query
                                .replace("\r", " ")
                                .replace("\n", " ")
                                .trim();

                // Escapa el contenido para construir el JSON de transporte.
                String consultaEscapada = consultaCompacta
                                .replace("\\", "\\\\")
                                .replace("\"", "\\\"");

                String cuerpo = "{\"query\":\"" + consultaEscapada + "\"}";

                System.out.println("URL Function GraphQL: " + url);
                System.out.println("Consulta GraphQL enviada: " + consultaCompacta);

                Map<?, ?> respuesta = client.post()
                                .uri(url)
                                .header("Content-Type", "application/json")
                                .body(cuerpo)
                                .retrieve()
                                .body(Map.class);

                if (respuesta == null) {
                        throw new IllegalStateException(
                                        "La Function GraphQL no devolvió respuesta");
                }

                System.out.println("Respuesta de Function GraphQL: " + respuesta);
                return respuesta;
        }

        private Map<?, ?> obtenerData(Map<?, ?> respuesta) {
                if (respuesta.containsKey("errors")) {
                        throw new IllegalStateException(
                                        "Error GraphQL de la Function: "
                                                        + respuesta.get("errors"));
                }

                Object data = respuesta.get("data");

                if (!(data instanceof Map<?, ?> mapa)) {
                        throw new IllegalStateException(
                                        "La respuesta GraphQL no contiene data");
                }

                return mapa;
        }

        private Map<String, Object> obtenerObjeto(
                        Map<?, ?> respuesta, String campo) {

                Object resultado = obtenerData(respuesta).get(campo);

                if (!(resultado instanceof Map<?, ?> mapa)) {
                        throw new IllegalStateException(
                                        "La mutación " + campo + " no devolvió un objeto");
                }

                return convertirMapa(mapa);
        }

        private Boolean obtenerBooleano(
                        Map<?, ?> respuesta, String campo) {

                Object resultado = obtenerData(respuesta).get(campo);

                if (!(resultado instanceof Boolean valor)) {
                        throw new IllegalStateException(
                                        "La mutación " + campo + " no devolvió un Boolean");
                }

                return valor;
        }

        private String graphqlString(String valor) {
                if (valor == null) {
                        return "null";
                }

                String escapado = valor
                                .replace("\\", "\\\\")
                                .replace("\"", "\\\"")
                                .replace("\n", "\\n")
                                .replace("\r", "\\r")
                                .replace("\t", "\\t");

                return "\"" + escapado + "\"";
        }

        private Map<String, Object> convertirMapa(Map<?, ?> mapa) {
                return mapa.entrySet()
                                .stream()
                                .collect(Collectors.toMap(
                                                entry -> String.valueOf(entry.getKey()),
                                                Map.Entry::getValue));
        }

        private List<Map<String, Object>> convertirLista(List<?> lista) {
                return lista.stream()
                                .filter(item -> item instanceof Map<?, ?>)
                                .map(item -> convertirMapa((Map<?, ?>) item))
                                .toList();
        }
}
