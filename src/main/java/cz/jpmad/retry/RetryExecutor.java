package cz.jpmad.retry;

import cz.jpmad.retry.functions.CheckedSupplier;
import cz.jpmad.retry.listeners.RetryListener;

import java.util.function.Predicate;

public final class RetryExecutor {

    private RetryExecutor() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    public static <T> T execute(RetryConfig<T> config) {
        if (config == null || config.getAction() == null || config.getMaxAttempts() < 1) {
            throw new IllegalArgumentException("Invalid retry configuration");
        }

        CheckedSupplier<T> action = config.getAction();
        int maxAttempts = config.getMaxAttempts();
        long delay = config.getDelayMillis();
        Predicate<Exception> retryOn = config.getRetryOn();
        RetryListener listener = config.getListener();

        Exception lastException = null;

        for (int i = 1; i <= maxAttempts; i++) {
            try {
                T result = action.get();
                if (listener != null) {
                    RetryContext context = new RetryContext(i, maxAttempts, delay, null, true);
                    listener.onSuccess(context);
                }
                return result;
            } catch (Exception e) {
                lastException = e;
                boolean hasMoreAttempts = i < maxAttempts;
                boolean shouldRetry = hasMoreAttempts && ((retryOn == null) || (retryOn.test(e)));

                if (shouldRetry) {
                    if (listener != null) {
                        RetryContext context = new RetryContext(i, maxAttempts, delay, e, false);
                        listener.onRetry(context);
                    }
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e1) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Retry interrupted", e1);
                    }
                } else {
                    if (listener != null) {
                        RetryContext context = new RetryContext(i, maxAttempts, delay, e, true);
                        listener.onFailure(context);
                    }
                    throw new RetryException("Retry failed: " + lastException);
                }
            }
        }
        throw new IllegalStateException("This state is not supported: " + lastException);
    }
}
