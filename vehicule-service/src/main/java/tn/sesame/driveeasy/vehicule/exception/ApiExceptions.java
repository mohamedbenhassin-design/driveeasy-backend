package tn.sesame.driveeasy.vehicule.exception;

/**
 * Exceptions métier. Chacune est traduite en code HTTP par GlobalExceptionHandler :
 * NotFound -> 404, Conflict -> 409, Business -> 400.
 */
public final class ApiExceptions {

    private ApiExceptions() {}

    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String message) { super(message); }
    }

    public static class ConflictException extends RuntimeException {
        public ConflictException(String message) { super(message); }
    }

    public static class BusinessException extends RuntimeException {
        public BusinessException(String message) { super(message); }
    }
}
