package io.github.psokovykh.divin.model;

import io.github.psokovykh.divin.core.DataChecker;
import io.github.psokovykh.divin.core.RequestMessage;
import io.github.psokovykh.divin.core.ResponseMessage;
import org.jetbrains.annotations.Contract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Basic implementation of {@link Model}.
 *
 * {@link #processRequests()} is kinda Model (since it does the logic)
 *
 * The way how requests are actually preformed is
 * defined by user in {@link #doJob(RequestMessage)}.
 *
 * Organises messages handling by a queue:
 *      gets requests from {@link #inRequestQueue}
 * It's not recommended to write to it directly from outside
 * of that class and its subclasses (but possible).
 *
 * Is scalable in it's natures, cuz of using multiple threads pool, see {@link #workers}.
 *
 * It's written without active waiting (thx to {@code BlockingQueue}) and so should
 * be written all subclasses.
 */
@SuppressWarnings("WeakerAccess, unused")
public abstract class BasicModel implements Model {
	private static Logger logger = LoggerFactory.getLogger( BasicModel.class );

	/*============================================*/
	/*                     FIELDS                 */
	/*============================================*/

	/** Queue for getting requests from ActiveModules.
	 * That class and subclasses should only read from it */
	protected BlockingQueue<RequestMessage> inRequestQueue;

	/**
	 * Purpose of those threads is to check for new requests in {@link #inRequestQueue}
	 * and invoke user-defined {@link #doJob(RequestMessage)} on them.
	 *
	 * It's created with {@link #processRequests()} as {@code Runnable} by default.
	 */
	List<Thread> workers;

	/*============================================*/
	/*                  CONSTRUCTORS              */
	/*============================================*/

	/**
	 * @param threadPullSize number of threads to be run
	 * @see #workers
	 *
	 * @throws IllegalArgumentException if {@code threadPullSize} is below 1
	 */
	protected BasicModel(int threadPullSize){
		inRequestQueue = new PriorityBlockingQueue<>();

		DataChecker.checkIntInRange(
				threadPullSize, 1, Integer.MAX_VALUE,
				"threadPullSize", "BasicModel", logger
		);
		workers = new LinkedList<>();
		for(int i=0; i<threadPullSize; ++i){
			workers.add(new Thread(this::processRequests));
		}
	}

	/**
	 * Defaults {@code threadPullSize} in {@link #BasicModel(int)} to 1
	 * @see #BasicModel(int)
	 */
	protected BasicModel(){
		this(1);
	}

	/*============================================*/
	/* THREADLIKE FUNCTIONALITY (start, end, etc) */
	/*============================================*/

	@Override
	public void start(){
		for (Thread worker : workers) {
			worker.start();
		}
	}

	@Override
	public void interrupt(){
		for (Thread worker : workers) {
			worker.interrupt();
		}
	}

	@Override
	public void join() throws InterruptedException{
		for (Thread worker : workers) {
			if(worker.isAlive()) {
				worker.join();
			}
		}
	}

	@Override
	public void setPriority(int priority) {
		//Yeah, threads check it by themselves, but I want custom message
		//to appear and custom exception to be thrown
		DataChecker.checkIntInRange(
				priority, MIN_PRIORITY, MAX_PRIORITY,
				"priority", "setPriority", logger
		);
		for (Thread worker : workers) {
			worker.setPriority(priority);
		}
	}


	/*============================================*/
	/*         PASSIVE MODULE FUNCTIONALITY       */
	/*============================================*/

	/**
	 * Attempts to take a request from queue, then handles it
	 * (see {@link #doJob(RequestMessage) },
	 * and so on, until the thread is interrupted.
	 *
	 * Does never actively wait, relies on {@code BlockingQueue}.
	 *
	 * Returns if thread is interrupted.
	 */
	private void processRequests() {
		try{
			while(!Thread.interrupted()){
				var req = inRequestQueue.take();
				var res = doJob(req);
				//wanna give user a way to handle ProcessingFailed
				req.answer(res);
			}
		}catch(InterruptedException err){
			//It's totally fine ;)
			//WANNA save unhandled requests
			// (needs rework of am, cuz they are stored in msg by ref now)
		}

	}

	/** @param req the request to be (somewhen) performed later */
	@Override
	@Contract("null->fail;")
	public void addJob(RequestMessage req){
		DataChecker.checkNull(
				req,
				"req", "addJob", logger
		);
		this.inRequestQueue.offer(req);
	}


	//WANNA enlargeThreadPull(int = 1)
	//WANNA truncThreadPull(int = 1)
	//WANNA setThreadPullSize(int)

	/*============================================*/
	/*ABSTRACT METHODS TO BE DEFINED BY SUBCLASSES*/
	/*============================================*/
	/**
	 * Model part of application, which is defined by framework user.
	 *
	 * Describes the way, how response is produced form request.
	 *
	 * It is legal to block thread in that method, as long as it's able to interrupt.
	 *
	 * @param req the request to be processed
	 * @return response to the request
	 *
	 * @see #workers
	 * @see #processRequests()
	 * @see ResponseMessage
	 *
	 * @throws ProcessingFailed if something went wrong
	 * @throws InterruptedException if thread was interrupted
	 * @throws IllegalArgumentException if null is provided
	 */
	@Contract("null->fail;")
	protected abstract ResponseMessage doJob(RequestMessage req)
			throws InterruptedException, ProcessingFailed;
}
