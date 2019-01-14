package io.github.psokovykh.divin.util;

@FunctionalInterface
public interface InterruptableSupplier <T> {

	T get() throws InterruptedException;

}
