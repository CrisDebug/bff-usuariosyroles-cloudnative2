package bff.cl.ms_bff.graphql;

import bff.cl.ms_bff.graphql.dto.ClienteDTO;
import bff.cl.ms_bff.graphql.dto.MascotaDTO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.List;

@Controller
public class ClienteGraphQLController {

    @Value("${microservices.clientes.url}")
    private String clientesUrl;

    @Value("${microservices.mascotas.url}")
    private String mascotasUrl;

    @QueryMapping
    public List<ClienteDTO> clientes() {

        RestClient client = RestClient.create();

        ClienteDTO[] respuesta = client.get()
                .uri(clientesUrl)
                .retrieve()
                .body(ClienteDTO[].class);

        return respuesta != null
                ? Arrays.asList(respuesta)
                : List.of();
    }

    @QueryMapping
    public List<MascotaDTO> mascotas() {

        RestClient client = RestClient.create();

        MascotaDTO[] respuesta = client.get()
                .uri(mascotasUrl)
                .retrieve()
                .body(MascotaDTO[].class);

        return respuesta != null
                ? Arrays.asList(respuesta)
                : List.of();
    }
}