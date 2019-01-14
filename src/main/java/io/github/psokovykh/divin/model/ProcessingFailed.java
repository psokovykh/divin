package io.github.psokovykh.divin.model;

/**
 * For situation, when something went wrong while producing
 * {@link io.github.psokovykh.divin.core.ResponseMessage} out of
 * {@link io.github.psokovykh.divin.core.RequestMessage}
 */
public class ProcessingFailed extends RuntimeException {
	public ProcessingFailed(String msg){
		super(msg);
	}
}
