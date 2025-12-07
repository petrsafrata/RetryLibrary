package cz.jpmad.retry;

public class RetryException extends RuntimeException {
    public RetryException(String message) {
        super(message);
    }
}
