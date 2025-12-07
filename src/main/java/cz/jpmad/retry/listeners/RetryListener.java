package cz.jpmad.retry.listeners;

import cz.jpmad.retry.RetryContext;

public interface RetryListener {

    void onRetry(RetryContext context);

    void onFailure(RetryContext context);

    void onSuccess(RetryContext context);

}
