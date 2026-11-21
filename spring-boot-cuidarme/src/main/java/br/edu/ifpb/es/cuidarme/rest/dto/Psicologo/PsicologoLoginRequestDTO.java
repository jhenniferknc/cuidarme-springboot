package br.edu.ifpb.es.cuidarme.rest.dto.Psicologo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PsicologoLoginRequestDTO {

    @NotBlank
    private String email;

    @NotBlank
    private String senha;

}
