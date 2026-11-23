package br.edu.ifpb.es.cuidarme.service;

import br.edu.ifpb.es.cuidarme.exception.EstadoInvalidoException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.edu.ifpb.es.cuidarme.exception.SistemaException;
import br.edu.ifpb.es.cuidarme.model.Atendimento;
import br.edu.ifpb.es.cuidarme.model.StatusAtendimento;
import br.edu.ifpb.es.cuidarme.repository.AtendimentoRepository;
import br.edu.ifpb.es.cuidarme.rest.dto.Atendimento.AtendimentoBuscarDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AtendimentoService {

    @Autowired
    private AtendimentoRepository repository;

    @Transactional
    public Atendimento criar(Atendimento obj) {
        return repository.save(obj);
    }

    public List<Atendimento> recuperarTodos() {
        return repository.findAll();
    }

    public Optional<Atendimento> buscarPor(UUID lookupId) {
        Atendimento objExemplo = Atendimento.builder()
                .lookupId(lookupId)
                .build();
        Example<Atendimento> exemplo = Example.of(objExemplo);

        return repository.findOne(exemplo);
    }

    @Transactional
    public Atendimento atualizar(Atendimento obj) {
        return repository.save(obj);
    }

    public Page<Atendimento> buscar(AtendimentoBuscarDTO dto) {
        PageRequest pageRequest = PageRequest.of(dto.getNumeroPagina(), dto.getTamanhoPagina());
        return repository.buscarPor(dto, pageRequest);
    }

    @Transactional
    public Atendimento remarcar(Atendimento obj, LocalDateTime novaData, String novaLocalidade) throws EstadoInvalidoException {
        if (obj.getStatus() == StatusAtendimento.CANCELADO || obj.getStatus() == StatusAtendimento.CONCLUIDO) {
            throw new EstadoInvalidoException("Não é possível remarcar um atendimento " + obj.getStatus().name().toLowerCase() + ".");
        }
        obj.setData(novaData);
        obj.setLocalidade(novaLocalidade);
        obj.setStatus(StatusAtendimento.REMARCADO);
        return repository.save(obj);
    }

    @Transactional
    public Atendimento cancelar(Atendimento obj) throws EstadoInvalidoException {
        if (obj.getStatus() == StatusAtendimento.CANCELADO) {
            return obj;
        }
        if (obj.getStatus() == StatusAtendimento.CONCLUIDO) {
            throw new EstadoInvalidoException("Não é possível cancelar um atendimento concluído.");
        }
        obj.setStatus(StatusAtendimento.CANCELADO);
        return repository.save(obj);
    }

}
