package br.edu.ifpb.es.cuidarme.mapper;

import br.edu.ifpb.es.cuidarme.model.Paciente;
import br.edu.ifpb.es.cuidarme.model.Prontuario;
import br.edu.ifpb.es.cuidarme.rest.dto.Paciente.PacienteSalvarRequestDTO;
import br.edu.ifpb.es.cuidarme.rest.dto.Paciente.PacienteResponseDTO;
import br.edu.ifpb.es.cuidarme.rest.dto.Prontuario.ProntuarioResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PacienteMapper {

    @Autowired
    private ProntuarioMapper prontuarioMapper;
    @Autowired
    private EnderecoMapper enderecoMapper;
    @Autowired
    private ContatoEmergenciaMapper contatoEmergenciaMapper;

    public Paciente from(PacienteSalvarRequestDTO from) {
        Paciente paciente = new Paciente();
        paciente.setLookupId(from.getLookupId());
        paciente.setNome(from.getNome());
        paciente.setCpf(from.getCpf());
        paciente.setRg(from.getRg());
        paciente.setDataNascimento(from.getDataNascimento());
        paciente.setSexo(from.getSexo());
        paciente.setEstadoCivil(from.getEstadoCivil());
        paciente.setGrauInstrucao(from.getGrauInstrucao());
        paciente.setProfissao(from.getProfissao());
        paciente.setTelefone(from.getTelefone());
        paciente.setEnderecoPessoal(enderecoMapper.from(from.getEnderecoPessoal()));
        paciente.setEnderecoTrabalho(enderecoMapper.from(from.getEnderecoTrabalho()));
        paciente.setInfoAdicionais(from.getInfoAdicionais());
        paciente.setContatoEmergencia(contatoEmergenciaMapper.from(from.getContatoEmergencia()));
        return paciente;
    }

    public PacienteResponseDTO from(Paciente from) {
        PacienteResponseDTO pacienteResponseDTO = new PacienteResponseDTO();
        pacienteResponseDTO.setLookupId(from.getLookupId());
        pacienteResponseDTO.setNome(from.getNome());
        pacienteResponseDTO.setCpf(from.getCpf());
        pacienteResponseDTO.setRg(from.getRg());
        pacienteResponseDTO.setDataNascimento(from.getDataNascimento());
        pacienteResponseDTO.setSexo(from.getSexo());
        pacienteResponseDTO.setEstadoCivil(from.getEstadoCivil());
        pacienteResponseDTO.setGrauInstrucao(from.getGrauInstrucao());
        pacienteResponseDTO.setProfissao(from.getProfissao());
        pacienteResponseDTO.setTelefone(from.getTelefone());
        pacienteResponseDTO.setEnderecoPessoal(enderecoMapper.from(from.getEnderecoPessoal()));
        pacienteResponseDTO.setEnderecoTrabalho(enderecoMapper.from(from.getEnderecoTrabalho()));
        pacienteResponseDTO.setInfoAdicionais(from.getInfoAdicionais());
        pacienteResponseDTO.setContatoEmergencia(contatoEmergenciaMapper.from(from.getContatoEmergencia()));
        pacienteResponseDTO.setAtualizadoEm(from.getAtualizadoEm());

        if (from.getProntuarios() != null) {
            List<Prontuario> prontuarios = from.getProntuarios();

            List<ProntuarioResponseDTO> prontuariosDTO = prontuarios.stream()
                    .map(prontuarioMapper::from)
                    .collect(Collectors.toList());

            pacienteResponseDTO.setProntuarios(prontuariosDTO);
        }
        return pacienteResponseDTO;
    }
}