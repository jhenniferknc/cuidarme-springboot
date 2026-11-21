package br.edu.ifpb.es.cuidarme.repository;

import java.util.Optional;
import java.util.UUID;

import br.edu.ifpb.es.cuidarme.model.Paciente;
import br.edu.ifpb.es.cuidarme.rest.dto.Paciente.PacienteBuscarDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, UUID> {

    @Query("SELECT p FROM Paciente p WHERE p.lookupId = :lookupId")
    Optional<Paciente> findByLookupId(@Param("lookupId") UUID lookupId);

    boolean existsByCpf(String cpf);

    boolean existsByCpfAndLookupIdNot(String cpf, UUID lookupId);

    @Query("""
        SELECT p FROM Paciente p
        WHERE (:#{#dto.nome} IS NULL OR LOWER(p.nome) LIKE LOWER(CONCAT('%', :#{#dto.nome}, '%')))
          AND (:#{#dto.cpf} IS NULL OR p.cpf = :#{#dto.cpf})
          AND (:#{#dto.dataNascimento} IS NULL OR p.dataNascimento = :#{#dto.dataNascimento})
    """)
    Page<Paciente> buscarPor(@Param("dto") PacienteBuscarDTO dto, Pageable pageable);

}

