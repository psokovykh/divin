package io.github.psokovykh.divin.util;

import java.util.function.Supplier;

public class BlockingCooldown extends Cooldown{
	public BlockingCooldown(long length){
		super(length);
	}

	public boolean tryDo(Runnable action){
		try {
			return this.tryDoWait(action);
		} catch (InterruptedException e) {
			//We must set the flag back, so it will be processed somewhere else
			Thread.currentThread().interrupt();
			return false;
		}
	}

	public boolean tryDoWait(Runnable action) throws InterruptedException {
		this.waitUntilRefreshes();
		action.run();
		return true;
	}


	public <T> T waitAndProduce(Supplier<T> action) throws InterruptedException {
		this.waitUntilRefreshes();
		return action.get();
	}

	public void waitUntilRefreshes() throws InterruptedException{
		var waitRemain = length - (System.currentTimeMillis() - lastUsage);
		waitRemain = waitRemain < 0 ? 0 : waitRemain;
		Thread.sleep(waitRemain);
		lastUsage = System.currentTimeMillis();
	}
}
