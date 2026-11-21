package br.edu.ifpb.es.cuidarme.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
@Table(name = "Paciente")
public class Paciente {

    @Id
    @Column(nullable = false, unique = true)
    private UUID lookupId;

    @Setter
    @Getter
    @Column(name = "NOME", nullable = false)
    private String nome;

    @Setter
    @Getter
    @Column(name = "CPF", unique = true, nullable = false)
    private String cpf;

    @Setter
    @Getter
    @Column(name = "RG", nullable = false)
    private String rg;

    @Setter
    @Getter
    @Column(name = "DATA_NASCIMENTO", nullable = false)
    private LocalDate dataNascimento;

    @Setter
    @Getter
    @Column(name = "SEXO", nullable = false)
    private String sexo;

    @Setter
    @Getter
    @Column(name = "ESTADO_CIVIL", nullable = false)
    private String estadoCivil;

    @Setter
    @Getter
    @Column(name = "GRAU_INSTRUCAO", nullable = false)
    private String grauInstrucao;

    @Setter
    @Getter
    @Column(name = "PROFISSAO", nullable = false)
    private String profissao;

    @Setter
    @Getter
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "lookupId", column = @Column(name = "pessoal_lookup_id")),
            @AttributeOverride(name="cep", column=@Column(name = "CEP_PESSOAL", nullable = false)),
            @AttributeOverride(name="logradouro", column=@Column(name = "LOGRADOURO_PESSOAL", nullable = false)),
            @AttributeOverride(name="numero", column=@Column(name = "NUMERO_PESSOAL")),
            @AttributeOverride(name="cidade", column=@Column(name = "CIDADE_PESSOAL", nullable = false)),
            @AttributeOverride(name="estado", column=@Column(name = "ESTADO_PESSOAL", nullable = false)),
    })
    private Endereco enderecoPessoal;

    @Setter
    @Getter
    @Column(name = "TELEFONE", nullable = false)
    private String telefone;

    @Setter
    @Getter
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "lookupId", column = @Column(name = "trabalho_lookup_id")),
            @AttributeOverride(name="cep", column=@Column(name = "CEP_TRABALHO")),
            @AttributeOverride(name="logradouro", column=@Column(name = "LOGRADOURO_TRABALHO")),
            @AttributeOverride(name="numero", column=@Column(name = "NUMERO_TRABALHO")),
            @AttributeOverride(name="cidade", column=@Column(name = "CIDADE_TRABALHO")),
            @AttributeOverride(name="estado", column=@Column(name = "ESTADO_TRABALHO"))
    })
    private Endereco enderecoTrabalho;

    @Setter
    @Getter
    @Column(name = "INFOS_ADICIONAIS")
    private String infoAdicionais;

    @Setter
    @Getter
    @Embedded
    @Column(name = "CONTATO_EMERGENCIA")
    @AttributeOverrides({
            @AttributeOverride(name = "lookupId", column = @Column(name = "emergencia_lookup_id")),
            @AttributeOverride(name="nome", column=@Column(name = "NOME_CONTATOEMERGENCIA")),
            @AttributeOverride(name="telefone", column =@Column(name = "TELEFONE_CONTATOEMERGENCIA")),
            @AttributeOverride(name="parentesco", column =@Column(name = "PARENTESCO_CONTATOEMERGENCIA"))
    })
    private ContatoEmergencia contatoEmergencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "psicologo_lookup_id", referencedColumnName = "lookupId")
    private Psicologo psicologo;

    @Setter
    @Getter
    @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Atendimento> atendimentos = new ArrayList<>();

    @Setter
    @Getter
    @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Prontuario> prontuarios = new ArrayList<>();

    @Setter
    @Getter
    @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pagamento> pagamentos = new ArrayList<>();
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
        Paciente paciente = (Paciente) o;
        return Objects.equals(lookupId, paciente.lookupId) && Objects.equals(nome, paciente.nome) && Objects.equals(cpf, paciente.cpf) && Objects.equals(rg, paciente.rg) && Objects.equals(dataNascimento, paciente.dataNascimento) && Objects.equals(sexo, paciente.sexo) && Objects.equals(estadoCivil, paciente.estadoCivil) && Objects.equals(grauInstrucao, paciente.grauInstrucao) && Objects.equals(profissao, paciente.profissao) && Objects.equals(enderecoPessoal, paciente.enderecoPessoal) && Objects.equals(telefone, paciente.telefone) && Objects.equals(enderecoTrabalho, paciente.enderecoTrabalho) && Objects.equals(infoAdicionais, paciente.infoAdicionais) && Objects.equals(contatoEmergencia, paciente.contatoEmergencia) && Objects.equals(psicologo, paciente.psicologo) && Objects.equals(atendimentos, paciente.atendimentos) && Objects.equals(prontuarios, paciente.prontuarios) && Objects.equals(pagamentos, paciente.pagamentos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lookupId, nome, cpf, rg, dataNascimento, sexo, estadoCivil, grauInstrucao, profissao, enderecoPessoal, telefone, enderecoTrabalho, infoAdicionais, contatoEmergencia, psicologo, atendimentos, prontuarios, pagamentos);
    }

    @Override
    public String toString() {
        return "Paciente{" +
                "lookupId=" + lookupId +
                ", nome='" + nome + '\'' +
                ", cpf='" + cpf + '\'' +
                ", rg='" + rg + '\'' +
                ", dataNascimento=" + dataNascimento +
                ", sexo='" + sexo + '\'' +
                ", estadoCivil='" + estadoCivil + '\'' +
                ", grauInstrucao='" + grauInstrucao + '\'' +
                ", profissao='" + profissao + '\'' +
                ", enderecoPessoal=" + enderecoPessoal +
                ", telefone='" + telefone + '\'' +
                ", enderecoTrabalho=" + enderecoTrabalho +
                ", infoAdicionais='" + infoAdicionais + '\'' +
                ", contatoEmergencia=" + contatoEmergencia +
                ", psicologo=" + psicologo +
                ", atendimentos=" + atendimentos +
                ", prontuarios=" + prontuarios +
                ", pagamentos=" + pagamentos +
                '}';
    }
}
