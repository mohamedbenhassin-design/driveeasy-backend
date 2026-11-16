package tn.sesame.driveeasy.location.exception;

/**
 * Exceptions métier. Chacune est traduite en code HTTP par GlobalExceptionHandler :
 * NotFound -> 404, Conflict -> 409, Business -> 400, RemoteService -> 503.
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

    /** Un microservice distant est injoignable ou a renvoyé une erreur inattendue. */
    public static class RemoteServiceException extends RuntimeException {
        public RemoteServiceException(String message) { super(message); }
    }
}
