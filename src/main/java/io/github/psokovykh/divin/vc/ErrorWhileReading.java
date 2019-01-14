package io.github.psokovykh.divin.vc;

import java.io.IOException;

/**
 * Represents external error while trying to fetch user's request
 */
@SuppressWarnings("unused")
public class ErrorWhileReading extends IOException {

	public ErrorWhileReading(String msg){
		super(msg);
	}

	public ErrorWhileReading(String msg, Exception cause){
		super(msg, cause);
	}
}
