package db.migration;

import br.com.gestaoservicos.unidade.service.NormalizadorNomeUnidade;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class V2__unidades_nome_unico extends BaseJavaMigration {
    @Override
    public void migrate(Context context) throws Exception {
        try (Statement comando = context.getConnection().createStatement()) {
            comando.execute("alter table unidade add column nome_normalizado varchar(120)");
        }

        List<UnidadeExistente> unidades = new ArrayList<>();
        try (Statement consulta = context.getConnection().createStatement();
             ResultSet resultado = consulta.executeQuery("select id, nome from unidade")) {
            while (resultado.next()) {
                unidades.add(new UnidadeExistente(
                        resultado.getObject("id", UUID.class),
                        resultado.getString("nome")));
            }
        }

        try (PreparedStatement atualizacao = context.getConnection().prepareStatement(
                "update unidade set nome_normalizado = ? where id = ?")) {
            for (UnidadeExistente unidade : unidades) {
                atualizacao.setString(1, NormalizadorNomeUnidade.normalizar(unidade.nome()));
                atualizacao.setObject(2, unidade.id());
                atualizacao.addBatch();
            }
            atualizacao.executeBatch();
        }

        try (Statement comando = context.getConnection().createStatement()) {
            comando.execute("alter table unidade alter column nome_normalizado set not null");
            comando.execute("alter table unidade add constraint uk_unidade_nome_normalizado unique (nome_normalizado)");
        }
    }

    private record UnidadeExistente(UUID id, String nome) {}
}
