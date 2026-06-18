package br.edu.ifpb.es.cuidarme.rest;

import br.edu.ifpb.es.cuidarme.mapper.AtendimentoMapper;
import br.edu.ifpb.es.cuidarme.mapper.PagamentoMapper;
import br.edu.ifpb.es.cuidarme.model.*;
import br.edu.ifpb.es.cuidarme.rest.dto.Atendimento.AtendimentoResponseDTO;
import br.edu.ifpb.es.cuidarme.rest.dto.Atendimento.AtendimentoSalvarRequestDTO;
import br.edu.ifpb.es.cuidarme.rest.dto.Pagamento.PagamentoResponseDTO;
import br.edu.ifpb.es.cuidarme.rest.dto.Pagamento.PagamentoSalvarRequestDTO;
import br.edu.ifpb.es.cuidarme.service.AtendimentoService;
import br.edu.ifpb.es.cuidarme.service.PagamentoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({AtendimentoRestController.class})
@AutoConfigureMockMvc
public class AtendimentoRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AtendimentoService atendimentoService;

    @MockitoBean
    private AtendimentoMapper atendimentoMapper;

    @MockitoBean
    private PagamentoService pagamentoService;

    @MockitoBean
    private PagamentoMapper pagamentoMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void deveCadastrarPagamentoAtreladoAoAtendimento() throws Exception {
        UUID atendimentoId = UUID.randomUUID();

        Paciente paciente = new Paciente();
        Atendimento atendimento = mock(Atendimento.class);
        when(atendimento.getPaciente()).thenReturn(paciente);

        PagamentoSalvarRequestDTO requestDTO = new PagamentoSalvarRequestDTO();
        requestDTO.setValor(150);
        requestDTO.setData(LocalDateTime.now());
        requestDTO.setMetodo(Metodo.PIX);
        requestDTO.setStatus("PAGO");

        Pagamento pagamentoMapeado = new Pagamento();
        Pagamento pagamentoSalvo = new Pagamento();
        pagamentoSalvo.setLookupId(UUID.randomUUID());

        PagamentoResponseDTO responseDTO = new PagamentoResponseDTO();
        responseDTO.setLookupId(pagamentoSalvo.getLookupId());
        responseDTO.setStatus(StatusPagamento.PAGO);

        when(atendimentoService.buscarPor(atendimentoId))
                .thenReturn(Optional.of(atendimento));

        when(pagamentoMapper.from(any(PagamentoSalvarRequestDTO.class)))
                .thenReturn(pagamentoMapeado);

        when(pagamentoService.criar(any(Pagamento.class)))
                .thenReturn(pagamentoSalvo);

        when(pagamentoMapper.from(pagamentoSalvo))
                .thenReturn(responseDTO);

        mockMvc.perform(post("/atendimentos/cadastrar-pagamento/{atendimentoId}", atendimentoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PAGO"));

        verify(atendimento).setStatus(StatusAtendimento.AGENDADO);
        verify(atendimentoService).atualizar(atendimento);
        verify(pagamentoService).criar(argThat(pagamento ->
                pagamento.getAtendimento() == atendimento &&
                        pagamento.getPaciente() == paciente &&
                        pagamento.getStatusPagamento() == StatusPagamento.PAGO
        ));
    }

    @Test
    void deveLancarExcecaoAoBuscarPagamentoInexistente() throws Exception {
        UUID idInexistente = UUID.randomUUID();

        PagamentoSalvarRequestDTO requestDTO = new PagamentoSalvarRequestDTO();
        requestDTO.setValor(100);
        requestDTO.setMetodo(Metodo.PIX);
        requestDTO.setStatus("PAGO");

        when(pagamentoService.buscarPor(idInexistente))
                .thenReturn(Optional.empty());

        mockMvc.perform(patch("/pagamentos/atualizar/{pagamentoId}", idInexistente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound());

        verify(pagamentoService, never()).criar(any(Pagamento.class));
    }

    @Test
    void deveRetornarTodosOsAtendimentos() throws Exception {
        Atendimento atendimento1 = mock(Atendimento.class);
        Atendimento atendimento2 = mock(Atendimento.class);

        when(atendimentoService.recuperarTodos()).thenReturn(List.of(atendimento1, atendimento2));

        AtendimentoResponseDTO dto1 = new AtendimentoResponseDTO();
        dto1.setLookupId(UUID.randomUUID());
        dto1.setLocalidade("Esperança-PB");

        AtendimentoResponseDTO dto2 = new AtendimentoResponseDTO();
        dto2.setLookupId(UUID.randomUUID());
        dto2.setLocalidade("Lagoa de Roça-PB");

        when(atendimentoMapper.from(atendimento1)).thenReturn(dto1);
        when(atendimentoMapper.from(atendimento2)).thenReturn(dto2);

        mockMvc.perform(get("/atendimentos/listar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].localidade").value("Esperança-PB"))
                .andExpect(jsonPath("$[1].localidade").value("Lagoa de Roça-PB"));
    }

    @Test
    void deveBuscarOsAtendimentosExistentes() throws Exception {
        UUID id = UUID.randomUUID();
        Atendimento a = mock(Atendimento.class);
        when(atendimentoService.buscarPor(id)).thenReturn(Optional.of(a));

        AtendimentoResponseDTO dto = new AtendimentoResponseDTO();
        dto.setLookupId(id);
        dto.setLocalidade("Esperança-PB");
        when(atendimentoMapper.from(a)).thenReturn(dto);

        mockMvc.perform(get("/atendimentos/buscar/{atendimentoId}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lookupId").value(id.toString()))
                .andExpect(jsonPath("$.localidade").value("Esperança-PB"));
    }

    @Test
    void deveAtualizarAtendimento() throws Exception {
        UUID atendimentoId = UUID.randomUUID();
        Atendimento atendimentoExistente = mock(Atendimento.class);
        Atendimento atendimentoAtualizado = mock(Atendimento.class);

        AtendimentoSalvarRequestDTO requestDTO = new AtendimentoSalvarRequestDTO();
        requestDTO.setData(LocalDateTime.of(2026, 6, 20, 14, 0));
        requestDTO.setLocalidade("Campina Grande-PB");
        requestDTO.setStatus(StatusAtendimento.CONCLUIDO);

        AtendimentoResponseDTO responseDTO = new AtendimentoResponseDTO();
        responseDTO.setLookupId(atendimentoId);
        responseDTO.setData(requestDTO.getData());
        responseDTO.setLocalidade(requestDTO.getLocalidade());
        responseDTO.setStatus(requestDTO.getStatus());

        when(atendimentoService.buscarPor(atendimentoId)).thenReturn(Optional.of(atendimentoExistente));
        when(atendimentoService.atualizar(any(Atendimento.class))).thenReturn(atendimentoAtualizado);
        when(atendimentoMapper.from(atendimentoAtualizado)).thenReturn(responseDTO);

        mockMvc.perform(patch("/atendimentos/atualizar/{atendimentoId}", atendimentoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lookupId").value(atendimentoId.toString()))
                .andExpect(jsonPath("$.localidade").value("Campina Grande-PB"))
                .andExpect(jsonPath("$.status").value("CONCLUIDO"));

        verify(atendimentoExistente).setLocalidade(requestDTO.getLocalidade());
        verify(atendimentoExistente).setStatus(requestDTO.getStatus());
    }

    @Test
    void deveLancarExcecaoAoAtualizarAtendimentoInexistente() throws Exception {
        UUID idInexistente = UUID.randomUUID();

        AtendimentoSalvarRequestDTO requestDTO = new AtendimentoSalvarRequestDTO();
        requestDTO.setData(LocalDateTime.of(2026, 6, 20, 14, 0));
        requestDTO.setLocalidade("Qualquer Lugar");
        requestDTO.setStatus(StatusAtendimento.CONCLUIDO);

        when(atendimentoService.buscarPor(idInexistente))
                .thenReturn(Optional.empty());

        mockMvc.perform(patch("/atendimentos/atualizar/{atendimentoId}", idInexistente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound());

        verify(atendimentoService, never()).atualizar(any(Atendimento.class));
    }

    @Test
    void deveLancarExcecaoAoAdicionarPagamentoEmAtendimentoInexistente() throws Exception {
        UUID id = UUID.randomUUID();
        PagamentoSalvarRequestDTO dto = new PagamentoSalvarRequestDTO();
        dto.setValor(100);
        dto.setData(LocalDateTime.now());
        dto.setMetodo(Metodo.PIX);
        dto.setStatus("PAGO");

        when(atendimentoService.buscarPor(id)).thenReturn(Optional.empty());

        mockMvc.perform(post("/atendimentos/cadastrar-pagamento/{atendimentoId}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveSetarDataAtualQuandoOModeloMapeadoVierSemData() throws Exception {
        UUID atendimentoId = UUID.randomUUID();
        Atendimento atendimento = mock(Atendimento.class);
        when(atendimentoService.buscarPor(atendimentoId)).thenReturn(Optional.of(atendimento));

        PagamentoSalvarRequestDTO requestDTO = new PagamentoSalvarRequestDTO();
        requestDTO.setValor(200);
        requestDTO.setData(LocalDateTime.now());
        requestDTO.setMetodo(Metodo.PIX);
        requestDTO.setStatus("PAGO");

        Pagamento pagamentoVindoDoMapper = new Pagamento();
        pagamentoVindoDoMapper.setData(null);

        when(pagamentoMapper.from(any(PagamentoSalvarRequestDTO.class)))
                .thenReturn(pagamentoVindoDoMapper);

        when(pagamentoService.criar(any(Pagamento.class)))
                .thenReturn(pagamentoVindoDoMapper);

        when(pagamentoMapper.from(any(Pagamento.class)))
                .thenReturn(new PagamentoResponseDTO());

        mockMvc.perform(post("/atendimentos/cadastrar-pagamento/{atendimentoId}", atendimentoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated());

        org.junit.jupiter.api.Assertions.assertNotNull(pagamentoVindoDoMapper.getData());
    }

    @Test
    void deveSetarDataAtualQuandoModeloMapeadoVierSemData() throws Exception {
        UUID atendimentoId = UUID.randomUUID();
        Atendimento atendimentoMock = mock(Atendimento.class);
        Paciente pacienteMock = new Paciente();

        when(atendimentoService.buscarPor(atendimentoId)).thenReturn(Optional.of(atendimentoMock));
        when(atendimentoMock.getPaciente()).thenReturn(pacienteMock);

        PagamentoSalvarRequestDTO requestDTO = new PagamentoSalvarRequestDTO();
        requestDTO.setValor(150);
        requestDTO.setData(LocalDateTime.now());
        requestDTO.setMetodo(Metodo.PIX);
        requestDTO.setStatus("PAGO");

        Pagamento pagamentoSemData = new Pagamento();
        pagamentoSemData.setData(null);

        when(pagamentoMapper.from(any(PagamentoSalvarRequestDTO.class))).thenReturn(pagamentoSemData);

        when(pagamentoService.criar(any(Pagamento.class))).thenReturn(pagamentoSemData);
        when(pagamentoMapper.from(any(Pagamento.class))).thenReturn(new PagamentoResponseDTO());

        mockMvc.perform(post("/atendimentos/cadastrar-pagamento/{atendimentoId}", atendimentoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated());

        org.junit.jupiter.api.Assertions.assertNotNull(pagamentoSemData.getData(),
                "A data deveria ter sido preenchida pelo Controller dentro do bloco IF");
    }

    @Test
    void naoDeveAlterarDataQuandoJaEstiverPreenchida() throws Exception {
        UUID atendimentoId = UUID.randomUUID();
        Atendimento atendimento = mock(Atendimento.class);
        Paciente paciente = new Paciente();

        when(atendimentoService.buscarPor(atendimentoId)).thenReturn(Optional.of(atendimento));
        when(atendimento.getPaciente()).thenReturn(paciente);

        LocalDateTime dataEsperada = LocalDateTime.of(2025, 1, 1, 10, 0);

        Pagamento pagamentoComData = new Pagamento();
        pagamentoComData.setData(dataEsperada); // data já preenchida

        when(pagamentoMapper.from(any(PagamentoSalvarRequestDTO.class))).thenReturn(pagamentoComData);
        when(pagamentoService.criar(any())).thenReturn(pagamentoComData);
        when(pagamentoMapper.from(any(Pagamento.class))).thenReturn(new PagamentoResponseDTO());

        PagamentoSalvarRequestDTO dto = new PagamentoSalvarRequestDTO();
        dto.setValor(100);
        dto.setData(LocalDateTime.now());
        dto.setMetodo(Metodo.PIX);
        dto.setStatus("PAGO");

        mockMvc.perform(post("/atendimentos/cadastrar-pagamento/{atendimentoId}", atendimentoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        assertEquals(dataEsperada, pagamentoComData.getData());
    }
}