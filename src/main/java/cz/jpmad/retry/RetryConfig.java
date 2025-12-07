package cz.jpmad.retry;

import cz.jpmad.retry.functions.CheckedSupplier;
import cz.jpmad.retry.listeners.RetryListener;

import java.util.function.Predicate;

public class RetryConfig<T> {

    private final CheckedSupplier<T> action;
    private int maxAttempts;
    private long delayMillis;

    private Predicate<Exception> retryOn;

    private RetryListener listener;

    public RetryConfig(CheckedSupplier<T> action) {
        this.action = action;
        this.maxAttempts = 3;
        this.delayMillis = 0;
        this.retryOn = e -> true;
        this.listener = null;
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
        this.retryOn = retryOn;
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

    public T execute() throws InterruptedException {
        return RetryExecutor.execute(this);
    }
}
