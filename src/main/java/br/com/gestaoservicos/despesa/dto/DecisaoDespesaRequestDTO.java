package br.com.gestaoservicos.despesa.dto;

import jakarta.validation.constraints.Size;

public record DecisaoDespesaRequestDTO(@Size(max = 2000) String comentario) {}
