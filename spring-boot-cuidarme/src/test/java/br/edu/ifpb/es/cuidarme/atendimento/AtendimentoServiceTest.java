package br.edu.ifpb.es.cuidarme.atendimento;

import br.edu.ifpb.es.cuidarme.exception.EstadoInvalidoException;
import br.edu.ifpb.es.cuidarme.model.Atendimento;
import br.edu.ifpb.es.cuidarme.model.StatusAtendimento;
import br.edu.ifpb.es.cuidarme.repository.AtendimentoRepository;
import br.edu.ifpb.es.cuidarme.service.AtendimentoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AtendimentoServiceTest {

    @Mock
    private AtendimentoRepository repository;

    @InjectMocks
    private AtendimentoService service;

    private Atendimento atendimento(StatusAtendimento status) {
        Atendimento a = new Atendimento();
        a.setData(LocalDateTime.of(2026, 12, 1, 14, 0));
        a.setLocalidade("Online");
        a.setStatus(status);
        return a;
    }

    @Test
    void remarcarDeveMudarDataLocalidadeEStatus() throws Exception {
        Atendimento a = atendimento(StatusAtendimento.AGENDADO);
        when(repository.save(a)).thenReturn(a);
        LocalDateTime novaData = LocalDateTime.of(2026, 12, 3, 9, 30);

        Atendimento r = service.remarcar(a, novaData, "Consultório");

        assertEquals(novaData, r.getData());
        assertEquals("Consultório", r.getLocalidade());
        assertEquals(StatusAtendimento.REMARCADO, r.getStatus());
    }

    @Test
    void remarcarAtendimentoCanceladoDeveLancarEstadoInvalido() {
        Atendimento a = atendimento(StatusAtendimento.CANCELADO);

        assertThrows(EstadoInvalidoException.class,
                () -> service.remarcar(a, LocalDateTime.now().plusDays(1), "Online"));
        verify(repository, never()).save(any());
    }

    @Test
    void remarcarAtendimentoConcluidoDeveLancarEstadoInvalido() {
        Atendimento a = atendimento(StatusAtendimento.CONCLUIDO);

        assertThrows(EstadoInvalidoException.class,
                () -> service.remarcar(a, LocalDateTime.now().plusDays(1), "Online"));
        verify(repository, never()).save(any());
    }

    @Test
    void cancelarDeveMudarStatusParaCancelado() throws Exception {
        Atendimento a = atendimento(StatusAtendimento.REMARCADO);
        when(repository.save(a)).thenReturn(a);

        assertEquals(StatusAtendimento.CANCELADO, service.cancelar(a).getStatus());
    }

    @Test
    void cancelarDeNovoNaoDeveGravarNada() throws Exception {
        Atendimento a = atendimento(StatusAtendimento.CANCELADO);

        assertSame(a, service.cancelar(a));
        verify(repository, never()).save(any());
    }

    @Test
    void cancelarAtendimentoConcluidoDeveLancarEstadoInvalido() {
        Atendimento a = atendimento(StatusAtendimento.CONCLUIDO);

        assertThrows(EstadoInvalidoException.class, () -> service.cancelar(a));
        verify(repository, never()).save(any());
    }

}
