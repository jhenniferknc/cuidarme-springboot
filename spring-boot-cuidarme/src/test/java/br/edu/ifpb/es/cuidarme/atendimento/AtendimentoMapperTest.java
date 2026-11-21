package br.edu.ifpb.es.cuidarme.atendimento;

import br.edu.ifpb.es.cuidarme.mapper.AtendimentoMapper;
import br.edu.ifpb.es.cuidarme.model.Atendimento;
import br.edu.ifpb.es.cuidarme.model.Paciente;
import br.edu.ifpb.es.cuidarme.model.StatusAtendimento;
import br.edu.ifpb.es.cuidarme.rest.dto.Atendimento.AtendimentoResponseDTO;
import br.edu.ifpb.es.cuidarme.rest.dto.Atendimento.AtendimentoSalvarRequestDTO;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AtendimentoMapperTest {

    private final AtendimentoMapper mapper = new AtendimentoMapper();

    @Test
    void respostaDeveTrazerIdENomeDoPaciente() {
        Paciente paciente = new Paciente();
        paciente.setLookupId(UUID.randomUUID());
        paciente.setNome("Maria Souza");

        Atendimento a = new Atendimento();
        a.setLookupId(UUID.randomUUID());
        a.setStatus(StatusAtendimento.AGENDADO);
        a.setPaciente(paciente);
        a.setAtualizadoEm(LocalDateTime.of(2026, 11, 21, 10, 0));

        AtendimentoResponseDTO dto = mapper.from(a);

        assertEquals(paciente.getLookupId(), dto.getPacienteId());
        assertEquals("Maria Souza", dto.getPacienteNome());
        assertEquals(a.getAtualizadoEm(), dto.getAtualizadoEm());
    }

    @Test
    void respostaSemPacienteNaoDeveQuebrar() {
        Atendimento a = new Atendimento();
        a.setLookupId(UUID.randomUUID());

        AtendimentoResponseDTO dto = mapper.from(a);

        assertNull(dto.getPacienteId());
        assertNull(dto.getPacienteNome());
    }

    @Test
    void cadastroDeveManterIdEnviadoPeloApp() {
        AtendimentoSalvarRequestDTO dto = new AtendimentoSalvarRequestDTO();
        dto.setLookupId(UUID.randomUUID());
        dto.setData(LocalDateTime.of(2026, 12, 1, 14, 0));
        dto.setLocalidade("Online");
        dto.setStatus(StatusAtendimento.AGENDADO);

        assertEquals(dto.getLookupId(), mapper.from(dto).getLookupId());
    }

}
