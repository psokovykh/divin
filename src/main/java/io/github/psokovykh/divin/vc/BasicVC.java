package io.github.psokovykh.divin.vc;


import io.github.psokovykh.divin.core.DataChecker;
import io.github.psokovykh.divin.core.ResponseMessage;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;

import io.github.psokovykh.divin.core.RequestMessage;

/**
 * Basic implementation of {@link ViewController}, defining only internal part of "View".
 *
 * {@link #handleResponses()} is View (since it show response to user)
 *
 * The way how Controller work is defined by subclasses.
 *
 * Organises messages handling by two queues:
 *      writes requests to {@link #outRequestsQueue}
 *      gets answer from {@link #outRequestsQueue}
 * It's not recommended to write to them directly from outside
 * of that class and its subclasses (but possible).
 *
 * It's written without active waiting (thx to {@code BlockingQueue}) and so should
 * be written all subclasses.
 */
@SuppressWarnings("WeakerAccess, unused")
public abstract class BasicVC implements ViewController {
	private static Logger logger = LoggerFactory.getLogger( BasicVC.class );


	/*============================================*/
	/*                     FIELDS                 */
	/*============================================*/

	/** Queue for outputting produced requests.
	 * That class and subclasses should only write to it */
	protected BlockingQueue<RequestMessage> outRequestsQueue;

	/** Queue for getting responses from PassiveModules.
	 * That class and subclasses should only read from it */
	protected BlockingQueue<ResponseMessage> inResponsesQueue;

	/**
	 * Purpose of that thread is to check for new responses in {@link #inResponsesQueue}
	 * and invoke user-defined {@link #handleResponse(ResponseMessage)} on them.
	 *
	 * It's created with {@link #handleResponses()} as {@code Runnable} by default.
	 */
	protected Thread responseThread = new Thread(this::handleResponses);

	/*============================================*/
	/*                  CONSTRUCTORS              */
	/*============================================*/

	/**
	 * @param outRequestsQueue See {@link #outRequestsQueue}
	 * @param inResponsesQueue See {@link #inResponsesQueue}
	 *
	 * @throws IllegalArgumentException if any of parameters are null
	 */
	@Contract("null, _ -> fail; _, null -> fail;")
	public BasicVC(
			BlockingQueue<RequestMessage> outRequestsQueue,
			BlockingQueue<ResponseMessage> inResponsesQueue
	) {
		this.setOutRequestsQueue(outRequestsQueue);
		this.setInResponsesQueue(inResponsesQueue);
	}


	/*============================================*/
	/* THREADLIKE FUNCTIONALITY (start, end, etc) */
	/*============================================*/

	/**
	 * {@inheritDoc}
	 *
	 * Child classes must call {@code super.start()} when overriding that.
	 */
	public void start() {
		this.responseThread.start();
	}

	/**
	 * {@inheritDoc}
	 *
	 * Child classes must call {@code super.interrupt()} when overriding that.
	 */
	@Override
	public void interrupt(){
		this.responseThread.interrupt();
	}

	/**
	 * {@inheritDoc}
	 *
	 * Child classes must call {@code super.join()} when overriding that.
	 */
	@Override
	public void join() throws InterruptedException{
		if(this.responseThread.isAlive()) {
			this.responseThread.join();
		}
	}


	/**
	 * {@inheritDoc}
	 *
	 * Child classes must call {@code super.setPriority()} when overriding that.
	 */
	@Override
	public void setPriority(int priority){
		//Yes, Thread performs this checks too, but we want custom message to appear
		// and our logger to be invoked
		DataChecker.checkIntInRange(
				priority, MIN_PRIORITY, MAX_PRIORITY,
				"priority", "setPriority", logger
		);
		this.responseThread.setPriority(priority);
	}

	/*============================================*/
	/*          ACTIVE MODULE FUNCTIONALITY       */
	/*============================================*/

