package io.github.psokovykh.divin.restvc;

import spark.Request;

import java.util.function.Function;

@FunctionalInterface
public interface LambdaDispatch extends Function<Request, UriRequest> {
}
