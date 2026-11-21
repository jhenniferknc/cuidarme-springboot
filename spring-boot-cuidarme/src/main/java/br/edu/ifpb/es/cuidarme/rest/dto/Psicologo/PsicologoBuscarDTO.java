package br.edu.ifpb.es.cuidarme.rest.dto.Psicologo;

import lombok.Data;
import org.hibernate.validator.constraints.UUID;

@Data
public class PsicologoBuscarDTO {

    private UUID lookupId;
    private String nome;
    private String email;
    private Integer numeroPagina = 0;
    private Integer tamanhoPagina = 10;

}
