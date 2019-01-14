package io.github.psokovykh.divin.vc;

import io.github.psokovykh.divin.core.RequestMessage;
import io.github.psokovykh.divin.core.ResponseMessage;
import org.jetbrains.annotations.Contract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.InvalidParameterException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

public abstract class ListenerReadVC extends BasicVC {
	private static Logger logger = LoggerFactory.getLogger( ListenerReadVC.class );

	/*============================================*/
	/*                  CONSTRUCTORS              */
	/*============================================*/

	/**
	 * @param outRequestsQueue See {@link BasicVC#outRequestsQueue}
	 * @param inResponsesQueue See {@link BasicVC#inResponsesQueue}
	 *
	 * @throws InvalidParameterException if any of parameters are null
	 */
	@Contract("null, _ -> fail; _, null -> fail;")
	protected ListenerReadVC(
			BlockingQueue<RequestMessage> outRequestsQueue,
			BlockingQueue<ResponseMessage> inResponsesQueue
	) {
		super(outRequestsQueue, inResponsesQueue);
	}

	/**
	 * Defaults parameter {@code inResponsesQueue} from
	 * {@link ListenerReadVC (BlockingQueue, BlockingQueue)}
	 * to newly created {@code PriorityBlockingQueue}
	 *
	 * @param outRequestsQueue See {@link BasicVC#outRequestsQueue}
	 * @throws InvalidParameterException if parameter is null
	 */
	@Contract("null -> fail;")
	protected ListenerReadVC(BlockingQueue<RequestMessage> outRequestsQueue) {
		this(outRequestsQueue, new PriorityBlockingQueue<>());
	}

	/*============================================*/
	/* THREADLIKE FUNCTIONALITY (start, end, etc) */
	/*============================================*/

	/**
	 * {@inheritDoc}
	 *
	 * Calls user-defined {@link ListenerReadVC#initializeListeners()}.
	 */
	@Override
	public void start() {
		super.start();
		initializeListeners();
	}

	/*============================================*/
	/*          ACTIVE MODULE FUNCTIONALITY       */
	/*============================================*/

	/**
	 * @param reqMsg request to be put into queue
	 * @throws InvalidParameterException if parameter is null
	 */
	@Contract("null->fail")
	protected void putToQueue(RequestMessage reqMsg){
		if(reqMsg == null){
			var errmsg = "null passed as reqMsg to putToQueue()";
			logger.error(errmsg);
			throw new InvalidParameterException(errmsg);
		}
		outRequestsQueue.offer(reqMsg);
	}

	/*============================================*/
	/*ABSTRACT METHODS TO BE DEFINED BY SUBCLASSES*/
	/*============================================*/

	/**
	 * Controller part of application, which is defined by framework user.
	 *
	 * Defines listeners, which should create {@link ResponseMessage} and call
	 * {@link ListenerReadVC#putToQueue(RequestMessage)} on them
	 */
	protected abstract void initializeListeners();
}
