package io.github.psokovykh.divin.model;

import io.github.psokovykh.divin.core.RequestMessage;
import io.github.psokovykh.divin.core.ResponseMessage;

/**
 * It must correspond to {@link BasicModel#doJob(RequestMessage)}
 */
@FunctionalInterface
public interface LambdaDoJob {
	ResponseMessage apply(RequestMessage arg) throws InterruptedException, ProcessingFailed;
}
