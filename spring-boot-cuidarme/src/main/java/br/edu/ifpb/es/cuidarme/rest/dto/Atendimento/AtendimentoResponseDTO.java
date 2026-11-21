package br.edu.ifpb.es.cuidarme.rest.dto.Atendimento;

import br.edu.ifpb.es.cuidarme.model.StatusAtendimento;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AtendimentoResponseDTO {

    private UUID lookupId;
    private LocalDateTime data;
    private String localidade;
    private StatusAtendimento status;
    private UUID pacienteId;
    private String pacienteNome;
    private LocalDateTime atualizadoEm;

}