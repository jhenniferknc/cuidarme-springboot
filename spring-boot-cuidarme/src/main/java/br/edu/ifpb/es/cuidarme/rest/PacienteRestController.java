package br.edu.ifpb.es.cuidarme.rest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.edu.ifpb.es.cuidarme.exception.NaoEncontradoException;
import br.edu.ifpb.es.cuidarme.exception.SistemaException;
import br.edu.ifpb.es.cuidarme.mapper.*;
import br.edu.ifpb.es.cuidarme.model.Atendimento;
import br.edu.ifpb.es.cuidarme.model.Paciente;
import br.edu.ifpb.es.cuidarme.model.Prontuario;
import br.edu.ifpb.es.cuidarme.model.Psicologo;
import br.edu.ifpb.es.cuidarme.rest.dto.Atendimento.AtendimentoResponseDTO;
import br.edu.ifpb.es.cuidarme.rest.dto.Atendimento.AtendimentoSalvarRequestDTO;
import br.edu.ifpb.es.cuidarme.rest.dto.Paciente.PacienteResponseDTO;
import br.edu.ifpb.es.cuidarme.rest.dto.Paciente.PacienteSalvarRequestDTO;
import br.edu.ifpb.es.cuidarme.rest.dto.Prontuario.ProntuarioResponseDTO;
import br.edu.ifpb.es.cuidarme.rest.dto.Prontuario.ProntuarioSalvarRequestDTO;
import br.edu.ifpb.es.cuidarme.service.AtendimentoService;
import br.edu.ifpb.es.cuidarme.service.PacienteService;
import br.edu.ifpb.es.cuidarme.service.ProntuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/pacientes")
public class PacienteRestController implements PacienteRestControllerApi {

    @Autowired
    private PacienteMapper pacienteMapper;

    @Autowired
    private PacienteService pacienteService;

    @Autowired
    private EnderecoMapper enderecoMapper;

    @Autowired
    private ContatoEmergenciaMapper contatoEmergenciaMapper;

    @Autowired
    private AtendimentoMapper atendimentoMapper;

    @Autowired
    private AtendimentoService atendimentoService;

    @Autowired
    private ProntuarioMapper prontuarioMapper;

    @Autowired
    private ProntuarioService prontuarioService;

    @Override
    @GetMapping("/listar")
    public ResponseEntity<List<PacienteResponseDTO>> listar() {
        List<Paciente> objs = pacienteService.recuperarTodos();
        List<PacienteResponseDTO> resultado = objs.stream()
                .map(pacienteMapper::from)
                .toList();

        return new ResponseEntity<>(resultado, HttpStatus.OK);
    }

    @Override
    @PatchMapping("/atualizar/{pacienteId}")
    public ResponseEntity<PacienteResponseDTO> atualizar(@PathVariable UUID pacienteId, @RequestBody @Valid PacienteSalvarRequestDTO dto) throws SistemaException {
        Paciente objExistente = validarExiste(pacienteId);
        objExistente.setNome(dto.getNome());
        objExistente.setCpf(dto.getCpf());
        objExistente.setRg(dto.getRg());
        objExistente.setDataNascimento(dto.getDataNascimento());
        objExistente.setSexo(dto.getSexo());
        objExistente.setEstadoCivil(dto.getEstadoCivil());
        objExistente.setGrauInstrucao(dto.getGrauInstrucao());
        objExistente.setProfissao(dto.getProfissao());
        objExistente.setTelefone(dto.getTelefone());
        objExistente.setEnderecoPessoal(enderecoMapper.from(dto.getEnderecoPessoal()));
        objExistente.setEnderecoTrabalho(enderecoMapper.from(dto.getEnderecoTrabalho()));
        objExistente.setInfoAdicionais(dto.getInfoAdicionais());
        objExistente.setContatoEmergencia(contatoEmergenciaMapper.from(dto.getContatoEmergencia()));
        Paciente objAtualizado = pacienteService.atualizar(objExistente);
        PacienteResponseDTO resultado = pacienteMapper.from(objAtualizado);

        return new ResponseEntity<>(resultado, HttpStatus.OK);
    }

    @Override
    @DeleteMapping("/remover/{pacienteId}")
    public ResponseEntity<Void> remover(@PathVariable UUID pacienteId) throws SistemaException {
        Paciente obj = validarExiste(pacienteId);
        pacienteService.remover(obj);

        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/buscar/{pacienteId}")
    public ResponseEntity<PacienteResponseDTO> buscarPorId(@PathVariable UUID pacienteId) throws SistemaException {
        Paciente obj = validarExiste(pacienteId);
        PacienteResponseDTO resultado = pacienteMapper.from(obj);

        return new ResponseEntity<>(resultado, HttpStatus.OK);
    }

    @Override
    @PostMapping("/cadastrar-atendimentos/{pacienteId}")
    public ResponseEntity<AtendimentoResponseDTO> adicionarAtendimento (@PathVariable UUID pacienteId, @RequestBody @Valid AtendimentoSalvarRequestDTO dto) throws SistemaException {
        Paciente paciente = validarExiste(pacienteId);
        Psicologo psicologo = paciente.getPsicologo();
        Atendimento novoAtendimento = atendimentoMapper.from(dto);
        novoAtendimento.setPaciente(paciente);
        novoAtendimento.setPsicologo(psicologo);
        Atendimento atendimentoCriado = atendimentoService.criar(novoAtendimento);
        AtendimentoResponseDTO resultado = atendimentoMapper.from(atendimentoCriado);

        return new ResponseEntity<>(resultado, HttpStatus.OK);
    }

    @Override
    @GetMapping("/atendimentos/{pacienteId}")
    public ResponseEntity<List<AtendimentoResponseDTO>> listarAtendimentosPorPaciente(@PathVariable UUID pacienteId) throws SistemaException {
        Paciente paciente = validarExiste(pacienteId);
        List<Atendimento> atendimentos = paciente.getAtendimentos();
        List<AtendimentoResponseDTO> resultado = atendimentos.stream()
                .map(atendimentoMapper::from)
                .toList();

        return new ResponseEntity<>(resultado, HttpStatus.OK);
    }

    @Override
    @PostMapping("/cadastrar-prontuarios/{pacienteId}")
    public ResponseEntity<ProntuarioResponseDTO> adicionarProntuario(@PathVariable UUID pacienteId, @RequestBody @Valid ProntuarioSalvarRequestDTO dto) throws SistemaException {
        Paciente paciente = validarExiste(pacienteId);
        Prontuario novoProntuario = prontuarioMapper.from(dto);
        novoProntuario.setPaciente(paciente);
        Prontuario prontuarioCriado = prontuarioService.criar(novoProntuario);
        ProntuarioResponseDTO resultado = prontuarioMapper.from(prontuarioCriado);

        return new ResponseEntity<>(resultado, HttpStatus.OK);
    }

    private Paciente validarExiste(UUID lookupId) throws SistemaException {
        Optional<Paciente> opt = pacienteService.buscarPor(lookupId);
        if (opt.isEmpty()) {
            throw new NaoEncontradoException("Paciente com o ID " + lookupId + " não foi encontrado.");
        }
        return opt.get();
    }
}
