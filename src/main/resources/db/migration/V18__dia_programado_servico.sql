ALTER TABLE servico ADD COLUMN data_programada DATE;
UPDATE servico SET data_programada = CAST(inicio_previsto AT TIME ZONE 'America/Sao_Paulo' AS DATE)
WHERE inicio_previsto IS NOT NULL;
CREATE INDEX idx_servico_agenda_dia ON servico(unidade_id, data_programada);
