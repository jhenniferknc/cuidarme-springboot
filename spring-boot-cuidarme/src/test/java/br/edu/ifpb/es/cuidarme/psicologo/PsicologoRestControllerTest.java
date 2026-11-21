package br.edu.ifpb.es.cuidarme.psicologo;

import br.edu.ifpb.es.cuidarme.exception.NaoAutorizadoException;
import br.edu.ifpb.es.cuidarme.mapper.AtendimentoMapper;
import br.edu.ifpb.es.cuidarme.mapper.PacienteMapper;
import br.edu.ifpb.es.cuidarme.mapper.PsicologoMapper;
import br.edu.ifpb.es.cuidarme.model.Psicologo;
import br.edu.ifpb.es.cuidarme.rest.PsicologoRestController;
import br.edu.ifpb.es.cuidarme.rest.dto.Psicologo.PsicologoLoginRequestDTO;
import br.edu.ifpb.es.cuidarme.rest.dto.Psicologo.PsicologoSalvarRequestDTO;
import br.edu.ifpb.es.cuidarme.service.PacienteService;
import br.edu.ifpb.es.cuidarme.service.PsicologoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({ PsicologoRestController.class })
@Import(PsicologoMapper.class) 
class PsicologoRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PsicologoService psicologoService;

    @MockitoBean
    private PacienteService pacienteService;

    @MockitoBean
    private PacienteMapper pacienteMapper;

    @MockitoBean
    private AtendimentoMapper atendimentoMapper;

    private Psicologo psicologoSalvo() {
        Psicologo p = new Psicologo();
        p.setLookupId(UUID.randomUUID());
        p.setNome("Ana");
        p.setEmail("ana@email.com");
        p.setSenha("$2a$10$hashqualquer");
        return p;
    }

    private String json(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    private PsicologoSalvarRequestDTO cadastro(String email, String senha) {
        PsicologoSalvarRequestDTO dto = new PsicologoSalvarRequestDTO();
        dto.setNome("Ana");
        dto.setEmail(email);
        dto.setSenha(senha);
        return dto;
    }

    private PsicologoLoginRequestDTO login(String email, String senha) {
        PsicologoLoginRequestDTO dto = new PsicologoLoginRequestDTO();
        dto.setEmail(email);
        dto.setSenha(senha);
        return dto;
    }

    @Test
    void loginValidoNaoDeveDevolverSenha() throws Exception {
        Psicologo p = psicologoSalvo();
        when(psicologoService.validarLogin("ana@email.com", "senha1234")).thenReturn(p);

        mockMvc.perform(post("/psicologos/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(login("ana@email.com", "senha1234"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lookupId").value(p.getLookupId().toString()))
                .andExpect(jsonPath("$.email").value("ana@email.com"))
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    void loginInvalidoDeveRetornar401() throws Exception {
        when(psicologoService.validarLogin(any(), any()))
                .thenThrow(new NaoAutorizadoException("Email ou senha inválidos."));

        mockMvc.perform(post("/psicologos/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(login("ana@email.com", "errada123"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Email ou senha inválidos."));
    }

    @Test
    void loginSemSenhaDeveRetornar400() throws Exception {
        mockMvc.perform(post("/psicologos/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(login("ana@email.com", ""))))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(psicologoService);
    }

    @Test
    void cadastroValidoNaoDeveDevolverSenha() throws Exception {
        when(psicologoService.criar(any(Psicologo.class))).thenReturn(psicologoSalvo());

        mockMvc.perform(post("/psicologos/cadastrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(cadastro("ana@email.com", "senha1234"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Ana"))
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    void cadastroComSenhaFracaDeveRetornar400() throws Exception {
        mockMvc.perform(post("/psicologos/cadastrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(cadastro("ana@email.com", "abcdefgh"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.senha").exists());

        verifyNoInteractions(psicologoService);
    }

    @Test
    void cadastroComEmailInvalidoDeveRetornar400() throws Exception {
        mockMvc.perform(post("/psicologos/cadastrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(cadastro("nao-e-email", "senha1234"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.email").exists());

        verifyNoInteractions(psicologoService);
    }

}
