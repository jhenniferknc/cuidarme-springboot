package br.edu.ifpb.es.cuidarme.exception;

public class ConflitoException extends SistemaException {
    private static final long serialVersionUID = 1L;

    public ConflitoException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConflitoException(String message) {
        super(message);
    }
}
