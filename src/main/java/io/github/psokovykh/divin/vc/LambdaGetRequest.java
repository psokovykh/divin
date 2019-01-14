package io.github.psokovykh.divin.vc;

/**
 * It must correspond to {@link BlockingReadVC#getRequest()}
 */
@FunctionalInterface
public interface LambdaGetRequest {
	BasicRequest run()  throws ErrorWhileReading, InterruptedException;
}
