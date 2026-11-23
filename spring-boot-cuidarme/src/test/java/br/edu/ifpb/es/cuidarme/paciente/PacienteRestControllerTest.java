package br.edu.ifpb.es.cuidarme.paciente;

import br.edu.ifpb.es.cuidarme.exception.ConflitoException;
import br.edu.ifpb.es.cuidarme.mapper.AtendimentoMapper;
import br.edu.ifpb.es.cuidarme.mapper.ContatoEmergenciaMapper;
import br.edu.ifpb.es.cuidarme.mapper.EnderecoMapper;
import br.edu.ifpb.es.cuidarme.mapper.PacienteMapper;
import br.edu.ifpb.es.cuidarme.mapper.ProntuarioMapper;
import br.edu.ifpb.es.cuidarme.model.Atendimento;
import br.edu.ifpb.es.cuidarme.model.Endereco;
import br.edu.ifpb.es.cuidarme.model.Paciente;
import br.edu.ifpb.es.cuidarme.rest.PacienteRestController;
import br.edu.ifpb.es.cuidarme.rest.dto.Atendimento.AtendimentoResponseDTO;
import br.edu.ifpb.es.cuidarme.rest.dto.Atendimento.AtendimentoSalvarRequestDTO;
import br.edu.ifpb.es.cuidarme.service.AtendimentoService;
import br.edu.ifpb.es.cuidarme.service.PacienteService;
import br.edu.ifpb.es.cuidarme.service.ProntuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({ PacienteRestController.class })
// mappers reais p testar os campos opcionais 
@Import({ PacienteMapper.class, EnderecoMapper.class, ContatoEmergenciaMapper.class })
class PacienteRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PacienteService pacienteService;

    @MockitoBean
    private AtendimentoService atendimentoService;

    @MockitoBean
    private AtendimentoMapper atendimentoMapper;

    @MockitoBean
    private ProntuarioService prontuarioService;

    @MockitoBean
    private ProntuarioMapper prontuarioMapper;

    private static final String CORPO_SEM_OPCIONAIS = """
            {
              "nome": "Maria Souza",
              "cpf": "12345678901",
              "rg": "1234567",
              "dataNascimento": "1990-05-10",
              "sexo": "Feminino",
              "estadoCivil": "Solteira",
              "grauInstrucao": "Superior completo",
              "profissao": "Professora",
              "telefone": "83999998888",
              "enderecoPessoal": {
                "cep": "58135000", "logradouro": "Rua A", "numero": "10",
                "cidade": "Esperança", "estado": "PB"
              }
            }
            """;

    private Paciente pacienteExistente(UUID id) {
        Paciente p = new Paciente();
        p.setLookupId(id);
        p.setNome("Antigo");
        p.setCpf("12345678901");
        p.setDataNascimento(LocalDate.of(1990, 5, 10));
        Endereco e = new Endereco();
        e.setCep("58135000");
        p.setEnderecoPessoal(e);
        return p;
    }

    @Test
    void removerPacienteExistenteDeveRetornar204() throws Exception {
        UUID id = UUID.randomUUID();
        Paciente p = pacienteExistente(id);
        when(pacienteService.buscarPor(id)).thenReturn(Optional.of(p));

        mockMvc.perform(delete("/pacientes/remover/{id}", id))
                .andExpect(status().isNoContent());

        verify(pacienteService).remover(p);
    }

    @Test
    void removerPacienteInexistenteDeveRetornar204SemRemover() throws Exception {
        UUID id = UUID.randomUUID();
        when(pacienteService.buscarPor(id)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/pacientes/remover/{id}", id))
                .andExpect(status().isNoContent());

        verify(pacienteService, never()).remover(any());
    }

    @Test
    void atualizarSemCamposOpcionaisDeveRetornar200() throws Exception {
        UUID id = UUID.randomUUID();
        when(pacienteService.buscarPor(id)).thenReturn(Optional.of(pacienteExistente(id)));
        when(pacienteService.atualizar(any(Paciente.class))).thenAnswer(inv -> {
            Paciente p = inv.getArgument(0);
            p.setAtualizadoEm(LocalDateTime.of(2026, 11, 21, 10, 0));
            return p;
        });

        mockMvc.perform(patch("/pacientes/atualizar/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CORPO_SEM_OPCIONAIS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lookupId").value(id.toString()))
                .andExpect(jsonPath("$.nome").value("Maria Souza"))
                .andExpect(jsonPath("$.enderecoTrabalho").doesNotExist())
                .andExpect(jsonPath("$.contatoEmergencia").doesNotExist())
                .andExpect(jsonPath("$.atualizadoEm").value("2026-11-21T10:00:00"));
    }

    @Test
    void atualizarComOpcionaisVaziosDeveGuardarNull() throws Exception {
        UUID id = UUID.randomUUID();
        when(pacienteService.buscarPor(id)).thenReturn(Optional.of(pacienteExistente(id)));
        when(pacienteService.atualizar(any(Paciente.class))).thenAnswer(inv -> inv.getArgument(0));

        String corpo = CORPO_SEM_OPCIONAIS.replace("\"enderecoPessoal\"", """
                "enderecoTrabalho": {"cep": "", "logradouro": "", "numero": "", "cidade": "", "estado": ""},
                "contatoEmergencia": {"nome": "", "telefone": "", "parentesco": ""},
                "enderecoPessoal\"""");

        mockMvc.perform(patch("/pacientes/atualizar/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enderecoTrabalho").doesNotExist())
                .andExpect(jsonPath("$.contatoEmergencia").doesNotExist());
    }

    @Test
    void atualizarComEnderecoPessoalIncompletoDeveRetornar400() throws Exception {
        UUID id = UUID.randomUUID();
        String corpo = CORPO_SEM_OPCIONAIS.replace("\"cidade\": \"Esperança\"", "\"cidade\": \"\"");

        mockMvc.perform(patch("/pacientes/atualizar/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.enderecoPessoalCompleto").exists());

        verifyNoInteractions(pacienteService);
    }

    @Test
    void atualizarPacienteInexistenteDeveRetornar404() throws Exception {
        UUID id = UUID.randomUUID();
        when(pacienteService.buscarPor(id)).thenReturn(Optional.empty());

        mockMvc.perform(patch("/pacientes/atualizar/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CORPO_SEM_OPCIONAIS))
                .andExpect(status().isNotFound());
    }

    @Test
    void atualizarComCpfDeOutroPacienteDeveRetornar409() throws Exception {
        UUID id = UUID.randomUUID();
        when(pacienteService.buscarPor(id)).thenReturn(Optional.of(pacienteExistente(id)));
        when(pacienteService.atualizar(any(Paciente.class)))
                .thenThrow(new ConflitoException("Já existe um paciente cadastrado com este CPF."));

        mockMvc.perform(patch("/pacientes/atualizar/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CORPO_SEM_OPCIONAIS))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Já existe um paciente cadastrado com este CPF."));
    }

    private static final String CORPO_ATENDIMENTO = """
            {"lookupId": "%s", "data": "2026-12-01T14:00:00", "localidade": "Online", "status": "AGENDADO"}
            """;

    private AtendimentoResponseDTO respostaAtendimento(UUID id) {
        AtendimentoResponseDTO dto = new AtendimentoResponseDTO();
        dto.setLookupId(id);
        return dto;
    }

    @Test
    void cadastrarAtendimentoNovoDeveRetornar201() throws Exception {
        UUID pacienteId = UUID.randomUUID();
        UUID atendimentoId = UUID.randomUUID();
        Atendimento mapeado = new Atendimento();

        when(pacienteService.buscarPor(pacienteId)).thenReturn(Optional.of(pacienteExistente(pacienteId)));
        when(atendimentoService.buscarPor(atendimentoId)).thenReturn(Optional.empty());
        when(atendimentoMapper.from(any(AtendimentoSalvarRequestDTO.class))).thenReturn(mapeado);
        when(atendimentoService.criar(mapeado)).thenReturn(mapeado);
        when(atendimentoMapper.from(mapeado)).thenReturn(respostaAtendimento(atendimentoId));

        mockMvc.perform(post("/pacientes/cadastrar-atendimentos/{id}", pacienteId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CORPO_ATENDIMENTO.formatted(atendimentoId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.lookupId").value(atendimentoId.toString()));

        verify(atendimentoService).criar(mapeado);
    }

    @Test
    void reenvioDoMesmoAtendimentoDeveRetornar200SemCriarDeNovo() throws Exception {
        UUID pacienteId = UUID.randomUUID();
        UUID atendimentoId = UUID.randomUUID();
        Paciente paciente = pacienteExistente(pacienteId);
        Atendimento existente = new Atendimento();
        existente.setLookupId(atendimentoId);
        existente.setPaciente(paciente);

        when(pacienteService.buscarPor(pacienteId)).thenReturn(Optional.of(paciente));
        when(atendimentoService.buscarPor(atendimentoId)).thenReturn(Optional.of(existente));
        when(atendimentoMapper.from(existente)).thenReturn(respostaAtendimento(atendimentoId));

        mockMvc.perform(post("/pacientes/cadastrar-atendimentos/{id}", pacienteId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CORPO_ATENDIMENTO.formatted(atendimentoId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lookupId").value(atendimentoId.toString()));

        verify(atendimentoService, never()).criar(any());
    }

    @Test
    void idDeAtendimentoDeOutroPacienteDeveRetornar409() throws Exception {
        UUID pacienteId = UUID.randomUUID();
        UUID atendimentoId = UUID.randomUUID();
        Atendimento existente = new Atendimento();
        existente.setLookupId(atendimentoId);
        existente.setPaciente(pacienteExistente(UUID.randomUUID()));

        when(pacienteService.buscarPor(pacienteId)).thenReturn(Optional.of(pacienteExistente(pacienteId)));
        when(atendimentoService.buscarPor(atendimentoId)).thenReturn(Optional.of(existente));

        mockMvc.perform(post("/pacientes/cadastrar-atendimentos/{id}", pacienteId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CORPO_ATENDIMENTO.formatted(atendimentoId)))
                .andExpect(status().isConflict());

        verify(atendimentoService, never()).criar(any());
    }

}
