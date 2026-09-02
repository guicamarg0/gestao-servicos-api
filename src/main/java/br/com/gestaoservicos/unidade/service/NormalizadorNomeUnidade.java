package br.com.gestaoservicos.unidade.service;

import java.text.Normalizer;
import java.util.Locale;

final class NormalizadorNomeUnidade {
    private NormalizadorNomeUnidade() {}

    static String normalizar(String nome) {
        String semAcentos = Normalizer.normalize(nome, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return semAcentos.strip()
                .replaceAll("\\s+", " ")
                .toLowerCase(Locale.ROOT);
    }

    static String limparParaExibicao(String nome) {
        return nome.strip().replaceAll("\\s+", " ");
    }
}
