package br.edu.ifpb.es.cuidarme.rest;

import br.edu.ifpb.es.cuidarme.model.Prontuario;
import br.edu.ifpb.es.cuidarme.rest.dto.Paciente.PacienteIdDTO;
import br.edu.ifpb.es.cuidarme.rest.dto.Prontuario.ProntuarioBuscarDTO;
import br.edu.ifpb.es.cuidarme.rest.dto.Prontuario.ProntuarioResponseDTO;
import br.edu.ifpb.es.cuidarme.rest.dto.Prontuario.ProntuarioSalvarRequestDTO;
import br.edu.ifpb.es.cuidarme.mapper.ProntuarioMapper;
import br.edu.ifpb.es.cuidarme.service.ProntuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({ ProntuarioRestController.class })
@AutoConfigureMockMvc
public class ProntuarioRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProntuarioService prontuarioService;

    @MockitoBean
    private ProntuarioMapper prontuarioMapper;

    @Test
    void deveRetornarTodosOsProntuariosAoListar() throws Exception {
        Prontuario p1 = mock(Prontuario.class);
        Prontuario p2 = mock(Prontuario.class);
        when(prontuarioService.recuperarTodos()).thenReturn(Arrays.asList(p1, p2));

        ProntuarioResponseDTO dto1 = new ProntuarioResponseDTO();
        dto1.setLookupId(UUID.randomUUID());
        dto1.setDescricao("Prontuario 1");

        ProntuarioResponseDTO dto2 = new ProntuarioResponseDTO();
        dto2.setLookupId(UUID.randomUUID());
        dto2.setDescricao("Prontuario 2");

        when(prontuarioMapper.from(p1)).thenReturn(dto1);
        when(prontuarioMapper.from(p2)).thenReturn(dto2);

        mockMvc.perform(get("/prontuarios/listar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].descricao").value("Prontuario 1"))
                .andExpect(jsonPath("$[1].descricao").value("Prontuario 2"));
    }

    @Test
    void deveBuscarProntuarioPorIdComSucesso() throws Exception {
        UUID id = UUID.randomUUID();
        Prontuario p = mock(Prontuario.class);
        when(prontuarioService.buscarPor(id)).thenReturn(Optional.of(p));

        ProntuarioResponseDTO dto = new ProntuarioResponseDTO();
        dto.setLookupId(id);
        dto.setDescricao("Consulta de rotina");
        when(prontuarioMapper.from(p)).thenReturn(dto);

        mockMvc.perform(get("/prontuarios/buscar/{prontuarioId}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lookupId").value(id.toString()))
                .andExpect(jsonPath("$.descricao").value("Consulta de rotina"));
    }

    @Test
    void deveLancarExcecaoAoBuscarProntuarioInexistente() throws Exception {
        UUID id = UUID.randomUUID();
        when(prontuarioService.buscarPor(id)).thenReturn(Optional.empty());

        mockMvc.perform(get("/prontuarios/buscar/{prontuarioId}", id))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.detail").value("Prontuário com o ID " + id + " não foi encontrado."));
    }

    @Test
    void deveAtualizarProntuarioComSucesso() throws Exception {
        UUID id = UUID.randomUUID();
        Prontuario pExistente = mock(Prontuario.class);
        Prontuario pAtualizado = mock(Prontuario.class);

        ProntuarioSalvarRequestDTO requestDTO = new ProntuarioSalvarRequestDTO();
        requestDTO.setDescricao("Descrição atualizada");
        requestDTO.setDataRegistro(LocalDateTime.now());

        PacienteIdDTO pacienteIdDTO = new PacienteIdDTO();
        pacienteIdDTO.setLookupId(UUID.randomUUID());
        requestDTO.setPaciente(pacienteIdDTO);

        when(prontuarioService.buscarPor(id)).thenReturn(Optional.of(pExistente));
        when(prontuarioService.atualizar(pExistente)).thenReturn(pAtualizado);

        ProntuarioResponseDTO responseDTO = new ProntuarioResponseDTO();
        responseDTO.setLookupId(id);
        responseDTO.setDescricao("Descrição atualizada");
        when(prontuarioMapper.from(pAtualizado)).thenReturn(responseDTO);

        mockMvc.perform(patch("/prontuarios/atualizar/{prontuarioId}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").value("Descrição atualizada"));

        verify(pExistente).setDescricao(requestDTO.getDescricao());
        verify(pExistente).setDataRegistro(requestDTO.getDataRegistro());
    }

    @Test
    void deveRetornarErroDeValidacaoAoAtualizarComDadosInvalidos() throws Exception {
        UUID id = UUID.randomUUID();
        ProntuarioSalvarRequestDTO requestDTOInvalido = new ProntuarioSalvarRequestDTO();

        mockMvc.perform(patch("/prontuarios/atualizar/{prontuarioId}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTOInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.descricao").exists())
                .andExpect(jsonPath("$.erros.dataRegistro").exists())
                .andExpect(jsonPath("$.erros.paciente").exists());
    }

    @Test
    void deveRemoverProntuarioExistenteComSucesso() throws Exception {
        UUID id = UUID.randomUUID();
        Prontuario p = mock(Prontuario.class);
        when(prontuarioService.buscarPor(id)).thenReturn(Optional.of(p));

        mockMvc.perform(delete("/prontuarios/remover/{prontuarioId}", id))
                .andExpect(status().isNoContent());

        verify(prontuarioService).remover(p);
    }

    @Test
    void deveBuscarProntuariosPaginadosComQueryParams() throws Exception {
        Prontuario p1 = mock(Prontuario.class);
        Prontuario p2 = mock(Prontuario.class);

        ProntuarioResponseDTO dto1 = new ProntuarioResponseDTO();
        dto1.setLookupId(UUID.randomUUID());
        dto1.setDescricao("Prontuario Paginado 1");

        ProntuarioResponseDTO dto2 = new ProntuarioResponseDTO();
        dto2.setLookupId(UUID.randomUUID());
        dto2.setDescricao("Prontuario Paginado 2");

        when(prontuarioMapper.from(p1)).thenReturn(dto1);
        when(prontuarioMapper.from(p2)).thenReturn(dto2);

        List<Prontuario> lista = Arrays.asList(p1, p2);
        Page<Prontuario> paginaMock = new PageImpl<>(lista, PageRequest.of(0, 2), 2);

        when(prontuarioService.buscar(any(ProntuarioBuscarDTO.class))).thenReturn(paginaMock);

        mockMvc.perform(get("/prontuarios/buscar-paginado")
                        .param("numeroPagina", "0")
                        .param("tamanhoPagina", "2")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2)) // O Spring Data Page coloca o array dentro do atributo "content"
                .andExpect(jsonPath("$.content[0].descricao").value("Prontuario Paginado 1"))
                .andExpect(jsonPath("$.content[1].descricao").value("Prontuario Paginado 2"));
    }
}