package br.edu.ifpb.es.cuidarme.psicologo;

import br.edu.ifpb.es.cuidarme.exception.NaoAutorizadoException;
import br.edu.ifpb.es.cuidarme.model.Psicologo;
import br.edu.ifpb.es.cuidarme.repository.PsicologoRepository;
import br.edu.ifpb.es.cuidarme.service.PsicologoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PsicologoServiceTest {

    @Mock
    private PsicologoRepository repository;

    @Spy
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @InjectMocks
    private PsicologoService service;

    private Psicologo psicologoComSenha(String senha) {
        Psicologo p = new Psicologo();
        p.setNome("Ana");
        p.setEmail("ana@email.com");
        p.setSenha(senha);
        return p;
    }

    @Test
    void criarDeveGuardarHashENaoASenhaPura() {
        when(repository.save(any(Psicologo.class))).thenAnswer(inv -> inv.getArgument(0));

        Psicologo salvo = service.criar(psicologoComSenha("senha1234"));

        assertNotEquals("senha1234", salvo.getSenha());
        assertTrue(salvo.getSenha().startsWith("$2"));
        assertTrue(passwordEncoder.matches("senha1234", salvo.getSenha()));
    }

    @Test
    void loginComSenhaCorretaDeveRetornarPsicologo() throws Exception {
        Psicologo p = psicologoComSenha(passwordEncoder.encode("senha1234"));
        when(repository.findByEmailIgnoreCase("ana@email.com")).thenReturn(Optional.of(p));

        assertSame(p, service.validarLogin("ana@email.com", "senha1234"));
        verify(repository, never()).save(any());
    }

    @Test
    void loginComSenhaErradaDeveLancarNaoAutorizado() {
        Psicologo p = psicologoComSenha(passwordEncoder.encode("senha1234"));
        when(repository.findByEmailIgnoreCase("ana@email.com")).thenReturn(Optional.of(p));

        assertThrows(NaoAutorizadoException.class,
                () -> service.validarLogin("ana@email.com", "outraSenha1"));
    }

    @Test
    void loginComEmailInexistenteDeveLancarNaoAutorizado() {
        when(repository.findByEmailIgnoreCase("x@email.com")).thenReturn(Optional.empty());

        assertThrows(NaoAutorizadoException.class,
                () -> service.validarLogin("x@email.com", "senha1234"));
    }

    @Test
    void loginDeContaAntigaComSenhaPuraDeveConverterParaHash() throws Exception {
        Psicologo p = psicologoComSenha("senha1234");
        when(repository.findByEmailIgnoreCase("ana@email.com")).thenReturn(Optional.of(p));
        when(repository.save(any(Psicologo.class))).thenAnswer(inv -> inv.getArgument(0));

        Psicologo resultado = service.validarLogin("ana@email.com", "senha1234");

        verify(repository).save(p);
        assertTrue(passwordEncoder.matches("senha1234", resultado.getSenha()));
    }

    @Test
    void loginDeContaAntigaComSenhaErradaNaoDeveConverter() {
        Psicologo p = psicologoComSenha("senha1234");
        when(repository.findByEmailIgnoreCase("ana@email.com")).thenReturn(Optional.of(p));

        assertThrows(NaoAutorizadoException.class,
                () -> service.validarLogin("ana@email.com", "errada123"));
        verify(repository, never()).save(any());
        assertEquals("senha1234", p.getSenha());
    }

}
