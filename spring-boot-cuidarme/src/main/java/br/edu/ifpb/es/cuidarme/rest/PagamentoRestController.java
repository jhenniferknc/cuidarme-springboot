package br.edu.ifpb.es.cuidarme.rest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.edu.ifpb.es.cuidarme.exception.NaoEncontradoException;
import br.edu.ifpb.es.cuidarme.exception.SistemaException;
import br.edu.ifpb.es.cuidarme.mapper.PagamentoMapper;
import br.edu.ifpb.es.cuidarme.model.Pagamento;
import br.edu.ifpb.es.cuidarme.model.StatusPagamento;
import br.edu.ifpb.es.cuidarme.rest.dto.Pagamento.PagamentoResponseDTO;
import br.edu.ifpb.es.cuidarme.rest.dto.Pagamento.PagamentoSalvarRequestDTO;
import br.edu.ifpb.es.cuidarme.service.PagamentoService;
import br.edu.ifpb.es.cuidarme.service.PacienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/pagamentos")
public class PagamentoRestController implements PagamentoRestControllerApi {

    @Autowired
    private PagamentoMapper pagamentoMapper;

    @Autowired
    private PagamentoService pagamentoService;

    @Autowired
    private PacienteService pacienteService;

    @Override
    @GetMapping("/listar")
    public ResponseEntity<List<PagamentoResponseDTO>> listar() throws SistemaException {
        List<Pagamento> objs = pagamentoService.recuperarTodos();
        List<PagamentoResponseDTO> resultado = objs.stream()
                .map(pagamentoMapper::from)
                .toList();

        return new ResponseEntity<>(resultado, HttpStatus.OK);
    }

    @Override
    @GetMapping("/buscar/{pagamentoId}")
    public ResponseEntity<PagamentoResponseDTO> buscarPor(@PathVariable UUID pagamentoId) throws SistemaException {
        Pagamento obj = validarExiste(pagamentoId);
        PagamentoResponseDTO resultado = pagamentoMapper.from(obj);

        return new ResponseEntity<>(resultado, HttpStatus.OK);
    }

    @Override
    @PatchMapping("/atualizar/{pagamentoId}")
    public ResponseEntity<PagamentoResponseDTO> atualizar(@PathVariable UUID pagamentoId, @RequestBody @Valid PagamentoSalvarRequestDTO dto) throws SistemaException {
        Pagamento objExistente = validarExiste(pagamentoId);
        objExistente.setData(dto.getData());
        objExistente.setValor(dto.getValor());
        objExistente.setMetodo(dto.getMetodo());
        objExistente.setStatusPagamento(StatusPagamento.valueOf(dto.getStatus()));
        Pagamento objAtualizado = pagamentoService.atualizar(objExistente);
        PagamentoResponseDTO resultado = pagamentoMapper.from(objAtualizado);

        return new ResponseEntity<>(resultado, HttpStatus.OK);
    }

    @Override
    @DeleteMapping("/remover/{pagamentoId}")
    public ResponseEntity<Void> remover(@PathVariable UUID pagamentoId) throws SistemaException {
        Pagamento obj = validarExiste(pagamentoId);
        pagamentoService.remover(obj);

        return ResponseEntity.noContent().build();
    }

    private Pagamento validarExiste(UUID lookupId) throws NaoEncontradoException {
        Optional<Pagamento> opt = pagamentoService.buscarPor(lookupId);
        if (!opt.isPresent()) {
            throw new NaoEncontradoException(String.format("Entidade 'Pagamento' de lookupId '%s' não foi encontrada!", lookupId));
        }
        return opt.get();
    }
}