package io.github.psokovykh.divin.vc;

import io.github.psokovykh.divin.core.BasicPriorityMessage;
import io.github.psokovykh.divin.core.DataChecker;
import io.github.psokovykh.divin.core.RequestMessage;
import io.github.psokovykh.divin.core.ResponseMessage;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.BlockingQueue;

/**
 * Default realisation of {@link RequestMessage}.
 *
 * Stores reference to queue of {@link ViewController}, which produced the message.
 * {@link #answer(ResponseMessage)} puts response to that queue directly.
 *
 * Note: this class has a natural ordering that is inconsistent with equals
 */ //wanna ability of serialisation, reference is messing things up, heh
@SuppressWarnings("WeakerAccess")
public class BasicRequest extends BasicPriorityMessage implements RequestMessage {
	private static Logger logger = LoggerFactory.getLogger( BasicRequest.class );

	/**Queue where answer to the request will be put*/
	protected BlockingQueue<ResponseMessage> returnAddress;

	/**
	 * Name of passive module to respond to that request
	 * @see #getTargetPmName()
	 */
	protected String targetPmName;

	/**
	 *
	 * @param targetPmName see {@link #targetPmName}
	 * @param text text of the request (arguments to PM)
	 * @param returnAddress queue for putting answer into (see {@link #returnAddress})
	 * @param priority priority of the message
	 *
	 * @throws IllegalArgumentException if {@code targetPM} is empty
	 * @throws IllegalArgumentException if {@code text} is null
	 * @throws IllegalArgumentException if {@code returnAddress} is empty
	 * @throws IllegalArgumentException if {@code priority} is not in right (see below) range
	 *
	 * @see BasicPriorityMessage#BasicPriorityMessage(String, int)
	 */
	public BasicRequest(
			String targetPmName, String text,
			BlockingQueue<ResponseMessage> returnAddress, int priority
	) {
		super(text, priority);

		this.setTargetPM(targetPmName);
		this.setReturnAddress(returnAddress);
	}

	/**
	 * Defaults param {@code priority} in {@link #BasicRequest(String, String, BlockingQueue, int)}
	 * to {@link io.github.psokovykh.divin.core.PriorityMessage#NORM_PRIORITY}
	 *
	 * @param targetPM see {@link #targetPmName}
	 * @param text text of the request (arguments to PM)
	 * @param returnAddress queue for putting answer into (see {@link #returnAddress})
	 *
	 * @throws IllegalArgumentException if {@code targetPM} is empty
	 * @throws IllegalArgumentException if {@code text} is null
	 * @throws IllegalArgumentException if {@code returnAddress} is empty
	 *
	 * @see #BasicRequest(String, String, BlockingQueue, int)
	 */
	public BasicRequest(
			String targetPM, String text, BlockingQueue<ResponseMessage> returnAddress
	) {
		this(targetPM, text, returnAddress, NORM_PRIORITY);
	}

	/**
	 * Defaults param {@code returnAddress} in {@link #BasicRequest(String, String, BlockingQueue)}
	 * to {@code null}. Have to be used with {@link BlockingReadVCLambdas} or similar method, which
	 * manually calls {@link #setReturnAddress(BlockingQueue)}.
	 *
	 * If talking fairly, it does not "default" parameter, since it would cause
	 * exception to be thrown. It just does same thing.
	 *
	 * @param targetPM see {@link #targetPmName}
	 * @param text text of the request (arguments to PM)
	 *
	 * @throws IllegalArgumentException if {@code targetPM} is empty
	 * @throws IllegalArgumentException if {@code text} is null
	 *
	 * @see #BasicRequest(String, String, BlockingQueue, int)
	 */
	public BasicRequest(String targetPM, String text) {
		super(text, NORM_PRIORITY);
		this.setTargetPM(targetPM);
	}

	/**
	 * Defaults param {@code returnAddress} in {@link #BasicRequest(String, String, BlockingQueue, int)}
	 * to {@code null}. Have to be used with {@link BlockingReadVCLambdas} or similar method, which
	 * manually calls {@link #setReturnAddress(BlockingQueue)}.
	 *
	 * If talking fairly, it does not "default" parameter, since it would cause
	 * exception to be thrown. It just does same thing.
	 *
	 * @param targetPM see {@link #targetPmName}
	 * @param text text of the request (arguments to PM)
	 *
	 * @throws IllegalArgumentException if {@code targetPM} is empty
	 * @throws IllegalArgumentException if {@code text} is null
	 * @throws IllegalArgumentException if {@code priority} is not in right (see below) range
	 *
	 * @see BasicPriorityMessage#BasicPriorityMessage(String, int)
	 * @see #BasicRequest(String, String, BlockingQueue, int)
	 */
	public BasicRequest(String targetPM, String text, int priority) {
		super(text, priority);
		this.setTargetPM(targetPM);
	}

	@NotNull
	@Override
	public String getTargetPmName() {
		return targetPmName;
	}

	@Override
	public void answer(ResponseMessage res) {
		DataChecker.checkNull(
				res,
				"res", "answer", logger
		);
		this.returnAddress.offer(res);
	}

	/**
	 * @param returnAddress see {@link #returnAddress}
	 *
	 * @throws IllegalArgumentException if {@code returnAddress} is null
	 */
	public void setReturnAddress(BlockingQueue<ResponseMessage> returnAddress){
		DataChecker.checkNull(
				returnAddress,
				"returnAddress", "BasicRequest", logger
		);
		this.returnAddress = returnAddress;
	}

	/**
	 * @param targetPmName see {@link #targetPmName}
	 *
	 * @throws IllegalArgumentException if {@code targetPmName} is null or empty
	 */
	private void setTargetPM(String targetPmName){
		DataChecker.checkEmptyStr(
				targetPmName,
				"targetPM", "setTargetPM", logger
		);
		this.targetPmName = targetPmName;
	}
}
