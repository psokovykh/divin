package io.github.psokovykh.divin.restvc;

import io.github.psokovykh.divin.vc.ListenerReadVC;
import io.github.psokovykh.divin.core.RequestMessage;
import io.github.psokovykh.divin.core.ResponseMessage;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Service;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;

import static spark.Service.ignite;

public class RestVC extends ListenerReadVC {
	private static Logger logger = LoggerFactory.getLogger( RestVC.class );

	private RequestDispatcher[] requestDispatchers;
	private HashMap<String, RequestProcessingJob> responsesCache;
	protected Thread garbageResponsesCollector;
	private int refreshRateSeconds = 2;
	protected Service http;

	/**
	 * @param outRequestsQueue The global queue, which is used by <tt>InterlayerModule</tt>,
	 *                         where requests from all ActiveModules will be stored, and from
	 *                         where they will be sent to corresponding <tt>Model</tt>
	 * @param inResponsesQueue For cases, when two instances of same <tt>ViewController</tt>
	 */
	public RestVC(
			BlockingQueue<RequestMessage> outRequestsQueue,
			BlockingQueue<ResponseMessage> inResponsesQueue,
			int port, int threadPool,
			RequestDispatcher... requestDispatchers
	) {
		super(outRequestsQueue, inResponsesQueue);
		this.requestDispatchers = requestDispatchers;
		this.garbageResponsesCollector = new Thread(this::garbageResponsesCollectorJob);
		this.responsesCache = new HashMap<>();
		this.http = ignite().port(port).threadPool(threadPool);
	}

	/**
	 *
	 */
	@Override
	public void start() {
		super.start();
		this.garbageResponsesCollector.start();
		//Unnecessary, since first http.get() call from initializeListeners implicitly calls it
		//this.http.init();
	}

	/**
	 *
	 */
	@Override
	public void interrupt() {
		super.interrupt();
		this.garbageResponsesCollector.interrupt();
		this.http.stop();
	}


	/**
	 *
	 */
	@Override
	public void join() throws InterruptedException {
		super.join();
		this.garbageResponsesCollector.join();
		this.http.awaitStop();
	}

	/**
	 *
	 */
	@Override
	public void initializeListeners(){
		for(var reqDisp : this.requestDispatchers){
			http.get(reqDisp.getUri(), (req, res)->
					processRoute(req, res, reqDisp::dispatch)
			);
		}
	}

	/**
	 *
	 * @param req
	 * @param res
	 * @param dispatchLambda
	 * @return
	 */
	private Object processRoute(
			Request req, Response res,
			LambdaDispatch dispatchLambda //FIXME it's actually not just RequestMessage
	){
		String fullURL = getFullURL(req);
		if(!responsesCache.containsKey(fullURL)){
			var reqMsg = dispatchLambda.apply(req);
			reqMsg.setReturnAddress(this.inResponsesQueue);
			reqMsg.setUri(fullURL);
			this.putToQueue(reqMsg);
			responsesCache.put(fullURL, new RequestProcessingJob());
		}else{
			var reqProcessing = responsesCache.get(fullURL);
			if(reqProcessing.isDone()){
				return getAdaptedResponseMsg(reqProcessing.getResponse());
			}
		}

		res.header("refresh", refreshRateSeconds+"; "+fullURL);
		return getProcessingStartedMsg(req);
	}

	/**
	 * User of framework might want to override that.
	 * @return
	 */
	@NotNull
	private Object getProcessingStartedMsg(Request req) {
		return "Processing your request...";//TODO
	}

	/**
	 * User of framework might want to override that.
	 * @param respMsg
	 * @return
	 */
	@Contract("null -> fail;")
	@NotNull
	private Object getAdaptedResponseMsg(ResponseMessage respMsg) {
		if(respMsg == null){
			logger.error("null passed as respMsg to getAdaptedResponseMsg");
			throw new InvalidParameterException("null passed as respMsg to getAdaptedResponseMsg");
		}
		return respMsg.toString();
	}



	private void garbageResponsesCollectorJob() {
		//TODO
	}

	@Override
	protected void handleResponse(ResponseMessage response) {
		if (!(response instanceof UriResponse)) {
			throw new Error(); //FIXME
		}
		UriResponse uriResponse = (UriResponse) response;

		if(this.responsesCache.containsKey(uriResponse.getUri())) {
			this.responsesCache.get(uriResponse.getUri())
					.setResponse(uriResponse.getActualResponse());
		}else{
			throw new Error(uriResponse.getUri()); //FIXME
		}
	}

	@NotNull
	@Contract("null->fail;")
	private static String getFullURL(Request req) {
		if(req == null){
			logger.error("null passed as req param in getFullURL");
			throw new InvalidParameterException("null passed as req param int getFullURL");
		}
		var request = req.raw();
		StringBuilder requestURL = new StringBuilder(request.getRequestURL().toString());
		String queryString = request.getQueryString();

		if (!StringUtils.isEmpty(queryString)) {
			requestURL.append('?').append(queryString);
		}
		return requestURL.toString();
	}
}
