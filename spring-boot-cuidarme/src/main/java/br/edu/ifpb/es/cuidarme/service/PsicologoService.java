package br.edu.ifpb.es.cuidarme.service;

import java.util.Optional;
import java.util.UUID;

import br.edu.ifpb.es.cuidarme.model.Psicologo;
import br.edu.ifpb.es.cuidarme.repository.PsicologoRepository;
import br.edu.ifpb.es.cuidarme.rest.dto.Psicologo.PsicologoBuscarDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PsicologoService {

    @Autowired
    private PsicologoRepository repository;

    @Transactional
    public Psicologo criar(Psicologo obj) {
        return repository.save(obj);
    }

    public Psicologo validarLogin(String email, String senha) {
        return repository.findByEmailAndSenha(email, senha)
                .orElseThrow(() -> new RuntimeException("Email ou senha inválidos."));
    }

    public Optional<Psicologo> buscarPor(UUID lookupId) {
        Psicologo objExemplo = Psicologo.builder()
                .lookupId(lookupId)
                .build();
        Example<Psicologo> exemplo = Example.of(objExemplo);

        return repository.findOne(exemplo);
    }

    @Transactional
    public Psicologo atualizar(Psicologo obj) {
        return repository.save(obj);
    }

    @Transactional
    public void remover(Psicologo obj) {
        repository.delete(obj);
    }

    public Page<Psicologo> buscar(PsicologoBuscarDTO dto) {
        PageRequest pageRequest = PageRequest.of(dto.getNumeroPagina(), dto.getTamanhoPagina());
        return repository.buscarPor(dto, pageRequest);
    }

}