package br.edu.ifpb.es.cuidarme.rest;

import java.util.List;
import java.util.UUID;

import br.edu.ifpb.es.cuidarme.exception.SistemaException;
import br.edu.ifpb.es.cuidarme.rest.dto.Atendimento.AtendimentoResponseDTO;
import br.edu.ifpb.es.cuidarme.rest.dto.Atendimento.AtendimentoSalvarRequestDTO;
import br.edu.ifpb.es.cuidarme.rest.dto.Pagamento.PagamentoResponseDTO;
import br.edu.ifpb.es.cuidarme.rest.dto.Pagamento.PagamentoSalvarRequestDTO;
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

@Tag(name = "atendimento", description = "API Atendimentos")
public interface AtendimentoRestControllerApi {

    @Operation(summary = "Retornar todos os atendimentos.",
               description = "Retorna todos os atendimentos que estão armazenados.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Operação realizada com sucesso.",
                    content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = AtendimentoResponseDTO.class)))),
            @ApiResponse(responseCode = "500",
                    description = "Erro inesperado.",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ProblemDetail.class))),
    })
    ResponseEntity<List<AtendimentoResponseDTO>> listar() throws SistemaException;

    @Operation(summary = "Buscar um atendimento existente.",
            description = "Recupera um atendimento existente com base no seu lookupId.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Operação realizada com sucesso.",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = AtendimentoResponseDTO.class))),
            @ApiResponse(responseCode = "400",
                    description = "Atendimento com lookupId não encontrado.",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500",
                    description = "Erro inesperado.",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ProblemDetail.class))),
    })
    ResponseEntity<AtendimentoResponseDTO> buscarPorId(@Parameter(description = "LookupId do atendimento a ser recuperado.")
                                                     UUID lookupId) throws SistemaException;

    @Operation(summary = "Atualizar dados de um atendimento existente.",
            description = "Atualiza dados de um atendimento existente com base no seu lookupId.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Operação realizada com sucesso.",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = AtendimentoResponseDTO.class))),
            @ApiResponse(responseCode = "404",
                    description = "Atendimento com lookupId não encontrado.",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500",
                    description = "Erro inesperado.",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ProblemDetail.class))),
    })
    ResponseEntity<AtendimentoResponseDTO> atualizar(@Parameter(description = "LookupId do atendimento a ser atualizado.")
                                                  UUID lookupId,
                                                  @RequestBody(description = "Dados do atendimento a ser atualizado.")
                                                     AtendimentoSalvarRequestDTO dto) throws SistemaException;

    @Operation(summary = "Cadastrar um novo pagamento para um atendimento específico.",
               description = "Cria um novo registro de pagamento e o associa a um atendimento existente, utilizando o ID. Após o pagamento, o status do atendimento é atualizado.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Pagamento cadastrado com sucesso. Retorna os dados do pagamento criado.",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = PagamentoResponseDTO.class))),
            @ApiResponse(responseCode = "400",
                    description = "Dados inválidos fornecidos no corpo da requisição (ex: valor ausente).",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404",
                    description = "Atendimento não encontrado com o ID fornecido na URL.",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500",
                    description = "Erro inesperado no servidor.",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ProblemDetail.class))),
    })
    ResponseEntity<PagamentoResponseDTO> adicionarPagamento(
            @PathVariable UUID atendimentoId,
            @RequestBody @Valid PagamentoSalvarRequestDTO dto) throws SistemaException;

}