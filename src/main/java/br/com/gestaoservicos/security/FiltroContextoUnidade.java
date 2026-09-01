package br.com.gestaoservicos.security;

import br.com.gestaoservicos.associacao.repository.AssociacaoRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class FiltroContextoUnidade extends OncePerRequestFilter {
    public static final String CABECALHO_UNIDADE = "X-Unidade-Id";
    private final AssociacaoRepository associacoes;

    public FiltroContextoUnidade(AssociacaoRepository associacoes) { this.associacoes = associacoes; }

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
                    escreverProblema(resposta, 400, "Identificador de unidade inválido");
                    return;
                }
                var associacao = associacoes.findByUsuarioIdAndUnidadeId(usuarioId, unidadeId);
                if (associacao.isEmpty()) {
                    escreverProblema(resposta, 403, "Usuário não possui acesso à unidade informada");
                    return;
                }
                ContextoUnidade.definir(unidadeId, associacao.get().getPerfil());
            }
            cadeia.doFilter(requisicao, resposta);
        } finally {
            ContextoUnidade.limpar();
        }
    }

    private void escreverProblema(HttpServletResponse resposta, int status, String detalhe) throws IOException {
        resposta.setStatus(status);
        resposta.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        resposta.getWriter().printf("{\"status\":%d,\"detail\":\"%s\"}", status, detalhe);
    }
}
