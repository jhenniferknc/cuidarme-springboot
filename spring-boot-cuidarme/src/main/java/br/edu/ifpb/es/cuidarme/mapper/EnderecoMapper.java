package br.edu.ifpb.es.cuidarme.mapper;

import br.edu.ifpb.es.cuidarme.model.Endereco;
import br.edu.ifpb.es.cuidarme.rest.dto.Endereco.EnderecoDTO;
import org.springframework.stereotype.Component;

@Component
public class EnderecoMapper {
    public Endereco from (EnderecoDTO from){
        if (from == null || estaVazio(from)) {
            return null;
        }
        Endereco endereco = new Endereco();
        endereco.setCep(from.getCep());
        endereco.setCidade(from.getCidade());
        endereco.setEstado(from.getEstado());
        endereco.setLogradouro(from.getLogradouro());
        endereco.setNumero(from.getNumero());
        return endereco;
    }

    public EnderecoDTO from(Endereco from){
        if (from == null) {
            return null;
        }
        EnderecoDTO enderecoDTO = new EnderecoDTO();
        enderecoDTO.setCep(from.getCep());
        enderecoDTO.setCidade(from.getCidade());
        enderecoDTO.setEstado(from.getEstado());
        enderecoDTO.setLogradouro(from.getLogradouro());
        enderecoDTO.setNumero(from.getNumero());
        return enderecoDTO;
    }

    private boolean estaVazio(EnderecoDTO dto) {
        return vazio(dto.getCep()) && vazio(dto.getLogradouro()) && vazio(dto.getNumero())
                && vazio(dto.getCidade()) && vazio(dto.getEstado());
    }

    private boolean vazio(String valor) {
        return valor == null || valor.isBlank();
    }
}
