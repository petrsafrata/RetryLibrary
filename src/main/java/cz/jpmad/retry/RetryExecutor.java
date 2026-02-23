package cz.jpmad.retry;

import cz.jpmad.retry.functions.CheckedSupplier;
import cz.jpmad.retry.listeners.RetryListener;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Utility class that executes an action with retry semantics based on a {@link RetryConfig}.
 * <p>
 * The executor invokes the configured {@link CheckedSupplier} up to {@code maxAttempts} times, optionally using
 * a {@link DelayStrategy} to compute delays between attempts. A {@link RetryListener} may be notified on success,
 * retry and failure events.
 * </p>
 * <p>
 * This class is not instantiable and exposes static execution methods only.
 * </p>
 */
public final class RetryExecutor {

    /**
     * Abstraction for sleeping to allow injection/mocking in tests.
     */
    interface Sleeper {

        /**
         * Sleep for the specified number of milliseconds.
         *
         * @param millis time to sleep in milliseconds
         * @throws InterruptedException if the sleep is interrupted
         */
        void sleep(long millis) throws InterruptedException;
    }

    /**
     * Default sleeper that delegates to {@link Thread#sleep(long)}.
     */
    private static final Sleeper DEFAULT_SLEEPER = Thread::sleep;

    /**
     * Private constructor to prevent instantiation.
     *
     * @throws AssertionError always thrown to indicate utility class
     */
    private RetryExecutor() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    /**
     * Execute the action described by the provided {@link RetryConfig} using the default sleeper.
     *
     * @param config retry configuration (must not be {@code null})
     * @param <T>    type of the action result
     * @return the result produced by the action when successful
     * @throws IllegalArgumentException if the configuration is invalid (null action or maxAttempts &lt; 1)
     * @throws RetryException           if all retry attempts fail
     * @throws IllegalStateException    if an unexpected internal state is encountered
     */
    public static <T> T execute(RetryConfig<T> config) {
        return execute(config, DEFAULT_SLEEPER);
    }

    /**
     * Execute the action described by the provided {@link RetryConfig} using a specific {@link Sleeper}.
     * <p>
     * The method will attempt the configured action up to {@code maxAttempts}. For each failure it will evaluate
     * the {@code retryOn} predicate (if provided) and use the {@code delayStrategy} (if provided) to compute delays
     * between attempts. The provided {@link RetryListener} (if any) will be notified on success, retry and failure.
     * </p>
     *
     * @param config  retry configuration (must not be {@code null}, must contain a non-null action and positive maxAttempts)
     * @param sleeper sleeper used to perform sleeps between retries (must not be {@code null})
     * @param <T>     type of the action result
     * @return the result produced by the action when successful
     * @throws IllegalArgumentException if the configuration is invalid or {@code sleeper} is {@code null}
     * @throws RetryException           if all retry attempts fail
     * @throws RuntimeException         if the retry sleep is interrupted (the thread's interrupt flag is re-set and the exception is wrapped)
     * @throws IllegalStateException    if an unexpected internal state is encountered
     */
    static <T> T execute(RetryConfig<T> config, Sleeper sleeper) {
        Objects.requireNonNull(sleeper, "sleeper");

        if (config == null || config.getAction() == null || config.getMaxAttempts() < 1) {
            throw new IllegalArgumentException("Invalid retry configuration");
        }

        CheckedSupplier<T> action = config.getAction();
        int maxAttempts = config.getMaxAttempts();
        long delay = config.getDelayMillis();
        Predicate<Exception> retryOn = config.getRetryOn();
        RetryListener listener = config.getListener();
        DelayStrategy delayStrategy = config.getDelayStrategy();

        Exception lastException = null;

        for (int i = 1; i <= maxAttempts; i++) {
            try {
                T result = action.get();
                if (listener != null) {
                    RetryContext context = new RetryContext(
                            i,
                            maxAttempts,
                            delayStrategy.computeDelayMillis(new RetryContext(i, maxAttempts, 0, null, i == maxAttempts)),
                            null,
                            i == maxAttempts
                    );
                    listener.onSuccess(context);
                }
                return result;
            } catch (Exception e) {
                lastException = e;
                boolean hasMoreAttempts = i < maxAttempts;
                boolean shouldRetry = hasMoreAttempts && ((retryOn == null) || (retryOn.test(e)));

                long nextDelay = 0;
                if (shouldRetry && delayStrategy != null) {
                    RetryContext ctxForDelay = new RetryContext(
                            i,
                            maxAttempts,
                            0,
                            e,
                            false
                    );
                    nextDelay = Math.max(0, delayStrategy.computeDelayMillis(ctxForDelay));
                }

                RetryContext ctx = new RetryContext(
                        i,
                        maxAttempts,
                        nextDelay,
                        e,
                        !shouldRetry
                );

                if (shouldRetry) {
                    if (listener != null) {
                        listener.onRetry(ctx);
                    }
                    if (nextDelay > 0) {
                        try {
                            sleeper.sleep(nextDelay);
                        } catch (InterruptedException e1) {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException("Retry interrupted", e1);
                        }
                    }
                } else {
                    if (listener != null) {
                        listener.onFailure(ctx);
                    }
                    throw new RetryException("Retry failed: " + lastException);
                }
            }
        }
        throw new IllegalStateException("This state is not supported: " + lastException);
    }
}
