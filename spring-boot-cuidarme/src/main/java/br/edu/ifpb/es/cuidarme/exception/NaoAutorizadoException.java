package br.edu.ifpb.es.cuidarme.exception;

public class NaoAutorizadoException extends SistemaException {
    private static final long serialVersionUID = 1L;

    public NaoAutorizadoException(String message, Throwable cause) {
        super(message, cause);
    }

    public NaoAutorizadoException(String message) {
        super(message);
    }}
