package br.com.gestaoservicos;

import br.com.gestaoservicos.autenticacao.service.EmailRecuperacaoService;
import br.com.gestaoservicos.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.mockito.ArgumentCaptor;
import tools.jackson.databind.ObjectMapper;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc
class RecuperacaoSenhaIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired UsuarioRepository usuarios;
    @Autowired PasswordEncoder senhas;
    @Autowired ObjectMapper json;
    @Autowired JdbcTemplate banco;
    @MockitoBean EmailRecuperacaoService emails;
    @MockitoBean br.com.gestaoservicos.arquivo.service.SupabaseStorageService storage;

    @Test void equipeCompletaERecuperacaoRespeitamUnidade() throws Exception {
        String admin="equipe.admin@test.com", membro="equipe.membro@test.com";
        cadastrar(admin);cadastrar(membro);String token=login(admin,"senha12345");
        String unidade=criarUnidade(token,"Equipe");
        String resposta=mvc.perform(post("/api/v1/unidades/atual/usuarios")
            .header("Authorization","Bearer "+token).header("X-Unidade-Id",unidade).contentType(MediaType.APPLICATION_JSON)
            .content(json.writeValueAsString(java.util.Map.of("email",membro,"perfil","OPERADOR"))))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String id=json.readTree(resposta).get("id").asText();
        mvc.perform(put("/api/v1/unidades/atual/usuarios/"+id).header("Authorization","Bearer "+token).header("X-Unidade-Id",unidade)
            .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(java.util.Map.of("nome","Nome atualizado","email",membro,"perfil","GESTOR"))))
            .andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Nome atualizado"));
        mvc.perform(post("/api/v1/unidades/atual/usuarios/"+id+"/recuperar-senha").header("Authorization","Bearer "+token).header("X-Unidade-Id",unidade)).andExpect(status().isNoContent());
        verify(emails).enviar(eq(membro),anyString());
        String outra=criarUnidade(token,"Outra equipe");
        mvc.perform(post("/api/v1/unidades/atual/usuarios/"+id+"/recuperar-senha").header("Authorization","Bearer "+token).header("X-Unidade-Id",outra)).andExpect(status().isNotFound());
        mvc.perform(delete("/api/v1/unidades/atual/usuarios/"+id).header("Authorization","Bearer "+token).header("X-Unidade-Id",unidade)).andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/unidades/atual/usuarios").header("Authorization","Bearer "+token).header("X-Unidade-Id",unidade)).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(id)).andExpect(jsonPath("$[0].ativa").value(false));
        mvc.perform(post("/api/v1/unidades/atual/usuarios/"+id+"/reativar").header("Authorization","Bearer "+token).header("X-Unidade-Id",unidade)).andExpect(status().isOk()).andExpect(jsonPath("$.ativa").value(true));
    }

    @Test void comprovantePrivadoValidaConteudoEUnidade() throws Exception {
        cadastrar("arquivo.teste@test.com");String token=login("arquivo.teste@test.com","senha12345");String unidade=criarUnidade(token,"Arquivos");
        var imagem=new java.awt.image.BufferedImage(2,2,java.awt.image.BufferedImage.TYPE_INT_RGB);
        var saida=new java.io.ByteArrayOutputStream();javax.imageio.ImageIO.write(imagem,"png",saida);byte[] bytes=saida.toByteArray();
        var arquivo=new org.springframework.mock.web.MockMultipartFile("arquivo","comprovante.png","image/png",bytes);
        String resposta=mvc.perform(multipart("/api/v1/arquivos").file(arquivo).header("Authorization","Bearer "+token).header("X-Unidade-Id",unidade))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.tipo").value("image/png")).andReturn().getResponse().getContentAsString();
        var caminho=ArgumentCaptor.forClass(String.class);verify(storage).salvar(caminho.capture(),eq(bytes),eq("image/png"));
        assertThat(caminho.getValue()).startsWith(unidade+"/anexos/");when(storage.ler(caminho.getValue())).thenReturn(bytes);
        String id=json.readTree(resposta).get("id").asText();
        mvc.perform(get("/api/v1/arquivos/"+id).header("Authorization","Bearer "+token).header("X-Unidade-Id",unidade)).andExpect(status().isOk()).andExpect(content().bytes(bytes));
        String outra=criarUnidade(token,"Sem arquivos");
        mvc.perform(get("/api/v1/arquivos/"+id).header("Authorization","Bearer "+token).header("X-Unidade-Id",outra)).andExpect(status().isNotFound());
        var falso=new org.springframework.mock.web.MockMultipartFile("arquivo","falso.pdf","application/pdf","texto".getBytes());
        mvc.perform(multipart("/api/v1/arquivos").file(falso).header("Authorization","Bearer "+token).header("X-Unidade-Id",unidade)).andExpect(status().isBadRequest());
    }

    private String criarUnidade(String token,String nome) throws Exception {
        String resposta=mvc.perform(post("/api/v1/unidades").header("Authorization","Bearer "+token).contentType(MediaType.APPLICATION_JSON)
            .content(json.writeValueAsString(java.util.Map.of("nome",nome)))).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return json.readTree(resposta).get("id").asText();
    }

    @Test void pdfEmitidoUsaStorageEPreservaDownloadDaRevisao() throws Exception {
        cadastrar("pdf.storage@test.com");String token=login("pdf.storage@test.com","senha12345");String unidade=criarUnidade(token,"PDF Storage");
        when(storage.configurado()).thenReturn(true);
        String corpo="{\"exibirAssinatura\":false,\"descontoTipo\":\"VALOR\",\"descontoValor\":0,\"acrescimoTipo\":\"VALOR\",\"acrescimoValor\":0,\"itens\":[{\"tipo\":\"MAO_DE_OBRA\",\"descricao\":\"Visita\",\"unidadeMedida\":\"HORA\",\"quantidade\":1,\"valorUnitario\":100}]}";
        String criado=mvc.perform(post("/api/v1/orcamentos").header("Authorization","Bearer "+token).header("X-Unidade-Id",unidade).contentType(MediaType.APPLICATION_JSON).content(corpo)).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String id=json.readTree(criado).get("id").asText();
        mvc.perform(post("/api/v1/orcamentos/"+id+"/emitir").header("Authorization","Bearer "+token).header("X-Unidade-Id",unidade)).andExpect(status().isOk());
        var caminho=ArgumentCaptor.forClass(String.class);var conteudo=ArgumentCaptor.forClass(byte[].class);
        verify(storage).salvar(caminho.capture(),conteudo.capture(),eq("application/pdf"));
        assertThat(caminho.getValue()).startsWith(unidade+"/orcamentos/");
        var registro=banco.queryForMap("select conteudo,caminho_storage from pdf_orcamento where caminho_storage=?",caminho.getValue());
        assertThat(registro.get("conteudo")).isNull();assertThat(registro.get("caminho_storage")).isEqualTo(caminho.getValue());
        when(storage.ler(caminho.getValue())).thenReturn(conteudo.getValue());
        mvc.perform(get("/api/v1/orcamentos/"+id+"/revisoes/1/pdf").header("Authorization","Bearer "+token).header("X-Unidade-Id",unidade)).andExpect(status().isOk()).andExpect(content().bytes(conteudo.getValue()));
    }

    @Test void linkUsoUnicoAlteraHashERevogaSessao() throws Exception {
        String email = "recuperacao.fluxo@test.com";
        cadastrar(email);
        String sessao = login(email, "senha12345");
        solicitar(email);
        var captor = ArgumentCaptor.forClass(String.class);
        verify(emails).enviar(eq(email), captor.capture());
        String token = captor.getValue();
        assertThat(token).hasSize(43);
        assertThat(banco.queryForObject("select token_hash from recuperacao_senha where usuario_id = ?", String.class,
            usuarios.findByEmailIgnoreCase(email).orElseThrow().getId())).isNotEqualTo(token).hasSize(64);
        mvc.perform(post("/api/v1/autenticacao/redefinir-senha").contentType(MediaType.APPLICATION_JSON)
            .content(json.writeValueAsString(java.util.Map.of("token", token, "senha", "novaSenha123")))).andExpect(status().isNoContent());
        assertThat(senhas.matches("novaSenha123", usuarios.findByEmailIgnoreCase(email).orElseThrow().getSenhaHash())).isTrue();
        mvc.perform(get("/api/v1/me").header("Authorization", "Bearer " + sessao)).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/autenticacao/redefinir-senha").contentType(MediaType.APPLICATION_JSON)
            .content(json.writeValueAsString(java.util.Map.of("token", token, "senha", "outraSenha123")))).andExpect(status().isBadRequest());
        login(email, "novaSenha123");
    }
    @Test void expiracaoLimiteEEmailInexistente() throws Exception {
        String email = "recuperacao.limite@test.com";
        cadastrar(email); solicitar(email); solicitar(email);
        var captor = ArgumentCaptor.forClass(String.class);
        verify(emails, times(1)).enviar(eq(email), captor.capture());
        banco.update("update recuperacao_senha set expira_em = criado_em where usuario_id = ?", usuarios.findByEmailIgnoreCase(email).orElseThrow().getId());
        mvc.perform(post("/api/v1/autenticacao/redefinir-senha").contentType(MediaType.APPLICATION_JSON)
            .content(json.writeValueAsString(java.util.Map.of("token", captor.getValue(), "senha", "novaSenha123")))).andExpect(status().isBadRequest());
        solicitar("nao.existe@test.com");
        verify(emails, never()).enviar(eq("nao.existe@test.com"), anyString());
    }
    private void solicitar(String email) throws Exception {
        mvc.perform(post("/api/v1/autenticacao/recuperar-senha").contentType(MediaType.APPLICATION_JSON)
            .content(json.writeValueAsString(java.util.Map.of("email", email)))).andExpect(status().isOk()).andExpect(jsonPath("$.mensagem").exists());
    }
    private void cadastrar(String email) throws Exception {
        mvc.perform(post("/api/v1/autenticacao/cadastro").contentType(MediaType.APPLICATION_JSON)
            .content(json.writeValueAsString(java.util.Map.of("nome", "Teste", "email", email, "senha", "senha12345")))).andExpect(status().isCreated());
    }
    private String login(String email, String senha) throws Exception {
        var resposta = mvc.perform(post("/api/v1/autenticacao/login").contentType(MediaType.APPLICATION_JSON)
            .content(json.writeValueAsString(java.util.Map.of("email", email, "senha", senha)))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return json.readTree(resposta).get("tokenAcesso").asText();
    }
}
