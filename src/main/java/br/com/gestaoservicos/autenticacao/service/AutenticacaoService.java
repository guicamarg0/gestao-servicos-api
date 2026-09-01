package br.com.gestaoservicos.autenticacao.service;

import br.com.gestaoservicos.autenticacao.dto.TokenResponseDTO;
import br.com.gestaoservicos.security.PropriedadesJwt;
import br.com.gestaoservicos.usuario.model.Usuario;
import br.com.gestaoservicos.usuario.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class AutenticacaoService {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder codificadorSenha;
    private final JwtEncoder codificadorJwt;
    private final PropriedadesJwt propriedadesJwt;

    public AutenticacaoService(UsuarioRepository usuarios, PasswordEncoder codificadorSenha,
                               JwtEncoder codificadorJwt, PropriedadesJwt propriedadesJwt) {
        this.usuarios = usuarios;
        this.codificadorSenha = codificadorSenha;
        this.codificadorJwt = codificadorJwt;
        this.propriedadesJwt = propriedadesJwt;
    }

    @Transactional
    public Usuario cadastrar(String nome, String email, String senha) {
        String emailNormalizado = email.trim().toLowerCase();
        if (usuarios.existsByEmailIgnoreCase(emailNormalizado)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado");
        }
        return usuarios.save(new Usuario(nome.trim(), emailNormalizado, codificadorSenha.encode(senha)));
    }

    @Transactional(readOnly = true)
    public TokenResponseDTO autenticar(String email, String senha) {
        Usuario usuario = usuarios.findByEmailIgnoreCase(email.trim())
                .filter(candidato -> codificadorSenha.matches(senha, candidato.getSenhaHash()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas"));
        Instant emitidoEm = Instant.now();
        Instant expiraEm = emitidoEm.plus(propriedadesJwt.minutosExpiracao(), ChronoUnit.MINUTES);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("gestao-servicos-api")
                .subject(usuario.getId().toString())
                .issuedAt(emitidoEm)
                .expiresAt(expiraEm)
                .claim("email", usuario.getEmail())
                .build();
        String tokenAcesso = codificadorJwt.encode(
                JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        return new TokenResponseDTO(tokenAcesso, "Bearer", expiraEm);
    }
}
