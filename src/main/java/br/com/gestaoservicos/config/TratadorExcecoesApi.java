package br.com.gestaoservicos.config;

import br.com.gestaoservicos.unidade.service.NomeUnidadeJaExistenteException;
import br.com.gestaoservicos.associacao.service.RegraAssociacaoException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class TratadorExcecoesApi {
    @ExceptionHandler(RegraAssociacaoException.class)
    ProblemDetail tratarRegraAssociacao(RegraAssociacaoException excecao, HttpServletRequest requisicao) {
        ProblemDetail detalhe = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, excecao.getMessage());
        detalhe.setProperty("code", excecao.getCodigo());
        detalhe.setProperty("path", requisicao.getRequestURI());
        return detalhe;
    }

    @ExceptionHandler(NomeUnidadeJaExistenteException.class)
    ProblemDetail tratarNomeUnidadeJaExistente(
            NomeUnidadeJaExistenteException excecao,
            HttpServletRequest requisicao) {
        ProblemDetail detalhe = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, excecao.getMessage());
        detalhe.setProperty("code", "UNIDADE_NOME_JA_EXISTENTE");
        detalhe.setProperty("path", requisicao.getRequestURI());
        return detalhe;
    }

    @ExceptionHandler(ResponseStatusException.class)
    ProblemDetail tratarStatus(ResponseStatusException excecao, HttpServletRequest requisicao) {
        ProblemDetail detalhe = ProblemDetail.forStatusAndDetail(excecao.getStatusCode(), excecao.getReason());
        detalhe.setProperty("code", codigoPara(excecao.getStatusCode()));
        detalhe.setProperty("path", requisicao.getRequestURI());
        return detalhe;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail tratarValidacao(MethodArgumentNotValidException excecao, HttpServletRequest requisicao) {
        String mensagem = excecao.getBindingResult().getFieldErrors().stream()
                .findFirst().map(erro -> erro.getField() + ": " + erro.getDefaultMessage())
                .orElse("Requisição inválida");
        ProblemDetail detalhe = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, mensagem);
        detalhe.setProperty("code", "VALIDACAO_INVALIDA");
        detalhe.setProperty("path", requisicao.getRequestURI());
        return detalhe;
    }

    private String codigoPara(org.springframework.http.HttpStatusCode status) {
        if (status.isSameCodeAs(HttpStatus.BAD_REQUEST)) return "REQUISICAO_INVALIDA";
        if (status.isSameCodeAs(HttpStatus.UNAUTHORIZED)) return "NAO_AUTENTICADO";
        if (status.isSameCodeAs(HttpStatus.FORBIDDEN)) return "ACESSO_NEGADO";
        if (status.isSameCodeAs(HttpStatus.NOT_FOUND)) return "RECURSO_NAO_ENCONTRADO";
        if (status.isSameCodeAs(HttpStatus.CONFLICT)) return "CONFLITO_DE_REGRA";
        return "ERRO_DE_NEGOCIO";
    }
}
