package br.com.gestaoservicos.associacao.service;

import br.com.gestaoservicos.associacao.dto.UsuarioUnidadeResponseDTO;
import br.com.gestaoservicos.associacao.model.Associacao;
import br.com.gestaoservicos.associacao.model.Perfil;
import br.com.gestaoservicos.associacao.repository.AssociacaoRepository;
import br.com.gestaoservicos.unidade.repository.UnidadeRepository;
import br.com.gestaoservicos.usuario.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class AssociacaoService {
    private final AssociacaoRepository associacoes;
    private final UsuarioRepository usuarios;
    private final UnidadeRepository unidades;

    public AssociacaoService(AssociacaoRepository associacoes, UsuarioRepository usuarios, UnidadeRepository unidades) {
        this.associacoes = associacoes;
        this.usuarios = usuarios;
        this.unidades = unidades;
    }

    @Transactional(readOnly = true)
    public List<UsuarioUnidadeResponseDTO> listarAtivos(UUID unidadeId) {
        return associacoes.findAllByUnidadeIdAndAtivaTrueOrderByUsuarioNome(unidadeId).stream()
                .map(UsuarioUnidadeResponseDTO::de).toList();
    }

    @Transactional
    public UsuarioUnidadeResponseDTO adicionar(UUID unidadeId, String email, Perfil perfil) {
        var usuario = usuarios.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new RegraAssociacaoException("USUARIO_NAO_ENCONTRADO", "Não há usuário cadastrado com este e-mail."));
        var associacaoExistente = associacoes.findByUsuarioIdAndUnidadeId(usuario.getId(), unidadeId);
        if (associacaoExistente.filter(Associacao::isAtiva).isPresent()) {
            throw new RegraAssociacaoException("USUARIO_JA_POSSUI_ACESSO_UNIDADE", "Este usuário já possui acesso à unidade." );
        }
        if (associacaoExistente.isPresent()) {
            var associacao = associacaoExistente.get();
            associacao.reativar(perfil);
            return UsuarioUnidadeResponseDTO.de(associacao);
        }
        var unidade = unidades.findById(unidadeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unidade não encontrada"));
        return UsuarioUnidadeResponseDTO.de(associacoes.save(new Associacao(usuario, unidade, perfil)));
    }

    @Transactional
    public UsuarioUnidadeResponseDTO alterarPerfil(UUID unidadeId, UUID associacaoId, Perfil perfil) {
        bloquearUnidade(unidadeId);
        var associacao = obterAtiva(unidadeId, associacaoId);
        validarNaoRemoveUltimoAdmin(unidadeId, associacao.getPerfil(), perfil);
        associacao.alterarPerfil(perfil);
        return UsuarioUnidadeResponseDTO.de(associacao);
    }

    @Transactional
    public void desativar(UUID unidadeId, UUID associacaoId) {
        bloquearUnidade(unidadeId);
        var associacao = obterAtiva(unidadeId, associacaoId);
        validarNaoRemoveUltimoAdmin(unidadeId, associacao.getPerfil(), null);
        associacao.desativar();
    }

    private Associacao obterAtiva(UUID unidadeId, UUID associacaoId) {
        return associacoes.findByIdAndUnidadeIdAndAtivaTrue(associacaoId, unidadeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado nesta unidade"));
    }

    private void bloquearUnidade(UUID unidadeId) {
        unidades.findByIdComBloqueio(unidadeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unidade não encontrada"));
    }

    private void validarNaoRemoveUltimoAdmin(UUID unidadeId, Perfil perfilAtual, Perfil novoPerfil) {
        if (perfilAtual == Perfil.ADMIN && novoPerfil != Perfil.ADMIN
                && associacoes.countByUnidadeIdAndPerfilAndAtivaTrue(unidadeId, Perfil.ADMIN) == 1) {
            throw new RegraAssociacaoException("ULTIMO_ADMIN_DA_UNIDADE", "A unidade precisa manter ao menos um administrador ativo.");
        }
    }
}
