package br.edu.ifpb.es.cuidarme.rest;

import java.util.List;
import java.util.UUID;

import br.edu.ifpb.es.cuidarme.exception.SistemaException;
import br.edu.ifpb.es.cuidarme.rest.dto.Atendimento.AtendimentoResponseDTO;
import br.edu.ifpb.es.cuidarme.rest.dto.Paciente.PacienteResponseDTO;
import br.edu.ifpb.es.cuidarme.rest.dto.Paciente.PacienteSalvarRequestDTO;
import br.edu.ifpb.es.cuidarme.rest.dto.Psicologo.PsicologoResponseDTO;
import br.edu.ifpb.es.cuidarme.rest.dto.Psicologo.PsicologoSalvarRequestDTO;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "psicologo", description = "API Psicologos")
public interface PsicologoRestControllerApi {

    @Operation(summary = "Adicionar um psicólogo.",
               description = "Adicionar um novo psicólogo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Operação realizada com sucesso.",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = PsicologoResponseDTO.class))),
            @ApiResponse(responseCode = "500",
                    description = "Erro inesperado.",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ProblemDetail.class))),
    })
    ResponseEntity<PsicologoResponseDTO> adicionar(@RequestBody(description = "Dados do psicólogo a ser adicionado.")
                                                   PsicologoSalvarRequestDTO dto) throws SistemaException;

    @Operation(summary = "Cadastrar paciente de um psicólogo existente.",
               description = "Cadastra paciente de um psicólogo especificado pelo lookupId. O app pode enviar o lookupId do paciente "
                       + "(gerado offline); se esse paciente já existir para o mesmo psicólogo, a chamada é tratada como reenvio e retorna 200.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",
                    description = "Paciente cadastrado.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PacienteResponseDTO.class))),
            @ApiResponse(responseCode = "200",
                    description = "Reenvio: o paciente com este lookupId já existia e foi devolvido sem alterações.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PacienteResponseDTO.class))),
            @ApiResponse(responseCode = "400",
                    description = "Dados inválidos.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404",
                    description = "Psicólogo com lookupId não encontrado.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409",
                    description = "CPF já cadastrado, ou lookupId já usado por paciente de outro psicólogo.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500",
                    description = "Erro inesperado.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class))),
    })
    ResponseEntity<PacienteResponseDTO> adicionarPaciente(@Parameter(description = "Psicólogo que recebe o paciente a ser cadastrado.")
                                                  UUID lookupId,
                                                  @RequestBody(description = "Dados do paciente cadastrado.")
                                                  PacienteSalvarRequestDTO dto) throws SistemaException;

    @Operation(summary = "Retornar todos os paciente de um determinado psicólogo.",
               description = "Retorna todos os pacientes que estão vinculados a um determinado psicólogo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Operação realizada com sucesso.",
                    content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = PacienteResponseDTO.class)))),
            @ApiResponse(responseCode = "500",
                    description = "Erro inesperado.",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ProblemDetail.class))),
    })
    ResponseEntity<List<PacienteResponseDTO>> listarPacientesPorPsicologo(@PathVariable UUID psicologoId) throws SistemaException;

    @Operation(summary = "Retornar todos os atendimentos de um determinado psicólogo.",
               description = "Retorna todos os atendimentos agendados que estão vinculados a um determinado psicólogo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Operação realizada com sucesso. Retorna uma lista de atendimentos.",
                    content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = AtendimentoResponseDTO.class)))),
            @ApiResponse(responseCode = "404",
                    description = "Psicólogo não encontrado com o ID fornecido.",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500",
                    description = "Erro inesperado no servidor.",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ProblemDetail.class))),
    })
    ResponseEntity<List<AtendimentoResponseDTO>> listarAtendimentosPorPsicologo(@PathVariable UUID psicologoId) throws SistemaException;

    @Operation(summary = "Atualizar dados de um psicólogo existente.",
               description = "Atualiza dados de um psicólogo existente com base no seu lookupId.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Operação realizada com sucesso.",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = PsicologoResponseDTO.class))),
            @ApiResponse(responseCode = "400",
                    description = "Psicólogo com lookupId não encontrado.",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500",
                    description = "Erro inesperado.",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ProblemDetail.class))),
    })
    ResponseEntity<PsicologoResponseDTO> atualizar(@Parameter(description = "LookupId do psicólogo a ser atualizado.")
                                                  UUID lookupId,
                                                  @RequestBody(description = "Dados do psicólogo a ser atualizado.")
                                                  PsicologoSalvarRequestDTO dto) throws SistemaException;

    @Operation(summary = "Remover um psicólogo existente.",
            description = "Remove um psicólogo existente com base no seu lookupId.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204",
                    description = "Operação realizada com sucesso.",
                    content = @Content),
            @ApiResponse(responseCode = "400",
                    description = "Psicólogo com lookupId não encontrado.",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500",
                    description = "Erro inesperado.",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ProblemDetail.class))),
    })
    ResponseEntity<Void> remover(@Parameter(description = "LookupId do psicólogo a ser removido.")
                                 UUID lookupId) throws SistemaException;
}