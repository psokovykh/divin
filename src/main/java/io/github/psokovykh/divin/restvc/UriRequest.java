package io.github.psokovykh.divin.restvc;

import io.github.psokovykh.divin.vc.BasicRequest;
import io.github.psokovykh.divin.core.PriorityMessage;
import io.github.psokovykh.divin.core.ResponseMessage;

public class UriRequest extends BasicRequest {
	private String uri;

	/**
	 * @param text     see {@link #text}
	 * @param priority see {@link #priority}
	 * @throws IllegalArgumentException if {@code text} is null
	 * @throws IllegalArgumentException if {@code priority} is not in range
	 * {@link PriorityMessage#MIN_PRIORITY} to {@link PriorityMessage#MAX_PRIORITY}
	 * including ends.
	 */
	public UriRequest(String targetPmName, String text, int priority) {
		super(targetPmName, text, priority);
	}

	/**
	 * Defaults {@code priority} of {@link UriRequest (String, int)} to
	 * {@link PriorityMessage#NORM_PRIORITY}
	 *
	 * @param text see {@link #text}
	 * @throws IllegalArgumentException if {@code text} is null
	 */
	public UriRequest(String targetPmName, String text) {
		super(targetPmName, text);
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	@Override
	public void answer(ResponseMessage res) {
		super.answer(new UriResponse(this.uri, res));
	}
}
