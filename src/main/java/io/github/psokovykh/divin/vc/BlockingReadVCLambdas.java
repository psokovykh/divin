package io.github.psokovykh.divin.vc;

import io.github.psokovykh.divin.core.DataChecker;
import io.github.psokovykh.divin.core.RequestMessage;
import io.github.psokovykh.divin.core.ResponseMessage;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;

/**
 *
 * Provides a way to create instance of {@link BlockingReadVC}, without creating
 * subclass, but just by passing lambdas in ctor. That lambdas are then invoked in methods.
 */
@SuppressWarnings("WeakerAccess, unused")
public class BlockingReadVCLambdas extends BlockingReadVC {
	private static Logger logger = LoggerFactory.getLogger( BlockingReadVCLambdas.class );

	/*============================================*/
	/*                     FIELDS                 */
	/*============================================*/

	/** Lambda to be invoked in {@link BlockingReadVCLambdas#handleResponse(ResponseMessage)} */
	private LambdaHandleResponse handleResponseLambda;

	/** Lambda to be invoked in {@link BlockingReadVCLambdas#getRequest()} */
	private LambdaGetRequest getRequestLambda;

	/*============================================*/
	/*                  CONSTRUCTORS              */
	/*============================================*/

	/**
	 * Calls {@link BlockingReadVC#BlockingReadVC(BlockingQueue, BlockingQueue)} as {@code super(...)}.
	 *
	 * @param inResponsesQueue See {@link BasicVC#inResponsesQueue}
	 * @param outRequestsQueue See {@link BasicVC#outRequestsQueue}
	 *
	 * @param getRequestLambda Lambda to be invoked in
	 * {@link BlockingReadVCLambdas#handleResponse(ResponseMessage)}
	 *
	 * @param handleResponseLambda Lambda to be invoked in
	 * {@link BlockingReadVCLambdas#getRequest()}
	 *
	 * @throws IllegalArgumentException if any of parameters are null
	 */
	@Contract("null, _, _, _-> fail; _, null, _, _-> fail;" +
			  "_, _, null, _-> fail; _, _, _, null->fail;")
	public BlockingReadVCLambdas(
			BlockingQueue<RequestMessage> outRequestsQueue,
			BlockingQueue<ResponseMessage> inResponsesQueue,
			LambdaGetRequest getRequestLambda,
			LambdaHandleResponse handleResponseLambda
	) {
		super(outRequestsQueue, inResponsesQueue);
		this.setHandleResponseLambda(handleResponseLambda);
		this.setGetRequestLambda(getRequestLambda);
	}

	/**
	 * Calls {@link BlockingReadVC#BlockingReadVC(BlockingQueue)} as {@code super(...)}.
	 *
	 * @param outRequestsQueue See {@link BasicVC#outRequestsQueue}
	 *
	 * @param getRequestLambda Lambda to be invoked in
	 * {@link BlockingReadVCLambdas#handleResponse(ResponseMessage)}
	 *
	 * @param handleResponseLambda Lambda to be invoked in
	 * {@link BlockingReadVCLambdas#getRequest()}
	 *
	 * @throws IllegalArgumentException if parameter is null
	 */
	public BlockingReadVCLambdas(
			BlockingQueue<RequestMessage> outRequestsQueue,
			LambdaGetRequest getRequestLambda,
			LambdaHandleResponse handleResponseLambda
	) {
		super(outRequestsQueue);
		this.setHandleResponseLambda(handleResponseLambda);
		this.setGetRequestLambda(getRequestLambda);
	}

	/*============================================*/
	/*           GETTERS AND SETTERS              */
	/*============================================*/

	/**
	 * Never returns null.
	 * @return See {@link BlockingReadVCLambdas#handleResponseLambda}
	 */
	public LambdaHandleResponse getHandleResponseLambda() {
		return handleResponseLambda;
	}

	/**
	 * @param handleResponseLambda See {@link BlockingReadVCLambdas#handleResponseLambda}
	 * @throws IllegalArgumentException if null passed
	 */
	@Contract("null->fail")
	public void setHandleResponseLambda(LambdaHandleResponse handleResponseLambda) {
		DataChecker.checkNull(
				handleResponseLambda,
				"handleResponseLambda", "setHandleResponseLambda", logger
		);
		this.handleResponseLambda = handleResponseLambda;
	}

	/**
	 * Never returns null.
	 * @return See {@link BlockingReadVCLambdas#handleResponseLambda}
	 */
	public LambdaGetRequest getGetRequestLambda() {
		return getRequestLambda;
	}

	/**
	 * @param getRequestLambda See {@link BlockingReadVCLambdas#handleResponseLambda}
	 * @throws IllegalArgumentException if null passed
	 */
	@Contract("null->fail")
	public void setGetRequestLambda(LambdaGetRequest getRequestLambda) {
		DataChecker.checkNull(
				getRequestLambda,
				"getRequestLambda", "setGetRequestLambda", logger
		);
		this.getRequestLambda = getRequestLambda;
	}

	/*============================================*/
	/*          ACTIVE MODULE FUNCTIONALITY       */
	/*============================================*/

	/** Invokes corresponding lambda */
	@Override @NotNull
	protected RequestMessage getRequest() throws ErrorWhileReading, InterruptedException {
		var request = getRequestLambda.run();
		request.setReturnAddress(this.inResponsesQueue);
		return request;
	}

	/** Invokes corresponding lambda, fulfills contract "null-&gt;fail" */
	@Override @Contract("null->fail")
	protected void handleResponse(ResponseMessage response)
	throws ErrorWhileSending, InterruptedException
	{
		DataChecker.checkNull(
				response,
				"response", "handleResponse", logger
		);
		this.handleResponseLambda.accept(response);
	}
}
