package io.github.psokovykh.divin.core;

import io.github.psokovykh.divin.model.Model;
import io.github.psokovykh.divin.vc.ViewController;
import org.jetbrains.annotations.NotNull;

/** It's used to represent every users request, usually instantiated in
 * {@link ViewController}
 */
public interface RequestMessage extends PriorityMessage {

	/**
	 * Never returns null or empty string.
	 * @return name of passive module to respond to that request
	 */ @NotNull
	String getTargetPmName();

	/**
	 *
	 * Sends response to subprogram, which created that request (not to end user!).
	 * Communicating with end user in that method can lead to executing major amount
	 * of code in thread of a {@link Model},
	 * while it's totally not it's job.
	 *
	 * @param res response to be sent back to those, who created the request
	 *
	 * @throws IllegalArgumentException if {@code res} is null
	 */
	void answer(ResponseMessage res);
}
