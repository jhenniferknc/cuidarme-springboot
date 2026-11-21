package br.edu.ifpb.es.cuidarme.rest.dto.Paciente;

import br.edu.ifpb.es.cuidarme.rest.dto.ContatoEmergencia.ContatoEmergenciaDTO;
import br.edu.ifpb.es.cuidarme.rest.dto.Endereco.EnderecoDTO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class PacienteSalvarRequestDTO {
    private UUID lookupId;
    @NotBlank
    private String nome;
    @NotBlank
    private String cpf;
    @NotBlank
    private String rg;
    @NotNull
    private LocalDate dataNascimento;
    @NotBlank
    private String sexo;
    @NotBlank
    private String estadoCivil;
    @NotBlank
    private String grauInstrucao;
    @NotBlank
    private String profissao;
    @NotBlank
    private String telefone;
    @NotNull
    private EnderecoDTO enderecoPessoal;
    private EnderecoDTO enderecoTrabalho;
    private String infoAdicionais;
    private ContatoEmergenciaDTO contatoEmergencia;

    @JsonIgnore
    @AssertTrue(message = "Endereço pessoal deve ter CEP, logradouro, cidade e estado.")
    public boolean isEnderecoPessoalCompleto() {
        if (enderecoPessoal == null) {
            return true;
        }
        return naoVazio(enderecoPessoal.getCep())
                && naoVazio(enderecoPessoal.getLogradouro())
                && naoVazio(enderecoPessoal.getCidade())
                && naoVazio(enderecoPessoal.getEstado());
    }

    private static boolean naoVazio(String valor) {
        return valor != null && !valor.isBlank();
    }
    
}
