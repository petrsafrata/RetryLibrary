package cz.jpmad.retry;

public record RetryContext(
        int attempt,
        int maxAttempts,
        long delay,
        Exception lastException,
        boolean lastAttempt
) {
}
