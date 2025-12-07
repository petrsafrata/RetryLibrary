package cz.jpmad.retry;

import cz.jpmad.retry.listeners.RetryListener;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class RetryTest {

    @Test
    public void successOnFirstAttempt() throws Exception {
        AtomicInteger attempts = new AtomicInteger(0);
        String result = Retry
                .run(() -> {
                    attempts.incrementAndGet();
                    return "OK";
                }).execute();

        assertEquals("OK", result);
        assertEquals(1, attempts.get(), "The action should only be called once.");

    }

    @Test
    public void retriesUntilSuccess() throws Exception {
        AtomicInteger attempts = new AtomicInteger(0);
        String result = Retry
                .run(() -> {
                    int current = attempts.incrementAndGet();
                    if (current < 3) {
                        throw new RetryException("Temporary failure " + current);
                    }
                    return "OK";
                })
                .setMaxAttempts(5)
                .setDelayMillis(0)
                .execute();

        assertEquals("OK", result);
        assertEquals(3, attempts.get(), "There should have been 3 attempts (2 fail, 1 success)");
    }

    @Test
    public void failsAfterMaxAttempts() throws Exception {
        AtomicInteger attempts = new AtomicInteger(0);
        RetryConfig<String> config = Retry
                .<String>run(() -> {
                    attempts.incrementAndGet();
                    throw new RuntimeException("Always failing");
                })
                .setMaxAttempts(3)
                .setDelayMillis(0);

        RetryException ex = assertThrows(
                RetryException.class,
                config::execute,
                "After 3 attempts, a RetryException should be thrown"
        );

        assertEquals(3, attempts.get(), "There are exactly 3 attempts to be made");
        assertTrue(ex.getMessage().contains("Retry failed"));
    }

    @Test
    public void retryOnlyOnSpecificException() throws Exception {
        AtomicInteger attempts = new AtomicInteger(0);
        RetryConfig<String> config = Retry
                .<String>run(() -> {
                    attempts.incrementAndGet();
                    if (attempts.get() == 1) {
                        throw new IllegalArgumentException("No retry for this");
                    }
                    return "OK";
                })
                .setMaxAttempts(5)
                .setDelayMillis(0)
                .setRetryOn(ex -> ex instanceof RuntimeException && !(ex instanceof IllegalArgumentException));

        assertThrows(RetryException.class, config::execute);
        assertEquals(1, attempts.get(), "There must be no more than one attempt.");
    }

    @Test
    public void runVoidWithRetries() throws Exception {
        AtomicInteger attempts = new AtomicInteger(0);
        Retry.runVoid(() -> {
            int current = attempts.incrementAndGet();
            if (current < 3) {
                throw new RetryException("Temporary failure " + current);
            }
        }).setMaxAttempts(5).setDelayMillis(0).execute();

        assertEquals(3, attempts.get(), "Void action should be called 3 times (2 fail, 1 success)");
    }

    @Test
    public void listenerIsCalled() throws Exception {
        AtomicInteger onRetryCount = new AtomicInteger(0);
        AtomicInteger onFailureCount = new AtomicInteger(0);
        AtomicInteger onSuccessCount = new AtomicInteger(0);

        RetryListener listener = new RetryListener() {
            @Override
            public void onRetry(RetryContext context) {
                onRetryCount.incrementAndGet();
            }

            @Override
            public void onFailure(RetryContext context) {
                onFailureCount.incrementAndGet();
            }

            @Override
            public void onSuccess(RetryContext context) {
                onSuccessCount.incrementAndGet();
            }
        };

        AtomicInteger attempts = new AtomicInteger(0);
        Retry.run(() -> {
            int current = attempts.incrementAndGet();
            if (current < 3) {
                throw new RuntimeException("Temporary");
            }
            return "OK";
        }).setMaxAttempts(5).setDelayMillis(0).setListener(listener).execute();

        assertEquals(3, attempts.get());
        assertEquals(2, onRetryCount.get(), "2x retries (after 1st and 2nd attempt)");
        assertEquals(0, onFailureCount.get(), "There should be no final failure.");
        assertEquals(1, onSuccessCount.get(), "onSuccess should be called once");
    }
}
