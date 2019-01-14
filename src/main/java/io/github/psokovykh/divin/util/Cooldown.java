package io.github.psokovykh.divin.util;

public class Cooldown {
	protected long length;
	protected long lastUsage;

	public Cooldown(long length){
		this.length = length;
		this.lastUsage = 0;
	}

	public boolean tryDo(Runnable action){
		if(System.currentTimeMillis() - lastUsage < length){
			return false;
		}

		lastUsage = System.currentTimeMillis();
		action.run();
		return true;
	}
}
