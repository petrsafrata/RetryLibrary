package cz.jpmad.retry;

import cz.jpmad.retry.functions.CheckedRunnable;
import cz.jpmad.retry.functions.CheckedSupplier;

import java.util.Objects;

/**
 * Utility factory for creating {@link RetryConfig} instances to configure retries for checked actions.
 * <p>
 * Provides two entry points:
 * - {@link #run(CheckedSupplier)} for actions returning a value.
 * - {@link #runVoid(CheckedRunnable)} for void actions (wrapped to return {@code null}).
 * <p>
 * The returned {@link RetryConfig} is mutable and intended to be consumed by {@link RetryExecutor}.
 * The class is non-instantiable and contains only static helpers.
 * <p>
 * Side effects: allocates and returns a new {@link RetryConfig} instance.
 * <p>
 * Nullability: input arguments must not be {@code null}; passing {@code null} causes a {@link NullPointerException}.
 *
 * @throws NullPointerException if a provided action or runnable is {@code null}
 */
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
