package br.edu.ifpb.es.cuidarme.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.edu.ifpb.es.cuidarme.exception.ConflitoException;
import br.edu.ifpb.es.cuidarme.model.Paciente;
import br.edu.ifpb.es.cuidarme.repository.PacienteRepository;
import br.edu.ifpb.es.cuidarme.rest.dto.Paciente.PacienteBuscarDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PacienteService {

    static final String MENSAGEM_CPF_DUPLICADO = "Já existe um paciente cadastrado com este CPF.";

    @Autowired
    private PacienteRepository repository;

    @Transactional
    public Paciente criar(Paciente obj) throws ConflitoException {
        boolean cpfEmUso = obj.getLookupId() == null
                ? repository.existsByCpf(obj.getCpf())
                : repository.existsByCpfAndLookupIdNot(obj.getCpf(), obj.getLookupId());
        if (cpfEmUso) {
            throw new ConflitoException(MENSAGEM_CPF_DUPLICADO);
        }
        return repository.save(obj);
    }

    public List<Paciente> recuperarTodos() {
        return repository.findAll();
    }

    public Optional<Paciente> buscarPor(UUID lookupId) {
        return repository.findById(lookupId);
    }

    @Transactional
    public Paciente atualizar(Paciente obj) throws ConflitoException {
        if (repository.existsByCpfAndLookupIdNot(obj.getCpf(), obj.getLookupId())) {
            throw new ConflitoException(MENSAGEM_CPF_DUPLICADO);
        }
        return repository.save(obj);
    }

    @Transactional
    public void remover(Paciente obj) {
        repository.delete(obj);
    }

    public Page<Paciente> buscar(PacienteBuscarDTO dto) {
        PageRequest pageRequest = PageRequest.of(dto.getNumeroPagina(), dto.getTamanhoPagina());
        return repository.buscarPor(dto, pageRequest);
    }

}
