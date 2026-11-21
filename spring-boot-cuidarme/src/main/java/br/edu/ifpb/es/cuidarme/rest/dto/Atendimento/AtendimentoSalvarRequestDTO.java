package br.edu.ifpb.es.cuidarme.rest.dto.Atendimento;

import br.edu.ifpb.es.cuidarme.model.StatusAtendimento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AtendimentoSalvarRequestDTO {
    
    private UUID lookupId;

    @NotNull
    private LocalDateTime data;
    @NotBlank
    private String localidade;
    @NotNull
    private StatusAtendimento status;

}