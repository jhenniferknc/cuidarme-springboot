package br.edu.ifpb.es.cuidarme.service;

import java.util.Optional;
import java.util.UUID;

import br.edu.ifpb.es.cuidarme.exception.NaoAutorizadoException;
import br.edu.ifpb.es.cuidarme.model.Psicologo;
import br.edu.ifpb.es.cuidarme.repository.PsicologoRepository;
import br.edu.ifpb.es.cuidarme.rest.dto.Psicologo.PsicologoBuscarDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PsicologoService {

    private static final String MENSAGEM_LOGIN_INVALIDO = "Email ou senha inválidos.";

    @Autowired
    private PsicologoRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /** Cria o psicólogo. O objeto chega com a senha em texto puro, que é guardada como hash. */
    @Transactional
    public Psicologo criar(Psicologo obj) {
        definirSenha(obj, obj.getSenha());
        return repository.save(obj);
    }

    public void definirSenha(Psicologo obj, String senhaPura) {
        obj.setSenha(passwordEncoder.encode(senhaPura));
    }

    @Transactional
    public Psicologo validarLogin(String email, String senha) throws NaoAutorizadoException {
        Psicologo psicologo = repository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NaoAutorizadoException(MENSAGEM_LOGIN_INVALIDO));

        String senhaGuardada = psicologo.getSenha();
        if (ehHashBcrypt(senhaGuardada)) {
            if (!passwordEncoder.matches(senha, senhaGuardada)) {
                throw new NaoAutorizadoException(MENSAGEM_LOGIN_INVALIDO);
            }
            return psicologo;
        }

        // contas criadas antes do bcrypt têm a senha em texto puro. se a senha conferir, ela é convertida para hash no mesmo login
        if (!senhaGuardada.equals(senha)) {
            throw new NaoAutorizadoException(MENSAGEM_LOGIN_INVALIDO);
        }
        definirSenha(psicologo, senha);
        return repository.save(psicologo);
    }

    private boolean ehHashBcrypt(String valor) {
        return valor != null && valor.matches("^\\$2[aby]\\$\\d{2}\\$.{53}$");
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
