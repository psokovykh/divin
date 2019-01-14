package io.github.psokovykh.divin.restvc;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import spark.Request;

public interface RequestDispatcher {

	@NotNull
	String getUri();

	@NotNull
	@Contract ("null->fail;")
	UriRequest dispatch(Request req);
}
