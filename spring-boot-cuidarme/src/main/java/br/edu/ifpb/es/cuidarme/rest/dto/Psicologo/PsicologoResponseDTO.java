package br.edu.ifpb.es.cuidarme.rest.dto.Psicologo;

import lombok.Data;

import java.util.UUID;

@Data
public class PsicologoResponseDTO {

    private UUID lookupId;
    private String nome;
    private String email;

}
