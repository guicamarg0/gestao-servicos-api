ALTER TABLE revisao_orcamento ADD COLUMN exibir_pagamento boolean NOT NULL DEFAULT true;
ALTER TABLE revisao_orcamento ADD COLUMN referencia varchar(4000);
