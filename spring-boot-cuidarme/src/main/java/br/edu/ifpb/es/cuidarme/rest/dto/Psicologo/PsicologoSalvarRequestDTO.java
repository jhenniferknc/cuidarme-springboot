package br.edu.ifpb.es.cuidarme.rest.dto.Psicologo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PsicologoSalvarRequestDTO {

    @NotBlank
    private String nome;

    @NotBlank
    @Email(message = "E-mail inválido.")
    private String email;

    @NotBlank
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$",
             message = "A senha deve ter no mínimo 8 caracteres, com letras e números.")
    private String senha;

}
