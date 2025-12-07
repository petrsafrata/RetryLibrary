package cz.jpmad.retry.functions;

@FunctionalInterface
public interface CheckedRunnable {

    void run() throws Exception;
}
