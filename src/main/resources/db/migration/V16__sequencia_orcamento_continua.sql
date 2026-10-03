INSERT INTO sequencia_orcamento(unidade_id,ano,proximo_numero)
SELECT unidade_id,0,MAX(proximo_numero) FROM sequencia_orcamento s
WHERE NOT EXISTS(SELECT 1 FROM sequencia_orcamento atual WHERE atual.unidade_id=s.unidade_id AND atual.ano=0)
GROUP BY unidade_id;
