package br.edu.ifpb.es.cuidarme.exception;

public class NaoEncontradoException extends SistemaException {
    private static final long serialVersionUID = 1L;

    public NaoEncontradoException(String message, Throwable cause) {
        super(message, cause);
    }

    public NaoEncontradoException(String message) {
        super(message);
    }
}
