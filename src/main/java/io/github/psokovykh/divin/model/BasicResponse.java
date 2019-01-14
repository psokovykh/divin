package io.github.psokovykh.divin.model;

import io.github.psokovykh.divin.core.BasicPriorityMessage;
import io.github.psokovykh.divin.core.ResponseMessage;

/**
 * Note: this class has a natural ordering that is inconsistent with equals
 */
public class BasicResponse extends BasicPriorityMessage implements ResponseMessage {

	public BasicResponse(String text, int priority) {
		super(text, priority);
	}

	public BasicResponse(String text) {
		super(text);
	}
}
