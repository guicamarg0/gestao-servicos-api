package br.com.gestaoservicos;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ContratoRascunhoVersaoIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    @Test
    void preservaRascunhosEVersoesDeModeloEContrato() throws Exception {
        String token = login("contrato.versoes@test.com");
        UUID unidade = unidade(token, "Unidade Versões Contrato");
        String modelo = mvc.perform(post("/api/v1/modelos-contrato").header("Authorization", bearer(token)).header("X-Unidade-Id", unidade)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Modelo técnico\",\"descricao\":\"Teste\",\"conteudo\":\"Cliente @RazaoSocialCliente\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("RASCUNHO")).andExpect(jsonPath("$.numeroVersao").value(1))
                .andReturn().getResponse().getContentAsString();
        UUID modeloId = UUID.fromString(json.readTree(modelo).get("id").asText());

        mvc.perform(put("/api/v1/modelos-contrato/{id}", modeloId).header("Authorization", bearer(token)).header("X-Unidade-Id", unidade)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Modelo técnico\",\"descricao\":\"Teste\",\"conteudo\":\"Cliente @RazaoSocialCliente atualizado\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.numeroVersao").value(2));
        mvc.perform(get("/api/v1/modelos-contrato/{id}/versoes", modeloId).header("Authorization", bearer(token)).header("X-Unidade-Id", unidade))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2)).andExpect(jsonPath("$[0].numero").value(2)).andExpect(jsonPath("$[0].conteudo").value("Cliente @RazaoSocialCliente atualizado"));
        mvc.perform(post("/api/v1/modelos-contrato/{id}/inativar", modeloId).header("Authorization", bearer(token)).header("X-Unidade-Id", unidade))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("ARQUIVADO"));
        mvc.perform(post("/api/v1/modelos-contrato/{id}/reativar", modeloId).header("Authorization", bearer(token)).header("X-Unidade-Id", unidade))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("PUBLICADO"));

        UUID orcamento = criarOrcamentoAprovado(token, unidade);
        UUID empresaInicial = empresa(token, unidade, "Empresa Inicial LTDA", "12345678000190");
        UUID empresaNova = empresa(token, unidade, "Empresa Nova LTDA", "12345678000270");
        UUID outraUnidade = unidade(token, "Outra Unidade Contrato");
        UUID empresaExterna = empresa(token, outraUnidade, "Empresa Externa LTDA", "12345678000350");
        String corpoInicial = comEmpresa(contrato(orcamento, modeloId, "Versão inicial"), empresaInicial);
        String criado = mvc.perform(post("/api/v1/contratos").header("Authorization", bearer(token)).header("X-Unidade-Id", unidade)
                        .contentType(MediaType.APPLICATION_JSON).content(corpoInicial))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.empresaId").value(empresaInicial.toString())).andExpect(jsonPath("$.numeroVersao").value(1)).andExpect(jsonPath("$.conteudoRascunho").value("Versão inicial"))
                .andReturn().getResponse().getContentAsString();
        UUID contratoId = UUID.fromString(json.readTree(criado).get("id").asText());

        mvc.perform(put("/api/v1/contratos/{id}", contratoId).header("Authorization", bearer(token)).header("X-Unidade-Id", unidade)
                        .contentType(MediaType.APPLICATION_JSON).content(comEmpresa(contrato(orcamento, modeloId, "Inválido"), empresaExterna)))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/api/v1/contratos/{id}", contratoId).header("Authorization", bearer(token)).header("X-Unidade-Id", unidade)
                        .contentType(MediaType.APPLICATION_JSON).content(comEmpresa(contrato(orcamento, modeloId, "Versão revisada"), empresaNova)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.empresaId").value(empresaNova.toString())).andExpect(jsonPath("$.empresaContratada").value("Empresa Nova LTDA")).andExpect(jsonPath("$.numeroVersao").value(2)).andExpect(jsonPath("$.conteudoRascunho").value("Versão revisada"));
        mvc.perform(post("/api/v1/contratos/previa").header("Authorization", bearer(token)).header("X-Unidade-Id", unidade)
                        .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(java.util.Map.of("orcamentoId", orcamento, "modeloContratoId", modeloId, "empresaId", empresaNova))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.conteudo").value("Cliente Cliente Contrato atualizado"))
                .andExpect(jsonPath("$.assinaturasHtml").value(org.hamcrest.Matchers.containsString("Empresa Nova LTDA")));
        mvc.perform(get("/api/v1/contratos/{id}/snapshots", contratoId).header("Authorization", bearer(token)).header("X-Unidade-Id", unidade))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2)).andExpect(jsonPath("$[0].numeroVersao").value(2)).andExpect(jsonPath("$[0].conteudo").value("Versão revisada")).andExpect(jsonPath("$[1].conteudo").value("Versão inicial"));
    }

    private String comEmpresa(String corpo, UUID empresaId) {
        return corpo.substring(0, corpo.length() - 1) + ",\"empresaId\":\"" + empresaId + "\"}";
    }
    private UUID empresa(String token, UUID unidade, String nome, String cnpj) throws Exception {
        return UUID.fromString(json.readTree(mvc.perform(post("/api/v1/empresas").header("Authorization", bearer(token)).header("X-Unidade-Id", unidade)
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(java.util.Map.of("tipo", "MATRIZ", "razaoSocial", nome, "cnpj", cnpj))))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString()).get("id").asText());
    }

    private UUID criarOrcamentoAprovado(String token, UUID unidade) throws Exception {
        UUID cliente = UUID.fromString(json.readTree(mvc.perform(post("/api/v1/contratantes").header("Authorization", bearer(token)).header("X-Unidade-Id", unidade)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"tipo\":\"PF\",\"nomeRazaoSocial\":\"Cliente Contrato\",\"documento\":\"12345678901\",\"contatos\":[{\"nome\":\"Principal\",\"telefone\":\"11999999999\"}]}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString()).get("id").asText());
        String orcamento = mvc.perform(post("/api/v1/orcamentos").header("Authorization", bearer(token)).header("X-Unidade-Id", unidade)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"contratanteId\":\"" + cliente + "\",\"exibirAssinatura\":false,\"descontoTipo\":\"VALOR\",\"descontoValor\":0,\"acrescimoTipo\":\"VALOR\",\"acrescimoValor\":0,\"itens\":[{\"tipo\":\"MAO_DE_OBRA\",\"descricao\":\"Visita\",\"unidadeMedida\":\"HORA\",\"quantidade\":1,\"valorUnitario\":100}]}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        UUID id = UUID.fromString(json.readTree(orcamento).get("id").asText());
        mvc.perform(post("/api/v1/orcamentos/{id}/emitir", id).header("Authorization", bearer(token)).header("X-Unidade-Id", unidade)).andExpect(status().isOk());
        mvc.perform(post("/api/v1/orcamentos/{id}/aprovar", id).header("Authorization", bearer(token)).header("X-Unidade-Id", unidade)).andExpect(status().isOk());
        return id;
    }

    private String contrato(UUID orcamentoId, UUID modeloId, String conteudo) {
        return "{\"orcamentoId\":\"" + orcamentoId + "\",\"modelo\":\"Modelo técnico\",\"objeto\":\"Serviço industrial\",\"inicioVigencia\":\"2026-10-01\",\"fimVigencia\":\"2027-04-01\",\"modeloContratoId\":\"" + modeloId + "\",\"conteudoRascunho\":\"" + conteudo + "\"}";
    }
    private String login(String email) throws Exception { mvc.perform(post("/api/v1/autenticacao/cadastro").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(new Cadastro("Ana", email, "password123")))).andExpect(status().isCreated()); return json.readTree(mvc.perform(post("/api/v1/autenticacao/login").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(new Login(email, "password123")))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get("tokenAcesso").asText(); }
    private UUID unidade(String token, String nome) throws Exception { return UUID.fromString(json.readTree(mvc.perform(post("/api/v1/unidades").header("Authorization", bearer(token)).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(new Nome(nome)))).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString()).get("id").asText()); }
    private String bearer(String token) { return "Bearer " + token; }
    record Cadastro(String nome, String email, String senha) {}
    record Login(String email, String senha) {}
    record Nome(String nome) {}
}
