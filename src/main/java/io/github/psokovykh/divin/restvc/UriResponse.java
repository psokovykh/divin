package io.github.psokovykh.divin.restvc;

import io.github.psokovykh.divin.core.PriorityMessage;
import io.github.psokovykh.divin.core.ResponseMessage;
import org.jetbrains.annotations.NotNull;

public class UriResponse implements ResponseMessage {

	private ResponseMessage actualResponse;
	private String uri;

	//TODO all the checks and stuff
	public UriResponse(String uri, ResponseMessage res) {
		this.uri = uri;
		this.actualResponse = res;
	}

	@Override
	public int getPriority() {
		return actualResponse.getPriority();
	}

	@Override
	public void setPriority(int prior) {
		actualResponse.setPriority(prior);
	}

	@Override
	public String getText() {
		return actualResponse.getText();
	}

	@Override
	public int compareTo(@NotNull PriorityMessage o) {
		return actualResponse.compareTo(o);
	}

	public String getUri(){
		return this.uri;
	}

	public ResponseMessage getActualResponse() {
		return this.actualResponse;
	}
}
