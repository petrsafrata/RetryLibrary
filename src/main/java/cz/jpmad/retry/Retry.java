package cz.jpmad.retry;

import cz.jpmad.retry.functions.CheckedRunnable;
import cz.jpmad.retry.functions.CheckedSupplier;

import java.util.Objects;

public final class Retry {

    private Retry() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    public static <T> RetryConfig<T> run(CheckedSupplier<T> action) {
        Objects.requireNonNull(action);
        return new RetryConfig<>(action);
    }

    public static RetryConfig<Void> runVoid(CheckedRunnable runnable) {
        Objects.requireNonNull(runnable);
        CheckedSupplier<Void> wrapped = () -> {
            runnable.run();
            return null;
        };
        return new RetryConfig<>(wrapped);
    }
}
