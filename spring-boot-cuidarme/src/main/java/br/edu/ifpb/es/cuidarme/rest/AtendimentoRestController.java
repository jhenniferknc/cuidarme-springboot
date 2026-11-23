package br.edu.ifpb.es.cuidarme.rest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.edu.ifpb.es.cuidarme.exception.NaoEncontradoException;
import br.edu.ifpb.es.cuidarme.exception.SistemaException;
import br.edu.ifpb.es.cuidarme.mapper.AtendimentoMapper;
import br.edu.ifpb.es.cuidarme.mapper.PagamentoMapper;
import br.edu.ifpb.es.cuidarme.model.Atendimento;
import br.edu.ifpb.es.cuidarme.model.Pagamento;
import br.edu.ifpb.es.cuidarme.model.StatusAtendimento;
import br.edu.ifpb.es.cuidarme.model.StatusPagamento;
import br.edu.ifpb.es.cuidarme.rest.dto.Atendimento.AtendimentoRemarcarDTO;
import br.edu.ifpb.es.cuidarme.rest.dto.Atendimento.AtendimentoResponseDTO;
import br.edu.ifpb.es.cuidarme.rest.dto.Atendimento.AtendimentoSalvarRequestDTO;
import br.edu.ifpb.es.cuidarme.rest.dto.Pagamento.PagamentoResponseDTO;
import br.edu.ifpb.es.cuidarme.rest.dto.Pagamento.PagamentoSalvarRequestDTO;
import br.edu.ifpb.es.cuidarme.service.AtendimentoService;
import br.edu.ifpb.es.cuidarme.service.PagamentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/atendimentos")
public class AtendimentoRestController implements AtendimentoRestControllerApi {

    @Autowired
    private AtendimentoMapper atendimentoMapper;

    @Autowired
    private AtendimentoService atendimentoService;

    @Autowired
    private PagamentoService pagamentoService;

    @Autowired
    private PagamentoMapper pagamentoMapper;

    @Override
    @PostMapping("/cadastrar-pagamento/{atendimentoId}")
    public ResponseEntity<PagamentoResponseDTO> adicionarPagamento(@PathVariable UUID atendimentoId, @RequestBody @Valid PagamentoSalvarRequestDTO dto) throws NaoEncontradoException {
        Atendimento atendimento = atendimentoService.buscarPor(atendimentoId)
                .orElseThrow(() -> new NaoEncontradoException("Atendimento com ID " + atendimentoId + " não encontrado."));
        Pagamento novoPagamento = pagamentoMapper.from(dto);
        novoPagamento.setAtendimento(atendimento);
        novoPagamento.setPaciente(atendimento.getPaciente());
        novoPagamento.setStatusPagamento(StatusPagamento.PAGO);

        if (novoPagamento.getData() == null) {
            novoPagamento.setData(LocalDateTime.now());
        }

        Pagamento pagamentoCriado = pagamentoService.criar(novoPagamento);
        atendimento.setStatus(StatusAtendimento.AGENDADO);
        atendimentoService.atualizar(atendimento);
        PagamentoResponseDTO resultado = pagamentoMapper.from(pagamentoCriado);

        return new ResponseEntity<>(resultado, HttpStatus.CREATED);
    }

    @Override
    @GetMapping("/listar")
    public ResponseEntity<List<AtendimentoResponseDTO>> listar() {
        List<Atendimento> objs = atendimentoService.recuperarTodos();
        List<AtendimentoResponseDTO> resultado = objs.stream()
                .map(atendimentoMapper::from)
                .toList();

        return new ResponseEntity<>(resultado, HttpStatus.OK);
    }

    @Override
    @GetMapping("/buscar/{atendimentoId}")
    public ResponseEntity<AtendimentoResponseDTO> buscarPorId(@PathVariable UUID atendimentoId) throws SistemaException {
        Atendimento obj = validarExiste(atendimentoId);
        AtendimentoResponseDTO resultado = atendimentoMapper.from(obj);

        return new ResponseEntity<>(resultado, HttpStatus.OK);
    }

    @Override
    @PatchMapping("/atualizar/{atendimentoId}")
    public ResponseEntity<AtendimentoResponseDTO> atualizar(@PathVariable UUID atendimentoId, @RequestBody @Valid AtendimentoSalvarRequestDTO dto) throws SistemaException {
        Atendimento objExistente = validarExiste(atendimentoId);
        objExistente.setData(dto.getData());
        objExistente.setLocalidade(dto.getLocalidade());
        objExistente.setStatus(dto.getStatus());
        Atendimento objAtualizado = atendimentoService.atualizar(objExistente);
        AtendimentoResponseDTO resultado = atendimentoMapper.from(objAtualizado);

        return new ResponseEntity<>(resultado, HttpStatus.OK);
    }

    @Override
    @PatchMapping("/remarcar/{atendimentoId}")
    public ResponseEntity<AtendimentoResponseDTO> remarcar(@PathVariable UUID atendimentoId, @RequestBody @Valid AtendimentoRemarcarDTO dto) throws SistemaException {
        Atendimento obj = validarExiste(atendimentoId);
        Atendimento remarcado = atendimentoService.remarcar(obj, dto.getData(), dto.getLocalidade());
        return new ResponseEntity<>(atendimentoMapper.from(remarcado), HttpStatus.OK);
    }

    @Override
    @PatchMapping("/cancelar/{atendimentoId}")
    public ResponseEntity<AtendimentoResponseDTO> cancelar(@PathVariable UUID atendimentoId) throws SistemaException {
        Atendimento obj = validarExiste(atendimentoId);
        Atendimento cancelado = atendimentoService.cancelar(obj);
        return new ResponseEntity<>(atendimentoMapper.from(cancelado), HttpStatus.OK);
    }

    private Atendimento validarExiste(UUID lookupId) throws SistemaException {
        Optional<Atendimento> opt = atendimentoService.buscarPor(lookupId);
        if (opt.isEmpty()) {
            throw new NaoEncontradoException("Atendimento com o ID " + lookupId + " não foi encontrado.");
        }
        return opt.get();
    }
}
