package br.edu.ifpb.es.cuidarme.rest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.edu.ifpb.es.cuidarme.exception.SistemaException;
import br.edu.ifpb.es.cuidarme.mapper.ProntuarioMapper;
import br.edu.ifpb.es.cuidarme.model.Prontuario;
import br.edu.ifpb.es.cuidarme.rest.dto.Prontuario.ProntuarioResponseDTO;
import br.edu.ifpb.es.cuidarme.rest.dto.Prontuario.ProntuarioBuscarDTO;
import br.edu.ifpb.es.cuidarme.rest.dto.Prontuario.ProntuarioSalvarRequestDTO;
import br.edu.ifpb.es.cuidarme.service.ProntuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/prontuarios")
public class ProntuarioRestController implements ProntuarioRestControllerApi {

    @Autowired
    private ProntuarioMapper prontuarioMapper;

    @Autowired
    private ProntuarioService prontuarioService;

    @Override
    @GetMapping("/listar")
    public ResponseEntity<List<ProntuarioResponseDTO>> listar() {
        List<Prontuario> objs = prontuarioService.recuperarTodos();
        List<ProntuarioResponseDTO> resultado = objs.stream()
                .map(prontuarioMapper::from)
                .toList();

        return new ResponseEntity<>(resultado, HttpStatus.OK);
    }

    @Override
    @GetMapping("/buscar/{prontuarioId}")
    public ResponseEntity<ProntuarioResponseDTO> buscarPorId(@PathVariable UUID prontuarioId) throws SistemaException {
        Prontuario obj = validarExiste(prontuarioId);
        ProntuarioResponseDTO resultado = prontuarioMapper.from(obj);

        return new ResponseEntity<>(resultado, HttpStatus.OK);
    }

    @Override
    @PatchMapping("/atualizar/{prontuarioId}")
    public ResponseEntity<ProntuarioResponseDTO> atualizar(@PathVariable UUID prontuarioId, @RequestBody @Valid ProntuarioSalvarRequestDTO dto) throws SistemaException {
        Prontuario objExistente = validarExiste(prontuarioId);
        objExistente.setDataRegistro(dto.getDataRegistro());
        objExistente.setDescricao(dto.getDescricao());
        Prontuario objAtualizado = prontuarioService.atualizar(objExistente);
        ProntuarioResponseDTO resultado = prontuarioMapper.from(objAtualizado);

        return new ResponseEntity<>(resultado, HttpStatus.OK);
    }

    @Override
    @DeleteMapping("/remover/{prontuarioId}")
    public ResponseEntity<Void> remover(@PathVariable UUID prontuarioId) throws SistemaException {
        Prontuario obj = validarExiste(prontuarioId);
        prontuarioService.remover(obj);

        return ResponseEntity.noContent().build();
    }

    private Prontuario validarExiste(UUID lookupId) throws SistemaException {
        Optional<Prontuario> opt = prontuarioService.buscarPor(lookupId);
        if (opt.isEmpty()) {
            throw new SistemaException("Prontuário com o ID " + lookupId + " não foi encontrado.");
        }
        return opt.get();
    }

    @Override
    @GetMapping("/buscar-paginado")
    public ResponseEntity<Page<ProntuarioResponseDTO>> buscarPaginado(
            @RequestParam(defaultValue = "0", required = false) int numeroPagina,
            @RequestParam(defaultValue = "10", required = false) int tamanhoPagina) {

        ProntuarioBuscarDTO buscarDTO = new ProntuarioBuscarDTO();
        buscarDTO.setNumeroPagina(numeroPagina);
        buscarDTO.setTamanhoPagina(tamanhoPagina);

        Page<Prontuario> pagina = prontuarioService.buscar(buscarDTO);

        Page<ProntuarioResponseDTO> resultado = pagina.map(prontuarioMapper::from);

        return new ResponseEntity<>(resultado, HttpStatus.OK);
    }
}