package br.com.gestaoservicos.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class TratadorExcecoesApi {
    @ExceptionHandler(ResponseStatusException.class)
    ProblemDetail tratarStatus(ResponseStatusException excecao, HttpServletRequest requisicao) {
        ProblemDetail detalhe = ProblemDetail.forStatusAndDetail(excecao.getStatusCode(), excecao.getReason());
        detalhe.setProperty("path", requisicao.getRequestURI());
        return detalhe;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail tratarValidacao(MethodArgumentNotValidException excecao, HttpServletRequest requisicao) {
        String mensagem = excecao.getBindingResult().getFieldErrors().stream()
                .findFirst().map(erro -> erro.getField() + ": " + erro.getDefaultMessage())
                .orElse("Requisição inválida");
        ProblemDetail detalhe = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, mensagem);
        detalhe.setProperty("path", requisicao.getRequestURI());
        return detalhe;
    }
}
