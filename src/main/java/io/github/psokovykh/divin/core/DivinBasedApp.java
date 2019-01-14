package io.github.psokovykh.divin.core;

import io.github.psokovykh.divin.model.Model;
import io.github.psokovykh.divin.vc.ViewController;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;


/**
 * Recommended superclass of all divin-based applications.
 *
 * It hides all the divin, user have just to provide {@link ViewController}s,
 * {@link Model}s, (optionally) write his own logic, and then just call {@code run()}
 *
 * It's intentionally not implementing {@link Threadlike}, cuz there have to be very few
 * instances of that in app, and so you need no polymorphism to handle them. If you need,
 * that's probably in cuz of bad design.
 */
@SuppressWarnings("WeakerAccess")
public abstract class DivinBasedApp implements Runnable{
	private static Logger logger = LoggerFactory.getLogger( DivinBasedApp.class );

	/*============================================*/
	/*                     FIELDS                 */
	/*============================================*/

	/** Number of {@link InterlayerModule}s running now. */
	private int interlayerPoolSize;

	/**
	 * The queue, containing all requests, which must be transferred
	 * from an {@link ViewController} to a {@link Model}
	 */
	protected BlockingQueue<RequestMessage> commonRequestsQueue;

	/** List of currently running {@link ViewController}s*/
	protected List<ViewController> vcs;

	/** List of currently running {@link Model}s*/
	protected List<Model> models;

	/** List of currently running {@link InterlayerModule}s*/
	protected List<InterlayerModule> controllers;

	/** = {@link #vcs} + {@link #models} + {@link #controllers}*/
	protected List<Threadlike> allTheModules;

	/*============================================*/
	/*                  CONSTRUCTORS              */
	/*============================================*/

	/**
	 * @param interlayerPoolSize number of {@link InterlayerModule}s to be started
	 * @throws IllegalArgumentException if interlayerPoolSize is zero or negative
	 */
	public DivinBasedApp (int interlayerPoolSize){
		this.setInterlayerPoolSize(interlayerPoolSize);
	}

	/** Defaults {@link #DivinBasedApp(int)} to 1. */
	public DivinBasedApp(){
		this(1);
	}

	/*============================================*/
	/*               MAIN FUNCTIONALITY           */
	/*============================================*/

	/**
	 * That method must be called to start the whole infrastructure.
	 *
	 * It consists of main loop, which is dependent, however, on user-defined methods.
	 */
	@Override
	public void run() {
		boolean restartNeeded = true;
		while(restartNeeded){
			this.initModules();
			this.startModules();

			restartNeeded = this.doMainLoop();
			
			this.terminateModules();
			try {
				this.awaitModulesStop();
			} catch (InterruptedException e) {
				logger.debug("Thread interrupted while awaiting stop", e);
			}
		}
	}

	/**
	 * (Re)Creates objects of all modules
	 *
	 * Retrieves {@link ViewController}s and {@link Model}s by user-defined functions,
	 * creates as many {@link InterlayerModule}s, as big {@link #interlayerPoolSize} was provided.
	 *
	 * Generates {@link #allTheModules} for beauty.
	 */
	protected void initModules(){
		this.commonRequestsQueue = new PriorityBlockingQueue<>();
		this.vcs = this.getViewControllers(commonRequestsQueue);
		this.models = this.getModels();
		this.controllers = new LinkedList<>();
		for(int i = 0; i < this.interlayerPoolSize; ++i){
			this.controllers.add(new InterlayerModule(models, commonRequestsQueue));
		}

		//Pack them all to one list, for easier control (see in below methods)
		this.allTheModules = new LinkedList<>();
		this.allTheModules.addAll(vcs);
		this.allTheModules.addAll(models);
		this.allTheModules.addAll(controllers);
	}


	/** Calls {@link Threadlike#start()} for each module.*/
	private void startModules() {
		allTheModules.forEach(Threadlike::start);
	}

	/**Calls {@link Threadlike#interrupt()} for each module.*/
	private void terminateModules() {
		allTheModules.forEach(Threadlike::interrupt);
	}

	/**Calls {@link Threadlike#join()} for each module.*/
	private void awaitModulesStop() throws InterruptedException{
		//We can't use foreach here, cuz of exception
		for (var allTheModule : allTheModules) {
			allTheModule.join();
		}
	}

	/*============================================*/
	/*           GETTERS AND SETTERS              */
	/*============================================*/

	/**
	 * @param interlayerPoolSize see {@link #interlayerPoolSize}
	 *
	 * @throws IllegalArgumentException if {@code interlayerPoolSize} is below 1
	 */
	private void setInterlayerPoolSize(int interlayerPoolSize){
		DataChecker.checkIntInRange(
				interlayerPoolSize, 1, Integer.MAX_VALUE,
				"interlayerPoolSize", "DivinBasedApp", logger
		);
		this.interlayerPoolSize = interlayerPoolSize;
	}

	/*============================================*/
	/*ABSTRACT METHODS TO BE DEFINED BY SUBCLASSES*/
	/*============================================*/

	/**
	 * That method must be defined by the user of framework.
	 *
	 * @param outQ common request bus, which is needed to instantiate {@link ViewController}s
	 * @return list of {@link ViewController}s  to be started
	 *
	 * @throws IllegalArgumentException if null is passed
	 */
	@NotNull @Contract("null -> fail;")
	protected abstract List<ViewController> getViewControllers(BlockingQueue<RequestMessage> outQ);

	/**
	 * That method must be defined by the user of framework.
	 *
	 * @return list of {@link Model}s to be started
	 */
	@NotNull
	protected abstract List<Model> getModels();

	/**
	 * Main logic of user's application. When this function returns, framework
	 * terminates everything, wait till they all die, and then restarts
	 * whole thing, if returned value is true.
	 *
	 * @return {@code true} if <b>restart</b> of program is needed,
	 * and <b>false</b> if shutdown
	 */
	//boolean is awful outside of on-off situations.
	// WANNA enum of end-states
	protected abstract boolean doMainLoop();
}
