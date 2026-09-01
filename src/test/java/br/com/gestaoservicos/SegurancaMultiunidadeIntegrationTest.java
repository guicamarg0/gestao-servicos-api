package br.com.gestaoservicos;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SegurancaMultiunidadeIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapeadorJson;

    @Test
    void criaUnidadeComAssociacaoAdminEBloqueiaAcessoCruzado() throws Exception {
        String tokenAlice = cadastrarEAutenticar("Alice", "alice@example.com");
        UUID unidadeAlice = criarUnidade(tokenAlice, "Oficina Alice");
        String tokenBob = cadastrarEAutenticar("Bob", "bob@example.com");
        UUID unidadeBob = criarUnidade(tokenBob, "Oficina Bob");

        mvc.perform(get("/api/v1/me").header("Authorization", "Bearer " + tokenAlice))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unidades[0].id").value(unidadeAlice.toString()))
                .andExpect(jsonPath("$.unidades[0].perfil").value("ADMIN"));

        mvc.perform(post("/api/v1/me/unidades/{unidadeId}/selecionar", unidadeBob)
                        .header("Authorization", "Bearer " + tokenAlice))
                .andExpect(status().isForbidden());

        mvc.perform(get("/api/v1/unidades/atual/protegida")
                        .header("Authorization", "Bearer " + tokenAlice)
                        .header("X-Unidade-Id", unidadeBob))
                .andExpect(status().isForbidden());

        mvc.perform(get("/api/v1/unidades/atual/protegida")
                        .header("Authorization", "Bearer " + tokenAlice)
                        .header("X-Unidade-Id", unidadeAlice))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unidadeId").value(unidadeAlice.toString()))
                .andExpect(jsonPath("$.perfil").value("ADMIN"));
    }

    @Test
    void rotaProtegidaExigeAutenticacaoEUnidadeSelecionada() throws Exception {
        mvc.perform(get("/api/v1/unidades/atual/protegida"))
                .andExpect(status().isUnauthorized());

        String token = cadastrarEAutenticar("Carol", "carol@example.com");
        mvc.perform(get("/api/v1/unidades/atual/protegida").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void cabecalhoDeUnidadeSemJwtNaoInterfereEmRotaPublica() throws Exception {
        mvc.perform(post("/api/v1/autenticacao/cadastro")
                        .header("X-Unidade-Id", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapeadorJson.writeValueAsString(
                                new CredenciaisCadastro("Daniel", "daniel@example.com", "password123"))))
                .andExpect(status().isCreated());
    }

    @Test
    void permitePreflightDoFrontendLocalNaPorta3000() throws Exception {
        mvc.perform(options("/api/v1/autenticacao/login")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"))
                .andExpect(header().string("Access-Control-Allow-Methods", org.hamcrest.Matchers.containsString("POST")));
    }

    private String cadastrarEAutenticar(String nome, String email) throws Exception {
        mvc.perform(post("/api/v1/autenticacao/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapeadorJson.writeValueAsString(new CredenciaisCadastro(nome, email, "password123"))))
                .andExpect(status().isCreated());
        String corpo = mvc.perform(post("/api/v1/autenticacao/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapeadorJson.writeValueAsString(new CredenciaisLogin(email, "password123"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return mapeadorJson.readTree(corpo).get("tokenAcesso").asText();
    }

    private UUID criarUnidade(String token, String nome) throws Exception {
        String corpo = mvc.perform(post("/api/v1/unidades")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapeadorJson.writeValueAsString(new NomeUnidade(nome))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.perfil").value("ADMIN"))
                .andReturn().getResponse().getContentAsString();
        JsonNode json = mapeadorJson.readTree(corpo);
        UUID id = UUID.fromString(json.get("id").asText());
        assertThat(id).isNotNull();
        return id;
    }

    record CredenciaisCadastro(String nome, String email, String senha) {}
    record CredenciaisLogin(String email, String senha) {}
    record NomeUnidade(String nome) {}
}
