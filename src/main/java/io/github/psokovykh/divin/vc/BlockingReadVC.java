package io.github.psokovykh.divin.vc;

import io.github.psokovykh.divin.core.RequestMessage;
import io.github.psokovykh.divin.core.ResponseMessage;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

/**
 *
 * Extends {@link BasicVC}, assuming, that it's possible to block a thread and
 * wait for user's request. Can be nicely used with {@link io.github.psokovykh.divin.util.BlockingCooldown}.
 */
@SuppressWarnings("WeakerAccess, unused")
public abstract class BlockingReadVC extends BasicVC {
	private static Logger logger = LoggerFactory.getLogger( BlockingReadVC.class );

	/*============================================*/
	/*                     FIELDS                 */
	/*============================================*/

	/**
	 * Purpose of that thread is to invoke user-defined {@link BlockingReadVC#getRequest()}
	 * and put resulting {@link RequestMessage} to {@link BasicVC#outRequestsQueue}.
	 *
	 * It's created with {@link BlockingReadVC#readRequests()} as {@code Runnable} by default.
	 */
	protected Thread readThread = new Thread(this::readRequests);


	/*============================================*/
	/*                  CONSTRUCTORS              */
	/*============================================*/

	/**
	 * @param outRequestsQueue See {@link BasicVC#outRequestsQueue}
	 * @param inResponsesQueue See {@link BasicVC#inResponsesQueue}
	 *
	 * @throws IllegalArgumentException if any of parameters are null
	 */
	@Contract("null, _ -> fail; _, null -> fail;")
	public BlockingReadVC(
			BlockingQueue<RequestMessage> outRequestsQueue,
			BlockingQueue<ResponseMessage> inResponsesQueue
	) {
		super(outRequestsQueue, inResponsesQueue);
	}

	/**
	 * Defaults parameter {@code inResponsesQueue} from
	 * {@link BlockingReadVC (BlockingQueue, BlockingQueue)}
	 * to newly created {@code PriorityBlockingQueue}
	 *
	 * @param outRequestsQueue See {@link BasicVC#outRequestsQueue}
	 * @throws IllegalArgumentException if parameter is null
	 */
	@Contract("null -> fail;")
	public BlockingReadVC(BlockingQueue<RequestMessage> outRequestsQueue) {
		this(outRequestsQueue, new PriorityBlockingQueue<>());
	}

	/*============================================*/
	/* THREADLIKE FUNCTIONALITY (start, end, etc) */
	/*============================================*/

	@Override
	public void start(){
		super.start();
		this.readThread.start();
	}

	@Override
	public void interrupt(){
		super.interrupt();
		this.readThread.interrupt();
	}

	@Override
	public void join() throws InterruptedException{
		super.join();
		if(this.readThread.isAlive()) {
			this.readThread.join();
		}
	}

	@Override
	public void setPriority(int priority){
		// Super performs check for argument and throws,
		// so we don't need to check it right here
		super.setPriority(priority);
		this.readThread.setPriority(priority);
	}


	/*============================================*/
	/*          ACTIVE MODULE FUNCTIONALITY       */
	/*============================================*/

	/**
	 *  Wait for user to provide a request, calling {@link BlockingReadVC#getRequest()},
	 *  then puts it into queue, and so on, until the thread is interrupted.
	 *
	 *  Does never actively wait, as long as {@link BlockingReadVC#getRequest()} is
	 *  implemented correctly.
	 *
	 *  Returns if thread is interrupted.
	 */
	private void readRequests() {
		try {
			while (!Thread.interrupted()) {
				try{
					//Here we can achieve thread block
					var request = this.getRequest();
					this.outRequestsQueue.offer(request);
				}catch(ErrorWhileReading err){
					//WANNA provide for user a way to handle such errors
					logger.error("Something went wrong while retrieving request", err);
				}
			}
		}catch(InterruptedException err){
			//It's totally fine :) However, we must terminate on that
		}
	}

	/*============================================*/
	/*ABSTRACT METHODS TO BE DEFINED BY SUBCLASSES*/
	/*============================================*/

	/**
	 * Controller part of application, which is defined by framework user.
	 *
	 * Describes the way, how request is transferred form user to application.
	 *
	 * It's mandatory to block thread here, if no requests are provided by user,
	 * but it must be able to interrupt.
	 *
	 * @return user's request
	 * @see BlockingReadVC#readThread
	 * @see BlockingReadVC#readRequests()
	 * @see RequestMessage
	 *
	 * @throws ErrorWhileReading on external errors
	 * @throws InterruptedException if thread was interrupted
	 */
	@NotNull
	protected abstract RequestMessage getRequest() throws ErrorWhileReading, InterruptedException;

}
