package io.github.psokovykh.divin.restvc;

import io.github.psokovykh.divin.core.DataChecker;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;

public class DispatcherLambda implements RequestDispatcher{
	private Logger logger = LoggerFactory.getLogger( LoggerFactory.class );

	String uri;
	LambdaDispatch dispatchLambda;


	public DispatcherLambda(String uri, LambdaDispatch dispatchLambda){
		this.setUri(uri);
		this.setDispatchLambda(dispatchLambda);
	}

	@Override
	public @NotNull String getUri() {
		return this.uri;
	}

	@Override
	public @NotNull UriRequest dispatch(Request req) {
		var reqMsg = dispatchLambda.apply(req);
		return reqMsg;
	}



	private void setUri(String uri) {
		DataChecker.checkEmptyStr(
				uri,
				"uri", "setUri", logger
		);
		this.uri = uri;
	}

	private void setDispatchLambda(LambdaDispatch dispatchLambda) {
		DataChecker.checkNull(
				dispatchLambda,
				"dispatchLambda", "setDispatchLambda", logger
		);
		this.dispatchLambda = dispatchLambda;
	}
}
