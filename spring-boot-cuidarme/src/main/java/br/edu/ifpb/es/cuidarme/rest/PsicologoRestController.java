package br.edu.ifpb.es.cuidarme.rest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.edu.ifpb.es.cuidarme.exception.SistemaException;
import br.edu.ifpb.es.cuidarme.mapper.AtendimentoMapper;
import br.edu.ifpb.es.cuidarme.mapper.PacienteMapper;
import br.edu.ifpb.es.cuidarme.mapper.PsicologoMapper;
import br.edu.ifpb.es.cuidarme.model.Atendimento;
import br.edu.ifpb.es.cuidarme.model.Paciente;
import br.edu.ifpb.es.cuidarme.model.Psicologo;
import br.edu.ifpb.es.cuidarme.rest.dto.Atendimento.AtendimentoResponseDTO;
import br.edu.ifpb.es.cuidarme.rest.dto.Paciente.PacienteResponseDTO;
import br.edu.ifpb.es.cuidarme.rest.dto.Paciente.PacienteSalvarRequestDTO;
import br.edu.ifpb.es.cuidarme.rest.dto.Psicologo.PsicologoLoginRequestDTO;
import br.edu.ifpb.es.cuidarme.rest.dto.Psicologo.PsicologoResponseDTO;
import br.edu.ifpb.es.cuidarme.rest.dto.Psicologo.PsicologoSalvarRequestDTO;
import br.edu.ifpb.es.cuidarme.service.PacienteService;
import br.edu.ifpb.es.cuidarme.service.PsicologoService;
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
@RequestMapping("/psicologos")
public class PsicologoRestController implements PsicologoRestControllerApi {

    @Autowired
    private PsicologoMapper psicologoMapper;

    @Autowired
    private PsicologoService psicologoService;

    @Autowired
    private PacienteService pacienteService;

    @Autowired
    private PacienteMapper pacienteMapper;

    @Autowired
    private AtendimentoMapper atendimentoMapper;

    @Override
    @PostMapping("/cadastrar")
    public ResponseEntity<PsicologoResponseDTO> adicionar(@RequestBody @Valid PsicologoSalvarRequestDTO dto) throws SistemaException {
        Psicologo objNovo = psicologoMapper.from(dto);
        Psicologo objCriado = psicologoService.criar(objNovo);
        PsicologoResponseDTO resultado = psicologoMapper.from(objCriado);

        return new ResponseEntity<>(resultado, HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<PsicologoResponseDTO> login(@RequestBody PsicologoLoginRequestDTO loginRequestDTO) {
        Psicologo psicologo = psicologoService.validarLogin(loginRequestDTO.getEmail(), loginRequestDTO.getSenha());
        PsicologoResponseDTO resultado = psicologoMapper.from(psicologo);
        return new ResponseEntity<>(resultado, HttpStatus.OK);
    }

    @Override
    @PostMapping("/cadastrar-pacientes/{psicologoId}")
    public ResponseEntity<PacienteResponseDTO> adicionarPaciente(@PathVariable UUID psicologoId, @RequestBody @Valid PacienteSalvarRequestDTO dto) throws SistemaException {
        Psicologo psicologo = validarExiste(psicologoId);
        Paciente novoPaciente = pacienteMapper.from(dto);
        novoPaciente.setPsicologo(psicologo);
        Paciente pacienteCriado = pacienteService.criar(novoPaciente);
        PacienteResponseDTO resultado = pacienteMapper.from(pacienteCriado);

        return new ResponseEntity<>(resultado, HttpStatus.CREATED);
    }

    @Override
    @GetMapping("/pacientes/{psicologoId}")
    public ResponseEntity<List<PacienteResponseDTO>> listarPacientesPorPsicologo(@PathVariable UUID psicologoId) throws SistemaException {
        Psicologo psicologo = validarExiste(psicologoId);
        List<Paciente> pacientes = psicologo.getPacientes();
        List<PacienteResponseDTO> resultado = pacientes.stream()
                .map(pacienteMapper::from)
                .toList();

        return new ResponseEntity<>(resultado, HttpStatus.OK);
    }

    @Override
    @GetMapping("/atendimentos/{psicologoId}")
    public ResponseEntity<List<AtendimentoResponseDTO>> listarAtendimentosPorPsicologo(@PathVariable UUID psicologoId) throws SistemaException {
        Psicologo psicologo = validarExiste(psicologoId);
        List<Atendimento> atendimentos = psicologo.getAtendimentos();
        List<AtendimentoResponseDTO> resultado = atendimentos.stream()
                .map(atendimentoMapper::from)
                .toList();

        return new ResponseEntity<>(resultado, HttpStatus.OK);
    }

    @Override
    @PatchMapping("/atualizar/{psicologoId}")
    public ResponseEntity<PsicologoResponseDTO> atualizar(@PathVariable UUID psicologoId, @RequestBody @Valid PsicologoSalvarRequestDTO dto) throws SistemaException {
        Psicologo objExistente = validarExiste(psicologoId);
        objExistente.setNome(dto.getNome());
        objExistente.setEmail(dto.getEmail());
        objExistente.setSenha(dto.getSenha());
        Psicologo objAtualizado = psicologoService.atualizar(objExistente);
        PsicologoResponseDTO resultado = psicologoMapper.from(objAtualizado);

        return new ResponseEntity<>(resultado, HttpStatus.OK);
    }

    @Override
    @DeleteMapping("/remover/{psicologoId}")
    public ResponseEntity<Void> remover(@PathVariable UUID psicologoId) throws SistemaException {
        Psicologo obj = validarExiste(psicologoId);
        psicologoService.remover(obj);

        return ResponseEntity.noContent().build();
    }

    private Psicologo validarExiste(UUID lookupId) throws SistemaException {
        Optional<Psicologo> opt = psicologoService.buscarPor(lookupId);
        if (opt.isEmpty()) {
            throw new SistemaException("Psicólogo com o ID " + lookupId + " não foi encontrado.");
        }
        return opt.get();
    }
}
