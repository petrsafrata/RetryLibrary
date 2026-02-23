package cz.jpmad.retry;

/**
 * Immutable context describing a single retry attempt.
 * <p>
 * Provides metadata about the current attempt within a retry operation,
 * including the attempt index, configured maximum attempts, computed delay,
 * the last exception that occurred (if any) and whether this is the final attempt.
 * <p>
 * Side effects: none (immutable record).
 * <p>
 * Nullability: {@code lastException} may be {@code null} when no exception occurred or on first attempt.
 *
 * @param attempt current attempt number (1-based)
 * @param maxAttempts configured maximum number of attempts (>= 1)
 * @param delay computed delay in milliseconds for the upcoming wait (>= 0)
 * @param lastException the exception thrown by the action on this attempt, or {@code null} if none
 * @param lastAttempt {@code true} if this attempt is the final allowed attempt, {@code false} otherwise
 */
public record RetryContext(
        int attempt,
        int maxAttempts,
        long delay,
        Exception lastException,
        boolean lastAttempt
) {
}
