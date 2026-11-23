package br.edu.ifpb.es.cuidarme.rest;

import java.util.List;
import java.util.UUID;

import br.edu.ifpb.es.cuidarme.exception.SistemaException;
import br.edu.ifpb.es.cuidarme.rest.dto.Atendimento.AtendimentoResponseDTO;
import br.edu.ifpb.es.cuidarme.rest.dto.Atendimento.AtendimentoSalvarRequestDTO;
import br.edu.ifpb.es.cuidarme.rest.dto.Paciente.PacienteResponseDTO;
import br.edu.ifpb.es.cuidarme.rest.dto.Paciente.PacienteSalvarRequestDTO;
import br.edu.ifpb.es.cuidarme.rest.dto.Prontuario.ProntuarioResponseDTO;
import br.edu.ifpb.es.cuidarme.rest.dto.Prontuario.ProntuarioSalvarRequestDTO;
import jakarta.validation.Valid;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "paciente", description = "API Pacientes")
public interface PacienteRestControllerApi {

    @Operation(summary = "Retornar todos os pacientes.",
            description = "Retorna todos os pacientes que estão armazenados, sem restrição de vínculo à um psicólogo específico.")
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
    ResponseEntity<List<PacienteResponseDTO>> listar() throws SistemaException;

    @Operation(summary = "Atualizar dados de um paciente existente.",
            description = "Atualiza dados de um paciente existente com base no seu lookupId. O campo lookupId do corpo é ignorado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Operação realizada com sucesso.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PacienteResponseDTO.class))),
            @ApiResponse(responseCode = "400",
                    description = "Dados inválidos.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404",
                    description = "Paciente com lookupId não encontrado.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409",
                    description = "Já existe outro paciente com este CPF.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500",
                    description = "Erro inesperado.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class))),
    })
    ResponseEntity<PacienteResponseDTO> atualizar(@Parameter(description = "LookupId do paciente a ser atualizado.")
                                                  UUID lookupId,
                                                  @RequestBody(description = "Dados do paciente a ser atualizado.")
                                                  PacienteSalvarRequestDTO dto) throws SistemaException;

    @Operation(summary = "Remover um paciente existente.",
            description = "Remove um paciente com base no seu lookupId. É idempotente: se o paciente não existir, também retorna 204.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204",
                    description = "Paciente removido (ou já não existia).",
                    content = @Content),
            @ApiResponse(responseCode = "500",
                    description = "Erro inesperado.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class))),
    })
    ResponseEntity<Void> remover(@Parameter(description = "LookupId do paciente a ser removido.")
                                 UUID lookupId) throws SistemaException;

    @Operation(summary = "Cadastrar um novo atendimento para um paciente.",
            description = "Cria e associa um novo atendimento a um paciente existente. O app pode enviar o lookupId do atendimento "
                    + "(gerado offline); se esse atendimento já existir para o mesmo paciente, a chamada é tratada como reenvio e retorna 200.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",
                    description = "Atendimento cadastrado.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AtendimentoResponseDTO.class))),
            @ApiResponse(responseCode = "200",
                    description = "Reenvio: o atendimento com este lookupId já existia e foi devolvido sem alterações.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AtendimentoResponseDTO.class))),
            @ApiResponse(responseCode = "400",
                    description = "Dados inválidos (ex: data ou localidade ausentes).",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404",
                    description = "Paciente não encontrado.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409",
                    description = "lookupId já usado por atendimento de outro paciente.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500",
                    description = "Erro inesperado.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class))),
    })
    ResponseEntity<AtendimentoResponseDTO> adicionarAtendimento(
            @PathVariable UUID pacienteId,
            @RequestBody @Valid AtendimentoSalvarRequestDTO dto) throws SistemaException;

    @Operation(summary = "Cadastrar um novo registro de prontuário para um paciente.",
               description = "Cria uma nova anotação no prontuário e a associa a um paciente existente, utilizando o ID do paciente fornecido na URL.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",
                    description = "Registro de prontuário criado com sucesso. Retorna os dados do registro criado.",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ProntuarioResponseDTO.class))),
            @ApiResponse(responseCode = "400",
                    description = "Dados inválidos fornecidos no corpo da requisição (ex: anotações em branco).",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404",
                    description = "Paciente não encontrado com o ID fornecido na URL.",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500",
                    description = "Erro inesperado no servidor.",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ProblemDetail.class))),
    })
    ResponseEntity<ProntuarioResponseDTO> adicionarProntuario(
            @PathVariable UUID pacienteId,
            @RequestBody @Valid ProntuarioSalvarRequestDTO dto) throws SistemaException;

    @Operation(summary = "Retornar todos os atendimentos de um determinado paciente.",
               description = "Retorna uma lista com todos os atendimentos (passados e futuros) que estão vinculados a um paciente específico, identificado pelo seu ID na URL.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Operação realizada com sucesso. Retorna a lista de atendimentos.",
                    content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = AtendimentoResponseDTO.class)))),
            @ApiResponse(responseCode = "404",
                    description = "Paciente não encontrado com o ID fornecido.",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500",
                    description = "Erro inesperado no servidor.",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ProblemDetail.class))),
    })
    ResponseEntity<List<AtendimentoResponseDTO>> listarAtendimentosPorPaciente(@PathVariable UUID pacienteId) throws SistemaException;

    @Operation(summary = "Recuperar um paciente específico pelo seu ID.",
            description = "Retorna os dados completos de um único paciente com base no seu lookupId.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Operação realizada com sucesso."),
            @ApiResponse(
                    responseCode = "404",
                    description = "Paciente não encontrado."),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro inesperado."),
    })
    ResponseEntity<PacienteResponseDTO> buscarPorId(@PathVariable UUID pacienteId) throws SistemaException;

}