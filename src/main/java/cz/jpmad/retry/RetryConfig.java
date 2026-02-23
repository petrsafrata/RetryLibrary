package cz.jpmad.retry;

import cz.jpmad.retry.functions.CheckedSupplier;
import cz.jpmad.retry.listeners.RetryListener;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Configuration holder for retrying a checked action.
 * <p>
 * Encapsulates retry parameters (action, max attempts, fixed delay, retry predicate,
 * listener and delay strategy) with fluent setters. Provides sensible defaults
 * and is intended to be consumed by {@link RetryExecutor} or via the {@link #execute()} helper.
 * <p>
 * Key points:
 * - The constructor requires a non-null {@code CheckedSupplier}; passing null is not allowed.
 * - Setters validate inputs and may throw {@link IllegalArgumentException}; {@code setRetryOn}
 *   and {@code setDelayStrategy} throw {@link NullPointerException} on null.
 * - Instances are mutable and not thread-safe; setters mutate internal state (side effect).
 *
 * @param <T> the type of value produced by the configured action
 */
public class RetryConfig<T> {

    private final CheckedSupplier<T> action;
    private int maxAttempts;
    private long delayMillis;

    private Predicate<Exception> retryOn;

    private RetryListener listener;

    private DelayStrategy delayStrategy;

    public RetryConfig(CheckedSupplier<T> action) {
        this.action = action;
        this.maxAttempts = 3;
        this.delayMillis = 0;
        this.retryOn = e -> true;
        this.listener = null;
        this.delayStrategy = DelayStrategy.none();
    }

    public RetryConfig<T> setMaxAttempts(int maxAttempts) {
        if (maxAttempts < 0) {
            throw new IllegalArgumentException("maxAttempts must be greater than 0");
        }
        this.maxAttempts = maxAttempts;
        return this;
    }

    public RetryConfig<T> setDelayMillis(long delayMillis) {
        if (delayMillis < -1) {
            throw new IllegalArgumentException("delayMillis must be positive number");
        }
        this.delayMillis = delayMillis;
        return this;
    }

    public RetryConfig<T> setListener(RetryListener listener) {
        this.listener = listener;
        return this;
    }

    public RetryConfig<T> setRetryOn(Predicate<Exception> retryOn) {
        this.retryOn = Objects.requireNonNull(retryOn, "retryOn");
        return this;
    }

    public RetryConfig<T> setDelayStrategy(DelayStrategy delayStrategy) {
        this.delayStrategy = Objects.requireNonNull(delayStrategy, "delayStrategy");
        return this;
    }

    public CheckedSupplier<T> getAction() {
        return action;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public long getDelayMillis() {
        return delayMillis;
    }

    public Predicate<Exception> getRetryOn() {
        return retryOn;
    }

    public RetryListener getListener() {
        return listener;
    }

    public DelayStrategy getDelayStrategy() {
        return delayStrategy;
    }

    public T execute() throws InterruptedException {
        return RetryExecutor.execute(this);
    }
}
