package io.github.psokovykh.divin.core;

import io.github.psokovykh.divin.model.Model;
import io.github.psokovykh.divin.vc.ViewController;
import org.jetbrains.annotations.Contract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;

/**
 *  Transfers requests from queue (filled by {@link ViewController}s) to {@link Model}s
 */
@SuppressWarnings("WeakerAccess")
public class InterlayerModule extends Thread implements Threadlike{
	private static Logger logger = LoggerFactory.getLogger( InterlayerModule.class );

	/**The queue, containing all requests, which must be transferred to a {@link Model} */
	private BlockingQueue<RequestMessage> inRequestsQueue;

	/**Tree for fast searching {@link Model} by its name*/
	private TreeMap<String, Model> models = new TreeMap<>();

	/**
	 * @param models passive modules, to which we will transfer requests
	 * @param inRequestsQueue queue with requests to transfer
	 *
	 * @throws IllegalArgumentException if any of parameters are null
	 */
	@Contract("null,_ -> fail; _, null -> fail;")
	protected InterlayerModule(
		List<Model> models,
		BlockingQueue<RequestMessage> inRequestsQueue
	){
		this.inRequestsQueue = inRequestsQueue;
		for(var pm : models){
			this.registerModel(pm);
		}
	}

	/** Get request, find {@link Model} by name, give first to second. Easy! */
	public void run(){
		try {
			while (!Thread.interrupted()) {
				RequestMessage msg = inRequestsQueue.take();
				//TreeMap.get() returns null if the map contains no mapping for the key
				Model suitablePM = models.get(msg.getTargetPmName());
				if(suitablePM == null){
					logger.debug("Non-existent module call: {}", msg.getTargetPmName());
					continue;
				}
				suitablePM.addJob(msg);
			}
		}catch(InterruptedException err){
			//It's totally fine :) However we must terminate
		}
	}

	/**
	 * @param pm PM to be added to {@link #models} under different names
	 *
	 * @throws IllegalArgumentException if parameter is null
	 */
	@Contract("null -> fail")
	private void registerModel(Model pm) {
		DataChecker.checkNull(
				pm,
				"pm", "registerModel", logger
		);

		var names = pm.getNames();
		this.models.put(pm.getClass().getName(), pm);
		for(var name : names){
			this.models.put(name, pm);
		}
	}
}
