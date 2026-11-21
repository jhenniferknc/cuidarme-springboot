package br.edu.ifpb.es.cuidarme.rest.dto.Paciente;

import br.edu.ifpb.es.cuidarme.rest.dto.ContatoEmergencia.ContatoEmergenciaDTO;
import br.edu.ifpb.es.cuidarme.rest.dto.Endereco.EnderecoDTO;
import br.edu.ifpb.es.cuidarme.rest.dto.Prontuario.ProntuarioResponseDTO;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class PacienteResponseDTO {

        private UUID lookupId;
        private String nome;
        private String cpf;
        private String rg;
        private LocalDate dataNascimento;
        private String sexo;
        private String estadoCivil;
        private String grauInstrucao;
        private String profissao;
        private String telefone;
        private EnderecoDTO enderecoPessoal;
        private EnderecoDTO enderecoTrabalho;
        private String infoAdicionais;
        private ContatoEmergenciaDTO contatoEmergencia;
        private LocalDateTime atualizadoEm;
        @Getter
        @Setter
        private List<ProntuarioResponseDTO> prontuarios;

}