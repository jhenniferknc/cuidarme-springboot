package br.edu.ifpb.es.cuidarme.rest.advice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import br.edu.ifpb.es.cuidarme.exception.ConflitoException;
import br.edu.ifpb.es.cuidarme.exception.NaoAutorizadoException;
import br.edu.ifpb.es.cuidarme.exception.NaoEncontradoException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.dao.DataIntegrityViolationException;

import br.edu.ifpb.es.cuidarme.exception.EstadoInvalidoException;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);


	static enum ErrorType {
		ERRO_INESPERADO, REQUISICAO_INVALIDA, ESTADO_INVÁLIDO, ERRO_DE_VALIDAÇÃO, NAO_ENCONTRADO, CONFLITO, NAO_AUTORIZADO;
	}
	
	@ExceptionHandler(Exception.class)
	public ProblemDetail handleException(Exception ex) {
		log.error("Erro não tratado.", ex);
		return buildProblemDetail("Erro inesperado no servidor.", HttpStatus.INTERNAL_SERVER_ERROR, ErrorType.ERRO_INESPERADO);
	}
	
	@ExceptionHandler(IllegalArgumentException.class)
	public ProblemDetail handleIllegalArgumentException(IllegalArgumentException ex) {
		return buildProblemDetail(ex, HttpStatus.BAD_REQUEST, ErrorType.REQUISICAO_INVALIDA);
	}
	
	@ExceptionHandler(EstadoInvalidoException.class)
	public ProblemDetail handleEstadoInválidoException(EstadoInvalidoException ex) {
		return buildProblemDetail(ex, HttpStatus.BAD_REQUEST, ErrorType.ESTADO_INVÁLIDO);
	}

	@ExceptionHandler(NaoEncontradoException.class)
	public ProblemDetail handleNaoEncontradoException(NaoEncontradoException ex) {
		return buildProblemDetail(ex, HttpStatus.NOT_FOUND, ErrorType.NAO_ENCONTRADO);
	}

	@ExceptionHandler(NaoAutorizadoException.class)
	public ProblemDetail handleNaoAutorizadoException(NaoAutorizadoException ex){
		return buildProblemDetail(ex, HttpStatus.UNAUTHORIZED, ErrorType.NAO_AUTORIZADO);
	}

	@ExceptionHandler(ConflitoException.class)
	public ProblemDetail handleConflitoException(ConflitoException ex) {
		return buildProblemDetail(ex, HttpStatus.CONFLICT, ErrorType.CONFLITO);
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ProblemDetail handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
		return buildProblemDetail("Já existe um registro com esses dados", HttpStatus.CONFLICT, ErrorType.CONFLITO);
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(
			MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(handleMethodArgumentNotValidException(ex));
	}
	
	private ProblemDetail handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
	    Map<String, String> errors = new HashMap<>();
	    ex.getBindingResult().getAllErrors().forEach((error) -> {
	        String fieldName = ((FieldError) error).getField();
	        String errorMessage = error.getDefaultMessage();
	        errors.put(fieldName, errorMessage);
	    });
	    ProblemDetail problemDetail = buildProblemDetail(ex, HttpStatus.BAD_REQUEST, ErrorType.ERRO_DE_VALIDAÇÃO);
	    problemDetail.setProperty("erros", errors);
	    return problemDetail;
	}

	private ProblemDetail buildProblemDetail(Exception ex, HttpStatus status, ErrorType type) {
		return buildProblemDetail(ex.getLocalizedMessage(), status, type);
	}

	private ProblemDetail buildProblemDetail(String detail, HttpStatus status, ErrorType type) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
		problemDetail.setType(URI.create(type.name()));
		problemDetail.setProperty("timestamp", LocalDateTime.now());
		return problemDetail;
	}

}
