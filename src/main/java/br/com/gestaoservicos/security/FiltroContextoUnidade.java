package br.com.gestaoservicos.security;

import br.com.gestaoservicos.associacao.repository.AssociacaoRepository;
import br.com.gestaoservicos.compartilhado.erro.CodigosErroApi;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.UUID;

@Component
public class FiltroContextoUnidade extends OncePerRequestFilter {
    public static final String CABECALHO_UNIDADE = "X-Unidade-Id";
    private final AssociacaoRepository associacoes;
    private final ObjectMapper mapeadorJson;

    public FiltroContextoUnidade(AssociacaoRepository associacoes, ObjectMapper mapeadorJson) {
        this.associacoes = associacoes;
        this.mapeadorJson = mapeadorJson;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest requisicao, HttpServletResponse resposta, FilterChain cadeia)
            throws ServletException, IOException {
        try {
            Authentication autenticacao = org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication();
            String cabecalho = requisicao.getHeader(CABECALHO_UNIDADE);
            if (autenticacao instanceof JwtAuthenticationToken && cabecalho != null && !cabecalho.isBlank()) {
                UUID usuarioId;
                UUID unidadeId;
                try {
                    usuarioId = UUID.fromString(autenticacao.getName());
                    unidadeId = UUID.fromString(cabecalho);
                } catch (IllegalArgumentException exception) {
                    escreverProblema(requisicao, resposta, HttpStatus.BAD_REQUEST, "Identificador de unidade inválido");
                    return;
                }
                var associacao = associacoes.findByUsuarioIdAndUnidadeIdAndAtivaTrue(usuarioId, unidadeId);
                if (associacao.isEmpty()) {
                    escreverProblema(requisicao, resposta, HttpStatus.FORBIDDEN, "Usuário não possui acesso à unidade informada");
                    return;
                }
                ContextoUnidade.definir(unidadeId, associacao.get().getPerfil());
            }
            cadeia.doFilter(requisicao, resposta);
        } finally {
            ContextoUnidade.limpar();
        }
    }

    private void escreverProblema(HttpServletRequest requisicao, HttpServletResponse resposta,
                                  HttpStatus status, String detalhe) throws IOException {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalhe);
        problema.setProperty("code", CodigosErroApi.para(status));
        problema.setProperty("path", requisicao.getRequestURI());
        resposta.setStatus(status.value());
        resposta.setContentType("application/problem+json");
        mapeadorJson.writeValue(resposta.getOutputStream(), problema);
    }
}
