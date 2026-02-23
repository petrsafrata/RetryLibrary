package cz.jpmad.retry;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Strategy for computing the delay (in milliseconds) to wait before the next retry attempt.
 * <p>
 * Implementations are expected to be side-effect free and thread-safe. The computed value
 * must be non-negative and expresses the time to sleep before the next retry.
 * <p>
 * Nullability: implementations must not be called with a {@code null} {@link RetryContext}.
 */
@FunctionalInterface
public interface DelayStrategy {

    /**
     * Compute the delay in milliseconds for the upcoming wait based on the provided retry context.
     * <p>
     * Implementations should return a non-negative value. A return value of {@code 0} indicates no wait.
     *
     * @param ctx the retry context to use for delay calculation; must not be {@code null}
     * @return non-negative delay in milliseconds to wait before the next attempt
     */
    long computeDelayMillis(RetryContext ctx);

    /**
     * Create a strategy that always returns a fixed delay.
     *
     * @param delayMillis fixed delay in milliseconds; must be {@code >= 0}
     * @return a {@link DelayStrategy} that always returns {@code delayMillis}
     * @throws IllegalArgumentException if {@code delayMillis < 0}
     */
    static DelayStrategy fixed(long delayMillis) {
        if (delayMillis < 0) {
            throw new IllegalArgumentException("delayMillis must be >= 0");
        }
        return ctx -> delayMillis;
    }

    /**
     * Create an exponential backoff strategy.
     * <p>
     * The computed delay grows by the given {@code multiplier} for successive attempts and is
     * bounded by {@code maxDelayMillis}. The first attempt (attempt == 1) yields {@code initialDelayMillis}.
     * Computation is clamped to {@code Long.MAX_VALUE} to avoid overflow.
     *
     * @param initialDelayMillis initial delay in milliseconds for attempt 1; must be {@code >= 0}
     * @param multiplier multiplicative factor applied for each subsequent attempt; must be {@code >= 1.0}
     * @param maxDelayMillis maximum delay in milliseconds; must be {@code >= 0}
     * @return a {@link DelayStrategy} implementing exponential backoff bounded by {@code maxDelayMillis}
     * @throws IllegalArgumentException if any parameter is out of the required range
     */
    static DelayStrategy exponentialBackoff(long initialDelayMillis, double multiplier, long maxDelayMillis) {
        if (initialDelayMillis < 0) {
            throw new IllegalArgumentException("initialDelayMillis must be >= 0");
        }
        if (multiplier < 1.0) {
            throw new IllegalArgumentException("multiplier must be >= 1.0");
        }
        if (maxDelayMillis < 0) {
            throw new IllegalArgumentException("maxDelayMillis must be >= 0");
        }

        return ctx -> {
            // attempt: 1 = first attempt (no retry yet), retry delays apply after failures
            // If we compute delay after failure of attempt i, then next delay is based on i (or i+1). We'll use i.
            int attempt = ctx.attempt();
            long delay = initialDelayMillis;

            // For attempt 1, delay = initialDelayMillis, for attempt 2 => initial * multiplier, etc.
            // (attempt - 1) multiplications
            for (int k = 1; k < attempt; k++) {
                double next = delay * multiplier;
                delay = next > Long.MAX_VALUE ? Long.MAX_VALUE : (long) next;
                if (delay >= maxDelayMillis) {
                    delay = maxDelayMillis;
                    break;
                }
            }

            return Math.min(delay, maxDelayMillis);
        };
    }

    /**
     * Decorate this strategy with random jitter.
     * <p>
     * The jittered delay is chosen uniformly from the range
     * [d * (1 - jitterRatio), d * (1 + jitterRatio)], where {@code d} is the base delay computed
     * by this strategy. If the base delay is {@code 0} or less, zero is returned.
     *
     * @param jitterRatio relative jitter ratio in range {@code [0.0, 1.0]}; must not be outside this range
     * @return a new {@link DelayStrategy} that applies random jitter to the base strategy's delay
     * @throws IllegalArgumentException if {@code jitterRatio} is not in {@code [0.0, 1.0]}
     */
    default DelayStrategy withJitter(double jitterRatio) {
        if (jitterRatio < 0.0 || jitterRatio > 1.0) {
            throw new IllegalArgumentException("jitterRatio must be in range [0.0, 1.0]");
        }
        DelayStrategy base = this;
        return ctx -> {
            long d = base.computeDelayMillis(ctx);
            if (d <= 0) return 0;

            // jitter range: [d*(1-j), d*(1+j)]
            double min = d * (1.0 - jitterRatio);
            double max = d * (1.0 + jitterRatio);

            long lo = (long) Math.floor(min);
            long hi = (long) Math.ceil(max);

            if (hi < lo) return d;
            if (hi == lo) return lo;

            return ThreadLocalRandom.current().nextLong(lo, hi + 1);
        };
    }

    /**
     * A strategy that always returns zero delay.
     *
     * @return a {@link DelayStrategy} that returns {@code 0} for any context
     */
    static DelayStrategy none() {
        return fixed(0);
    }

}
