package br.edu.ifpb.es.cuidarme.psicologo;

import br.edu.ifpb.es.cuidarme.exception.ConflitoException;
import br.edu.ifpb.es.cuidarme.exception.NaoAutorizadoException;
import br.edu.ifpb.es.cuidarme.mapper.AtendimentoMapper;
import br.edu.ifpb.es.cuidarme.mapper.PacienteMapper;
import br.edu.ifpb.es.cuidarme.mapper.PsicologoMapper;
import br.edu.ifpb.es.cuidarme.model.Paciente;
import br.edu.ifpb.es.cuidarme.model.Psicologo;
import br.edu.ifpb.es.cuidarme.rest.PsicologoRestController;
import br.edu.ifpb.es.cuidarme.rest.dto.Paciente.PacienteResponseDTO;
import br.edu.ifpb.es.cuidarme.rest.dto.Paciente.PacienteSalvarRequestDTO;
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

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

    private static final String CORPO_PACIENTE = """
            {
              "lookupId": "%s",
              "nome": "Maria Souza", "cpf": "12345678901", "rg": "1234567",
              "dataNascimento": "1990-05-10", "sexo": "Feminino", "estadoCivil": "Solteira",
              "grauInstrucao": "Superior completo", "profissao": "Professora", "telefone": "83999998888",
              "enderecoPessoal": {"cep": "58135000", "logradouro": "Rua A", "numero": "10",
                                  "cidade": "Esperança", "estado": "PB"}
            }
            """;

    private PacienteResponseDTO respostaPaciente(UUID id) {
        PacienteResponseDTO dto = new PacienteResponseDTO();
        dto.setLookupId(id);
        dto.setNome("Maria Souza");
        return dto;
    }

    @Test
    void cadastrarPacienteNovoComIdDoAppDeveRetornar201() throws Exception {
        Psicologo psicologo = psicologoSalvo();
        UUID pacienteId = UUID.randomUUID();
        Paciente mapeado = new Paciente();
        mapeado.setLookupId(pacienteId);

        when(psicologoService.buscarPor(psicologo.getLookupId())).thenReturn(Optional.of(psicologo));
        when(pacienteService.buscarPor(pacienteId)).thenReturn(Optional.empty());
        when(pacienteMapper.from(any(PacienteSalvarRequestDTO.class))).thenReturn(mapeado);
        when(pacienteService.criar(mapeado)).thenReturn(mapeado);
        when(pacienteMapper.from(mapeado)).thenReturn(respostaPaciente(pacienteId));

        mockMvc.perform(post("/psicologos/cadastrar-pacientes/{id}", psicologo.getLookupId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CORPO_PACIENTE.formatted(pacienteId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.lookupId").value(pacienteId.toString()));

        verify(pacienteService).criar(mapeado);
    }

    @Test
    void reenvioDoMesmoPacienteDeveRetornar200SemCriarDeNovo() throws Exception {
        Psicologo psicologo = psicologoSalvo();
        UUID pacienteId = UUID.randomUUID();
        Paciente existente = new Paciente();
        existente.setLookupId(pacienteId);
        existente.setPsicologo(psicologo);

        when(psicologoService.buscarPor(psicologo.getLookupId())).thenReturn(Optional.of(psicologo));
        when(pacienteService.buscarPor(pacienteId)).thenReturn(Optional.of(existente));
        when(pacienteMapper.from(existente)).thenReturn(respostaPaciente(pacienteId));

        mockMvc.perform(post("/psicologos/cadastrar-pacientes/{id}", psicologo.getLookupId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CORPO_PACIENTE.formatted(pacienteId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lookupId").value(pacienteId.toString()));

        verify(pacienteService, never()).criar(any());
    }

    @Test
    void idDePacienteDeOutroPsicologoDeveRetornar409() throws Exception {
        Psicologo psicologo = psicologoSalvo();
        Psicologo outro = psicologoSalvo();
        UUID pacienteId = UUID.randomUUID();
        Paciente existente = new Paciente();
        existente.setLookupId(pacienteId);
        existente.setPsicologo(outro);

        when(psicologoService.buscarPor(psicologo.getLookupId())).thenReturn(Optional.of(psicologo));
        when(pacienteService.buscarPor(pacienteId)).thenReturn(Optional.of(existente));

        mockMvc.perform(post("/psicologos/cadastrar-pacientes/{id}", psicologo.getLookupId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CORPO_PACIENTE.formatted(pacienteId)))
                .andExpect(status().isConflict());

        verify(pacienteService, never()).criar(any());
    }

    @Test
    void cadastrarPacienteComCpfDuplicadoDeveRetornar409() throws Exception {
        Psicologo psicologo = psicologoSalvo();
        UUID pacienteId = UUID.randomUUID();

        when(psicologoService.buscarPor(psicologo.getLookupId())).thenReturn(Optional.of(psicologo));
        when(pacienteService.buscarPor(pacienteId)).thenReturn(Optional.empty());
        when(pacienteMapper.from(any(PacienteSalvarRequestDTO.class))).thenReturn(new Paciente());
        when(pacienteService.criar(any(Paciente.class)))
                .thenThrow(new ConflitoException("Já existe um paciente cadastrado com este CPF."));

        mockMvc.perform(post("/psicologos/cadastrar-pacientes/{id}", psicologo.getLookupId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CORPO_PACIENTE.formatted(pacienteId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Já existe um paciente cadastrado com este CPF."));
    }

}
