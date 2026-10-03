package br.com.gestaoservicos;

import org.apache.pdfbox.Loader;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class OperacoesIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    @Test
    void agendaPermiteVariosServicosNoDiaEDespesaAprovadaSemVazarUnidade() throws Exception {
        String token = login("operacoes.fluxo@test.com");
        UUID unidade = unidade(token, "Operacoes Fluxo");
        UUID cliente = cliente(token, unidade);
        UUID primeiro = servico(token, unidade, cliente, "Primeiro");
        UUID segundo = servico(token, unidade, cliente, "Segundo");
        String horario = "{\"dataProgramada\":\"2026-09-29\",\"responsavel\":\"Carlos\"}";
        mvc.perform(put("/api/v1/servicos/{id}/agendamento", primeiro).header("Authorization", bearer(token)).header("X-Unidade-Id", unidade).contentType(MediaType.APPLICATION_JSON).content(horario))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("AGENDADO"));
        mvc.perform(put("/api/v1/servicos/{id}/agendamento", segundo).header("Authorization", bearer(token)).header("X-Unidade-Id", unidade).contentType(MediaType.APPLICATION_JSON).content(horario))
                .andExpect(status().isOk()).andExpect(jsonPath("$.dataProgramada").value("2026-09-29"));
        mvc.perform(get("/api/v1/agenda?de=2026-09-29T03:00:00Z&ate=2026-09-30T03:00:00Z").header("Authorization", bearer(token)).header("X-Unidade-Id", unidade))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElementos").value(2));
        mvc.perform(post("/api/v1/servicos/{id}/iniciar", primeiro).header("Authorization", bearer(token)).header("X-Unidade-Id", unidade))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("EM_ANDAMENTO"));
        mvc.perform(post("/api/v1/servicos/{id}/concluir", primeiro).header("Authorization", bearer(token)).header("X-Unidade-Id", unidade).contentType(MediaType.APPLICATION_JSON).content("{\"resumoConclusao\":\"Finalizado e testado\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CONCLUIDO"));
        String despesa = mvc.perform(post("/api/v1/despesas").header("Authorization", bearer(token)).header("X-Unidade-Id", unidade).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoria\":\"Transporte\",\"descricao\":\"Deslocamento\",\"valor\":450.00,\"dataDespesa\":\"2026-09-29\",\"servicoId\":\"" + primeiro + "\",\"enviarParaAprovacao\":true}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("PENDENTE"))
                .andReturn().getResponse().getContentAsString();
        UUID despesaId = UUID.fromString(json.readTree(despesa).get("id").asText());
        mvc.perform(post("/api/v1/despesas/{id}/aprovar", despesaId).header("Authorization", bearer(token)).header("X-Unidade-Id", unidade)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"comentario\":\"Conferido\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("APROVADA"));
        mvc.perform(get("/api/v1/relatorios/resumo?de=2026-09-29&ate=2026-09-29").header("Authorization", bearer(token)).header("X-Unidade-Id", unidade))
                .andExpect(status().isOk()).andExpect(jsonPath("$.despesasAprovadas").value(450.0));
        String outroToken = login("operacoes.outra@test.com");
        UUID outra = unidade(outroToken, "Operacoes Outra");
        mvc.perform(get("/api/v1/servicos/{id}", primeiro).header("Authorization", bearer(outroToken)).header("X-Unidade-Id", outra)).andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/despesas").header("Authorization", bearer(outroToken)).header("X-Unidade-Id", outra)).andExpect(status().isOk()).andExpect(jsonPath("$.totalElementos").value(0));
    }

    @Test
    void contratoNasceDeOrcamentoAprovadoEEmitePdfComTodasAsPaginas() throws Exception {
        String token = login("operacoes.contrato@test.com");
        UUID unidade = unidade(token, "Operacoes Contrato");
        UUID cliente = cliente(token, unidade);
        mvc.perform(put("/api/v1/unidades/atual/configuracao").header("Authorization", bearer(token)).header("X-Unidade-Id", unidade).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nomeRazaoSocial\":\"Prestadora\",\"documento\":\"52998224725\",\"enderecoCompleto\":\"Rua A\",\"formasRecebimento\":[{\"tipo\":\"PIX\",\"nomeExibicao\":\"Pix\",\"instrucoes\":\"chave\",\"ativa\":true,\"padrao\":true}]}"))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v1/unidades/atual/formas-recebimento").header("Authorization", bearer(token)).header("X-Unidade-Id", unidade))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].nomeExibicao").value("Pix"));
        String orcamento = mvc.perform(post("/api/v1/orcamentos").header("Authorization", bearer(token)).header("X-Unidade-Id", unidade).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"contratanteId\":\"" + cliente + "\",\"exibirAssinatura\":false,\"descontoTipo\":\"VALOR\",\"descontoValor\":0,\"acrescimoTipo\":\"VALOR\",\"acrescimoValor\":0,\"itens\":[{\"tipo\":\"MAO_DE_OBRA\",\"descricao\":\"Visita\",\"unidadeMedida\":\"HORA\",\"quantidade\":1,\"valorUnitario\":100}]}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        UUID orcamentoId = UUID.fromString(json.readTree(orcamento).get("id").asText());
        mvc.perform(post("/api/v1/orcamentos/{id}/emitir", orcamentoId).header("Authorization", bearer(token)).header("X-Unidade-Id", unidade)).andExpect(status().isOk());
        mvc.perform(post("/api/v1/orcamentos/{id}/aprovar", orcamentoId).header("Authorization", bearer(token)).header("X-Unidade-Id", unidade)).andExpect(status().isOk());
        String clausulas = "Condição especial. ".repeat(300);
        String corpo = json.writeValueAsString(new Contrato(orcamentoId, "Prestação de serviços", "Serviço industrial", clausulas, "2026-09-29", "2027-03-29"));
        String criado = mvc.perform(post("/api/v1/contratos").header("Authorization", bearer(token)).header("X-Unidade-Id", unidade).contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("RASCUNHO")).andReturn().getResponse().getContentAsString();
        UUID id = UUID.fromString(json.readTree(criado).get("id").asText());
        mvc.perform(post("/api/v1/contratos").header("Authorization", bearer(token)).header("X-Unidade-Id", unidade).contentType(MediaType.APPLICATION_JSON).content(corpo)).andExpect(status().isConflict());
        byte[] pdf = mvc.perform(get("/api/v1/contratos/{id}/pdf", id).header("Authorization", bearer(token)).header("X-Unidade-Id", unidade))
                .andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PDF)).andReturn().getResponse().getContentAsByteArray();
        try (var documento = Loader.loadPDF(pdf)) { assertThat(documento.getNumberOfPages()).isGreaterThan(1); }
        mvc.perform(post("/api/v1/contratos/{id}/enviar-assinatura", id).header("Authorization", bearer(token)).header("X-Unidade-Id", unidade))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("AGUARDANDO_ASSINATURA"));
        mvc.perform(post("/api/v1/contratos/{id}/registrar-assinatura", id).header("Authorization", bearer(token)).header("X-Unidade-Id", unidade).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assinadoPor\":\"Carlos\",\"assinadoEm\":\"2026-09-29\",\"canalAssinatura\":\"Eletrônica\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("ATIVO"));
    }

    @Test
    void despesasSomamItensNoServidorEFiltramPorServico() throws Exception {
        String token=login("despesas.itens@test.com");UUID unidade=unidade(token,"Despesas itens");UUID cliente=cliente(token,unidade);
        UUID servico=servico(token,unidade,cliente,"Com despesas"),outro=servico(token,unidade,cliente,"Sem despesas");
        String corpo="{\"categoria\":\"Materiais\",\"descricao\":\"Compra\",\"enviarParaAprovacao\":false,\"valor\":999,\"dataDespesa\":\"2026-10-03\",\"servicoId\":\""+servico+"\",\"itens\":[{\"descricao\":\"Peça\",\"quantidade\":2,\"valorUnitario\":12.34},{\"descricao\":\"Material\",\"quantidade\":0.5,\"valorUnitario\":20}]}";
        String resposta=mvc.perform(post("/api/v1/despesas").header("Authorization",bearer(token)).header("X-Unidade-Id",unidade).contentType(MediaType.APPLICATION_JSON).content(corpo))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.valor").value(34.68)).andExpect(jsonPath("$.itens.length()").value(2)).andReturn().getResponse().getContentAsString();
        UUID id=UUID.fromString(json.readTree(resposta).get("id").asText());
        mvc.perform(get("/api/v1/despesas?servicoId="+servico).header("Authorization",bearer(token)).header("X-Unidade-Id",unidade)).andExpect(status().isOk()).andExpect(jsonPath("$.totalElementos").value(1));
        mvc.perform(get("/api/v1/despesas?servicoId="+outro).header("Authorization",bearer(token)).header("X-Unidade-Id",unidade)).andExpect(status().isOk()).andExpect(jsonPath("$.totalElementos").value(0));
        mvc.perform(put("/api/v1/despesas/{id}",id).header("Authorization",bearer(token)).header("X-Unidade-Id",unidade).contentType(MediaType.APPLICATION_JSON).content(corpo.replace("12.34","10.00")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.valor").value(30.0));
    }

    @Test
    void painelConsideraRevisaoAtualPeriodoEUnidade() throws Exception {
        String token=login("painel.real@test.com");UUID unidade=unidade(token,"Painel real");
        String corpo="{\"exibirAssinatura\":false,\"descontoTipo\":\"VALOR\",\"descontoValor\":0,\"acrescimoTipo\":\"VALOR\",\"acrescimoValor\":0,\"itens\":[{\"tipo\":\"MAO_DE_OBRA\",\"descricao\":\"Visita\",\"unidadeMedida\":\"HORA\",\"quantidade\":1,\"valorUnitario\":100}]}";
        var criado=mvc.perform(post("/api/v1/orcamentos").header("Authorization",bearer(token)).header("X-Unidade-Id",unidade).contentType(MediaType.APPLICATION_JSON).content(corpo)).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String hoje=java.time.LocalDate.now(java.time.ZoneId.of("America/Sao_Paulo")).toString();
        String rota="/api/v1/painel?de="+hoje+"&ate="+hoje;
        mvc.perform(get(rota).header("Authorization",bearer(token)).header("X-Unidade-Id",unidade)).andExpect(status().isOk()).andExpect(jsonPath("$.orcamentos").value(1)).andExpect(jsonPath("$.valorOrcamentos").value(100)).andExpect(jsonPath("$.aprovados").value(0)).andExpect(jsonPath("$.evolucao.length()").value(1));
        UUID id=UUID.fromString(json.readTree(criado).get("id").asText());
        mvc.perform(post("/api/v1/orcamentos/{id}/emitir",id).header("Authorization",bearer(token)).header("X-Unidade-Id",unidade)).andExpect(status().isOk());
        mvc.perform(put("/api/v1/orcamentos/{id}",id).header("Authorization",bearer(token)).header("X-Unidade-Id",unidade).contentType(MediaType.APPLICATION_JSON).content(corpo.replace("100","200"))).andExpect(status().isOk());
        mvc.perform(get(rota).header("Authorization",bearer(token)).header("X-Unidade-Id",unidade)).andExpect(status().isOk()).andExpect(jsonPath("$.orcamentos").value(1)).andExpect(jsonPath("$.valorOrcamentos").value(200));
        String outro=login("painel.outra@test.com");UUID outra=unidade(outro,"Painel vazio");
        mvc.perform(get(rota).header("Authorization",bearer(outro)).header("X-Unidade-Id",outra)).andExpect(status().isOk()).andExpect(jsonPath("$.orcamentos").value(0)).andExpect(jsonPath("$.recentes.length()").value(0));
    }

    private UUID cliente(String token, UUID unidade) throws Exception {
        String corpo = "{\"tipo\":\"PF\",\"nomeRazaoSocial\":\"Cliente Operacional\",\"documento\":\"12345678901\",\"contatos\":[{\"nome\":\"Principal\",\"telefone\":\"11999999999\"}]}";
        return UUID.fromString(json.readTree(mvc.perform(post("/api/v1/contratantes").header("Authorization", bearer(token)).header("X-Unidade-Id", unidade).contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString()).get("id").asText());
    }
    private UUID servico(String token, UUID unidade, UUID cliente, String titulo) throws Exception {
        return UUID.fromString(json.readTree(mvc.perform(post("/api/v1/servicos").header("Authorization", bearer(token)).header("X-Unidade-Id", unidade).contentType(MediaType.APPLICATION_JSON)
                .content("{\"contratanteId\":\"" + cliente + "\",\"titulo\":\"" + titulo + "\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString()).get("id").asText());
    }
    private String login(String email) throws Exception {
        mvc.perform(post("/api/v1/autenticacao/cadastro").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(new Cadastro("Ana", email, "password123")))).andExpect(status().isCreated());
        return json.readTree(mvc.perform(post("/api/v1/autenticacao/login").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(new Login(email, "password123"))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get("tokenAcesso").asText();
    }
    private UUID unidade(String token, String nome) throws Exception {
        return UUID.fromString(json.readTree(mvc.perform(post("/api/v1/unidades").header("Authorization", bearer(token)).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(new Nome(nome))))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString()).get("id").asText());
    }
    private String bearer(String token) { return "Bearer " + token; }
    record Cadastro(String nome, String email, String senha) {}
    record Login(String email, String senha) {}
    record Nome(String nome) {}
    record Contrato(UUID orcamentoId, String modelo, String objeto, String clausulasAdicionais, String inicioVigencia, String fimVigencia) {}
}
