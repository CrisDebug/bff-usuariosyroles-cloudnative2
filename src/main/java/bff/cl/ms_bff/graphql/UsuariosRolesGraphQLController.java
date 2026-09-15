package bff.cl.ms_bff.graphql;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Controller
public class UsuariosRolesGraphQLController {

    @Value("${azure.functions.usuarios.graphql.url}")
    private String usuariosGraphQLUrl;

    @Value("${azure.functions.roles.graphql.url}")
    private String rolesGraphQLUrl;

    private final RestClient client = RestClient.create();

    @QueryMapping
    public List<Map<String, Object>> usuarios() {

        String query = """
                {
                    "query": "{ usuarios { id nombre email rolId rol } }"
                }
                """;

        Map<?, ?> respuesta = ejecutarGraphQL(usuariosGraphQLUrl, query);

        Object dataObj = respuesta.get("data");

        if (!(dataObj instanceof Map<?, ?> data)) {
            return List.of();
        }

        Object usuariosObj = data.get("usuarios");

        if (!(usuariosObj instanceof List<?> lista)) {
            return List.of();
        }

        return convertirLista(lista);
    }

    @QueryMapping
    public Map<String, Object> usuario(@Argument Long id) {

        String query = """
                {
                    "query": "{ usuario(id: %d) { id nombre email rolId rol } }"
                }
                """.formatted(id);

        Map<?, ?> respuesta = ejecutarGraphQL(usuariosGraphQLUrl, query);

        Object dataObj = respuesta.get("data");

        if (!(dataObj instanceof Map<?, ?> data)) {
            return null;
        }

        Object usuarioObj = data.get("usuario");

        if (!(usuarioObj instanceof Map<?, ?> usuario)) {
            return null;
        }

        return convertirMapa(usuario);
    }

    @QueryMapping
    public List<Map<String, Object>> roles() {

        String query = """
                {
                    "query": "{ roles { id nombre } }"
                }
                """;

        Map<?, ?> respuesta = ejecutarGraphQL(rolesGraphQLUrl, query);

        Object dataObj = respuesta.get("data");

        if (!(dataObj instanceof Map<?, ?> data)) {
            return List.of();
        }

        Object rolesObj = data.get("roles");

        if (!(rolesObj instanceof List<?> lista)) {
            return List.of();
        }

        return convertirLista(lista);
    }

    @QueryMapping
    public Map<String, Object> rol(@Argument Long id) {

        String query = """
                {
                    "query": "{ rol(id: %d) { id nombre } }"
                }
                """.formatted(id);

        Map<?, ?> respuesta = ejecutarGraphQL(rolesGraphQLUrl, query);

        Object dataObj = respuesta.get("data");

        if (!(dataObj instanceof Map<?, ?> data)) {
            return null;
        }

        Object rolObj = data.get("rol");

        if (!(rolObj instanceof Map<?, ?> rol)) {
            return null;
        }

        return convertirMapa(rol);
    }

    private Map<?, ?> ejecutarGraphQL(String url, String query) {

        Map<?, ?> respuesta = client.post()
                .uri(url)
                .header("Content-Type", "application/json")
                .body(query)
                .retrieve()
                .body(Map.class);

        return respuesta != null ? respuesta : Map.of();
    }

    private Map<String, Object> convertirMapa(Map<?, ?> mapa) {

        return mapa.entrySet()
                .stream()
                .collect(
                        java.util.stream.Collectors.toMap(
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