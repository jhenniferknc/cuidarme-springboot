package br.edu.ifpb.es.cuidarme.pagamento;

import br.edu.ifpb.es.cuidarme.mapper.PagamentoMapper;
import br.edu.ifpb.es.cuidarme.model.Metodo;
import br.edu.ifpb.es.cuidarme.model.Pagamento;
import br.edu.ifpb.es.cuidarme.model.StatusPagamento;
import br.edu.ifpb.es.cuidarme.rest.PagamentoRestController;
import br.edu.ifpb.es.cuidarme.rest.dto.Pagamento.PagamentoResponseDTO;
import br.edu.ifpb.es.cuidarme.rest.dto.Pagamento.PagamentoSalvarRequestDTO;
import br.edu.ifpb.es.cuidarme.service.PacienteService;
import br.edu.ifpb.es.cuidarme.service.PagamentoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({ PagamentoRestController.class })
@AutoConfigureMockMvc
public class PagamentoRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PagamentoService pagamentoService;

    @MockitoBean
    private PagamentoMapper pagamentoMapper;

    @MockitoBean
    private PacienteService pacienteService;

    @Test
    void deveRetornarTodosOsPagamentosAoListar() throws Exception {

        Pagamento pagamento1 = mock(Pagamento.class);
        Pagamento pagamento2 = mock(Pagamento.class);

        when(pagamentoService.recuperarTodos())
                .thenReturn(List.of(pagamento1, pagamento2));

        PagamentoResponseDTO dto1 = new PagamentoResponseDTO();
        dto1.setLookupId(UUID.randomUUID());
        dto1.setMetodo(Metodo.PIX);
        dto1.setStatus(StatusPagamento.PAGO);

        PagamentoResponseDTO dto2 = new PagamentoResponseDTO();
        dto2.setLookupId(UUID.randomUUID());
        dto2.setMetodo(Metodo.CARTAO);
        dto2.setStatus(StatusPagamento.PENDENTE);

        when(pagamentoMapper.from(pagamento1)).thenReturn(dto1);
        when(pagamentoMapper.from(pagamento2)).thenReturn(dto2);

        mockMvc.perform(get("/pagamentos/listar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].metodo").value("PIX"))
                .andExpect(jsonPath("$[1].metodo").value("CARTAO"));
    }

    @Test
    void deveRetornarListaVaziaAoListarPagamentos() throws Exception {

        when(pagamentoService.recuperarTodos()).thenReturn(List.of());

        mockMvc.perform(get("/pagamentos/listar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void deveBuscarPagamentoPorIdComSucesso() throws Exception {

        UUID id = UUID.randomUUID();

        Pagamento pagamento = mock(Pagamento.class);

        when(pagamentoService.buscarPor(id))
                .thenReturn(Optional.of(pagamento));

        PagamentoResponseDTO dto = new PagamentoResponseDTO();
        dto.setLookupId(id);
        dto.setMetodo(Metodo.PIX);
        dto.setStatus(StatusPagamento.PAGO);

        when(pagamentoMapper.from(pagamento)).thenReturn(dto);

        mockMvc.perform(get("/pagamentos/buscar/{pagamentoId}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lookupId").value(id.toString()))
                .andExpect(jsonPath("$.metodo").value("PIX"))
                .andExpect(jsonPath("$.status").value("PAGO"));
    }

    @Test
    void deveLancarExcecaoAoBuscarPagamentoInexistente() throws Exception {

        UUID id = UUID.randomUUID();

        when(pagamentoService.buscarPor(id))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/pagamentos/buscar/{pagamentoId}", id))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveAtualizarPagamentoComSucesso() throws Exception {

        UUID id = UUID.randomUUID();

        Pagamento pagamentoExistente = mock(Pagamento.class);
        Pagamento pagamentoAtualizado = mock(Pagamento.class);

        PagamentoSalvarRequestDTO requestDTO = new PagamentoSalvarRequestDTO();
        requestDTO.setValor(300);
        requestDTO.setData(LocalDateTime.now());
        requestDTO.setMetodo(Metodo.CARTAO);
        requestDTO.setStatus("PAGO");

        when(pagamentoService.buscarPor(id))
                .thenReturn(Optional.of(pagamentoExistente));

        when(pagamentoService.atualizar(pagamentoExistente))
                .thenReturn(pagamentoAtualizado);

        PagamentoResponseDTO responseDTO = new PagamentoResponseDTO();
        responseDTO.setLookupId(id);
        responseDTO.setValor(300);
        responseDTO.setMetodo(Metodo.CARTAO);
        responseDTO.setStatus(StatusPagamento.PAGO);

        when(pagamentoMapper.from(pagamentoAtualizado))
                .thenReturn(responseDTO);

        mockMvc.perform(
                        patch("/pagamentos/atualizar/{pagamentoId}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDTO))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metodo").value("CARTAO"));

        verify(pagamentoExistente).setValor(requestDTO.getValor());
        verify(pagamentoExistente).setData(requestDTO.getData());
        verify(pagamentoExistente).setMetodo(requestDTO.getMetodo());
    }

    @Test
    void deveLancarExcecaoAoAtualizarPagamentoInexistente() throws Exception {

        UUID id = UUID.randomUUID();

        PagamentoSalvarRequestDTO requestDTO = new PagamentoSalvarRequestDTO();
        requestDTO.setValor(100);
        requestDTO.setData(LocalDateTime.now());
        requestDTO.setMetodo(Metodo.ESPECIE);
        requestDTO.setStatus("PENDENTE");

        when(pagamentoService.buscarPor(id))
                .thenReturn(Optional.empty());

        mockMvc.perform(
                        patch("/pagamentos/atualizar/{pagamentoId}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDTO))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRemoverPagamentoExistenteComSucesso() throws Exception {

        UUID id = UUID.randomUUID();

        Pagamento pagamento = mock(Pagamento.class);

        when(pagamentoService.buscarPor(id))
                .thenReturn(Optional.of(pagamento));

        mockMvc.perform(delete("/pagamentos/remover/{pagamentoId}", id))
                .andExpect(status().isNoContent());

        verify(pagamentoService).remover(pagamento);
    }

    @Test
    void deveLancarExcecaoAoRemoverPagamentoInexistente() throws Exception {

        UUID id = UUID.randomUUID();

        when(pagamentoService.buscarPor(id))
                .thenReturn(Optional.empty());

        mockMvc.perform(delete("/pagamentos/remover/{pagamentoId}", id))
                .andExpect(status().isBadRequest());
    }
}