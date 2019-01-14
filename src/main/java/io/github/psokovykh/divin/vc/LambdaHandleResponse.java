package io.github.psokovykh.divin.vc;

import io.github.psokovykh.divin.core.ResponseMessage;

/**
 * It must correspond to {@link BasicVC#handleResponse(ResponseMessage)} ()}
 */
@FunctionalInterface
public interface LambdaHandleResponse {
	void accept(ResponseMessage msg)  throws ErrorWhileSending, InterruptedException;
}
