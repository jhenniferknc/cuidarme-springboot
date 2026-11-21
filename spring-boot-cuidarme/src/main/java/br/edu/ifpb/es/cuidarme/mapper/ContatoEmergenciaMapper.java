package br.edu.ifpb.es.cuidarme.mapper;

import br.edu.ifpb.es.cuidarme.model.ContatoEmergencia;
import br.edu.ifpb.es.cuidarme.rest.dto.ContatoEmergencia.ContatoEmergenciaDTO;
import org.springframework.stereotype.Component;

@Component
public class ContatoEmergenciaMapper {
    public ContatoEmergencia from (ContatoEmergenciaDTO from){
        if (from == null || (vazio(from.getNome()) && vazio(from.getTelefone()) && vazio(from.getParentesco()))) {
            return null;
        }
        ContatoEmergencia contatoEmergencia = new ContatoEmergencia();
        contatoEmergencia.setNome(from.getNome());
        contatoEmergencia.setParentesco(from.getParentesco());
        contatoEmergencia.setTelefone(from.getTelefone());
        return contatoEmergencia;
    }

    public ContatoEmergenciaDTO from (ContatoEmergencia from) {
        if (from == null) {
            return null;
        }
        ContatoEmergenciaDTO contatoEmergenciaDTO = new ContatoEmergenciaDTO();
        contatoEmergenciaDTO.setNome(from.getNome());
        contatoEmergenciaDTO.setParentesco(from.getParentesco());
        contatoEmergenciaDTO.setTelefone(from.getTelefone());
        return contatoEmergenciaDTO;
    }

    private boolean vazio(String valor) {
        return valor == null || valor.isBlank();
    }
}
