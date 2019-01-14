package io.github.psokovykh.divin.vc;

import java.io.IOException;

/**
 * Represents external error while trying to show response to a user
 */
@SuppressWarnings("unused")
public class ErrorWhileSending extends IOException {

	public ErrorWhileSending(String msg){
		super(msg);
	}

	public ErrorWhileSending(String msg, Exception cause){
		super(msg, cause);
	}
}