	/**
	 * Attempts to take a response from queue, then handles it
	 * (see {@link #handleResponse(ResponseMessage) },
	 * and so on, until the thread is interrupted.
	 *
	 * Does never actively wait, relies on {@code BlockingQueue}.
	 *
	 * Returns if thread is interrupted.
	 */
	private void handleResponses() {
		try {
			while (!Thread.interrupted()) {
				//Here we can achieve thread block
				var response = this.inResponsesQueue.take();
				try {
					this.handleResponse(response);
				} catch (ErrorWhileSending err) {
					//WANNA provide for user a way to handle such errors
					//WANNA add the response itself to error
					logger.error("Something went wrong while sending a response", err);
				}
			}
		}catch(InterruptedException err){
			//It's totally fine :) However, we must terminate on that
		}
	}

	/**
	 * That method is recommended to use for produced requests, if all what
	 * user needs is to get response back into {@link #inResponsesQueue}
	 *
	 * @param targetPM String identifier of passive module
	 * @param text Text of the request. It is possibly function name + arguments.
	 * @return Correctly formed {@link RequestMessage}, who's
	 * {@link RequestMessage#answer(ResponseMessage)} writes response
	 * to {@link #inResponsesQueue}
	 *
	 * @throws IllegalArgumentException if {@code targetPM} is empty
	 * @throws IllegalArgumentException if {@code text} is null
	 */
	@NotNull @Contract("null, _ -> fail; _, null ->fail;")
	protected BasicRequest produceBasicRequest(String targetPM, String text){
		//All checks are performed in the constructor
		return new BasicRequest(targetPM, text, this.inResponsesQueue);
	}

	/*============================================*/
	/*           GETTERS AND SETTERS              */
	/*============================================*/

	/**
	 * Never returns null.
	 * User must write to it very careful, cuz malformed writing can lead to inconsistency.
	 * @return See {@link #outRequestsQueue}
	 */
	@NotNull
	public BlockingQueue<RequestMessage> getOutRequestsQueue() {
		return outRequestsQueue;
	}

	/**
	 * @param outRequestsQueue See {@link #outRequestsQueue}
	 * @throws IllegalArgumentException if parameter is null
	 */
	@Contract("null -> fail;")
	public void setOutRequestsQueue(BlockingQueue<RequestMessage> outRequestsQueue) {
		DataChecker.checkNull(
				outRequestsQueue,
				"outRequestsQueue", "setOutRequestsQueue", logger
		);
		this.outRequestsQueue = outRequestsQueue;
	}

	/**
	 * Never returns null.
	 * User must write to it very careful, cuz malformed writing can lead to inconsistency.
	 * @return See {@link #inResponsesQueue}
	 */
	@NotNull
	public BlockingQueue<ResponseMessage> getInResponsesQueue() {
		return inResponsesQueue;
	}

	/**
	 * @param inResponsesQueue See {@link #inResponsesQueue}
	 * @throws IllegalArgumentException if parameter is null
	 */
	@Contract("null -> fail;")
	public void setInResponsesQueue(BlockingQueue<ResponseMessage> inResponsesQueue) {
		DataChecker.checkNull(
				inResponsesQueue,
				"inResponsesQueue", "setInResponsesQueue", logger
		);
		this.inResponsesQueue = inResponsesQueue;
	}

	/*============================================*/
	/*ABSTRACT METHODS TO BE DEFINED BY SUBCLASSES*/
	/*============================================*/

	/**
	 * View part of application, which is defined by framework user.
	 *
	 * Describes the way, how response is sent back to end user.
	 *
	 * It is legal to block thread in that method, as long as it's able to interrupt.
	 *
	 * @param response the response to be handled
	 *
	 * @see #responseThread
	 * @see #handleResponses()
	 * @see ResponseMessage
	 *
	 * @throws ErrorWhileSending on external errors
	 * @throws InterruptedException if thread was interrupted
	 * @throws IllegalArgumentException if null is provided
	 */
	@Contract("null->fail")
	protected abstract void handleResponse(ResponseMessage response)
			throws ErrorWhileSending, InterruptedException;
}
