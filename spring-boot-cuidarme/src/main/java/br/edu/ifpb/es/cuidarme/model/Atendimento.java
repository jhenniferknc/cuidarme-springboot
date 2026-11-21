package br.edu.ifpb.es.cuidarme.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Entity
@Table(name = "Atendimento")
public class Atendimento {

    @Id
    @Column(nullable = false, unique = true)
    private UUID lookupId;

    @Setter
    @Getter
    @Column(name = "DATA", nullable = false)
    private LocalDateTime data;

    @Setter
    @Getter
    @Column(name = "LOCALIDADE", nullable = false)
    private String localidade;

    @Setter
    @Getter
    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false)
    private StatusAtendimento status;

    @Setter
    @Getter
    @ManyToOne
    @JoinColumn(name = "PSICOLOGO_ID")
    private Psicologo psicologo;

    @Setter
    @Getter
    @ManyToOne
    @JoinColumn(name = "PACIENTE_ID")
    private Paciente paciente;
    @Column(name = "ATUALIZADO_EM")
    private LocalDateTime atualizadoEm;

    @PrePersist
    private void init() {
        if (this.lookupId == null) {
            this.lookupId = UUID.randomUUID();
        }
        this.atualizadoEm = LocalDateTime.now();
    }

    @PreUpdate
    private void aoAtualizar() {
        this.atualizadoEm = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Atendimento that = (Atendimento) o;
        return Objects.equals(lookupId, that.lookupId) && Objects.equals(data, that.data) && Objects.equals(localidade, that.localidade) && status == that.status && Objects.equals(psicologo, that.psicologo) && Objects.equals(paciente, that.paciente);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lookupId, data, localidade, status, psicologo, paciente);
    }

    @Override
    public String toString() {
        return "Atendimento{" +
                ", lookupId=" + lookupId +
                ", data=" + data +
                ", localidade='" + localidade + '\'' +
                ", status=" + status +
                ", psicologo=" + psicologo +
                ", paciente=" + paciente +
                '}';
    }
}
