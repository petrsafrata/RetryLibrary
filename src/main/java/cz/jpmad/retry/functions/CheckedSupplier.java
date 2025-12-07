package cz.jpmad.retry.functions;

public interface CheckedSupplier<T> {

    T get() throws Exception;
}
