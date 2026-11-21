package br.edu.ifpb.es.cuidarme.mapper;

import br.edu.ifpb.es.cuidarme.model.Atendimento;
import br.edu.ifpb.es.cuidarme.rest.dto.Atendimento.AtendimentoResponseDTO;
import br.edu.ifpb.es.cuidarme.rest.dto.Atendimento.AtendimentoSalvarRequestDTO;
import br.edu.ifpb.es.cuidarme.service.PacienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AtendimentoMapper {

    @Autowired
    private PacienteService pacienteService;

    @Autowired
    private PacienteMapper pacienteMapper;

    public Atendimento from(AtendimentoSalvarRequestDTO from) {
        Atendimento atendimento = new Atendimento();
        atendimento.setLookupId(from.getLookupId());
        atendimento.setData(from.getData());
        atendimento.setLocalidade(from.getLocalidade());
        atendimento.setStatus(from.getStatus());

        return atendimento;
    }

    public AtendimentoResponseDTO from(Atendimento from) {
        AtendimentoResponseDTO atendimentoResponseDTO = new AtendimentoResponseDTO();
        atendimentoResponseDTO.setLookupId(from.getLookupId());
        atendimentoResponseDTO.setData(from.getData());
        atendimentoResponseDTO.setLocalidade(from.getLocalidade());
        atendimentoResponseDTO.setStatus(from.getStatus());
        atendimentoResponseDTO.setAtualizadoEm(from.getAtualizadoEm());
        if (from.getPaciente() != null) {
            atendimentoResponseDTO.setPacienteId(from.getPaciente().getLookupId());
            atendimentoResponseDTO.setPacienteNome(from.getPaciente().getNome());
        }

        return atendimentoResponseDTO;
    }

}