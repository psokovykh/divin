package io.github.psokovykh.divin.restvc;

import io.github.psokovykh.divin.core.ResponseMessage;

public class RequestProcessingJob {

	private boolean isDone;
	private ResponseMessage response;

	public boolean isDone() {
		return this.isDone;
	}

	public ResponseMessage getResponse() {
		return this.response;
	}

	public void setResponse(ResponseMessage res){
		//TODO checks and stuff
		this.isDone = true;
		this.response = res;
	}
}
