package br.edu.ifpb.es.cuidarme.repository;

import java.util.Optional;
import java.util.UUID;

import br.edu.ifpb.es.cuidarme.model.Psicologo;
import br.edu.ifpb.es.cuidarme.rest.dto.Psicologo.PsicologoBuscarDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PsicologoRepository extends JpaRepository<Psicologo, UUID> {

    @Query("SELECT p FROM Psicologo p WHERE LOWER(p.email) = LOWER(:email) AND p.senha = :senha")
    Optional<Psicologo> findByEmailAndSenha(@Param("email") String email, @Param("senha") String senha);

    @Query("SELECT p FROM Psicologo p WHERE p.lookupId = :lookupId")
    Optional<Psicologo> findByLookupId(@Param("lookupId") UUID lookupId);

    @Query("""
        SELECT p FROM Psicologo p
        WHERE (:#{#dto.nome} IS NULL OR LOWER(p.nome) LIKE LOWER(CONCAT('%', :#{#dto.nome}, '%')))
          AND (:#{#dto.email} IS NULL OR p.email = :#{#dto.email})
          AND (:#{#dto.senha} IS NULL OR p.senha = :#{#dto.senha})
    """)
    Page<Psicologo> buscarPor(@Param("dto") PsicologoBuscarDTO dto, Pageable pageable);

}