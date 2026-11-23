package br.edu.ifpb.es.cuidarme.paciente;

import br.edu.ifpb.es.cuidarme.exception.ConflitoException;
import br.edu.ifpb.es.cuidarme.model.Atendimento;
import br.edu.ifpb.es.cuidarme.model.Paciente;
import br.edu.ifpb.es.cuidarme.model.Psicologo;
import br.edu.ifpb.es.cuidarme.repository.PacienteRepository;
import br.edu.ifpb.es.cuidarme.service.PacienteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PacienteServiceTest {

    @Mock
    private PacienteRepository repository;

    @InjectMocks
    private PacienteService service;

    private Paciente paciente(UUID id, String cpf) {
        Paciente p = new Paciente();
        p.setLookupId(id);
        p.setNome("Maria");
        p.setCpf(cpf);
        return p;
    }

    @Test
    void criarSemIdComCpfLivreDeveSalvar() throws Exception {
        Paciente p = paciente(null, "12345678901");
        when(repository.existsByCpf("12345678901")).thenReturn(false);
        when(repository.save(p)).thenReturn(p);

        assertSame(p, service.criar(p));
    }

    @Test
    void criarComIdDoAppDeveVerificarCpfIgnorandoOProprioId() throws Exception {
        UUID id = UUID.randomUUID();
        Paciente p = paciente(id, "12345678901");
        when(repository.existsByCpfAndLookupIdNot("12345678901", id)).thenReturn(false);
        when(repository.save(p)).thenReturn(p);

        Paciente salvo = service.criar(p);

        assertEquals(id, salvo.getLookupId());
        verify(repository, never()).existsByCpf(any());
    }

    @Test
    void criarComCpfDuplicadoDeveLancarConflito() {
        Paciente p = paciente(null, "12345678901");
        when(repository.existsByCpf("12345678901")).thenReturn(true);

        assertThrows(ConflitoException.class, () -> service.criar(p));
        verify(repository, never()).save(any());
    }

    @Test
    void atualizarComCpfDeOutroPacienteDeveLancarConflito() {
        UUID id = UUID.randomUUID();
        Paciente p = paciente(id, "12345678901");
        when(repository.existsByCpfAndLookupIdNot("12345678901", id)).thenReturn(true);

        assertThrows(ConflitoException.class, () -> service.atualizar(p));
        verify(repository, never()).save(any());
    }

    @Test
    void atualizarMantendoOProprioCpfDeveSalvar() throws Exception {
        UUID id = UUID.randomUUID();
        Paciente p = paciente(id, "12345678901");
        when(repository.existsByCpfAndLookupIdNot("12345678901", id)).thenReturn(false);
        when(repository.save(p)).thenReturn(p);

        assertSame(p, service.atualizar(p));
    }


    @Test
    void removerDeveTirarOPacienteEOsAtendimentosDasListasDoPsicologo() {
        Psicologo psicologo = new Psicologo();
        Paciente removido = paciente(UUID.randomUUID(), "12345678901");
        Paciente outro = paciente(UUID.randomUUID(), "10987654321");
        removido.setPsicologo(psicologo);

        Atendimento doRemovido = new Atendimento();
        doRemovido.setPaciente(removido);
        Atendimento doOutro = new Atendimento();
        doOutro.setPaciente(outro);

        psicologo.getPacientes().add(removido);
        psicologo.getPacientes().add(outro);
        psicologo.getAtendimentos().add(doRemovido);
        psicologo.getAtendimentos().add(doOutro);

        service.remover(removido);

        assertEquals(1, psicologo.getPacientes().size());
        assertSame(outro, psicologo.getPacientes().get(0));
        assertEquals(1, psicologo.getAtendimentos().size());
        assertSame(doOutro, psicologo.getAtendimentos().get(0));
        verify(repository).delete(removido);
    }
}
