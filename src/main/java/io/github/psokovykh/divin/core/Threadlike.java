package io.github.psokovykh.divin.core;

/**
 * Can be started, interrupted and other stuff like Thread can.
 *
 * Class declared like {@code class A extends Thread implements Threadlike} always
 * have all it's methods implemented.
 *
 * It can used for different wrappers around {@code Thread}s, to just proxy the methods to
 * one or several instances, but it is (obviously) not single approach.
 */
public interface Threadlike {
	/**The maximum priority that a {@code Threadlike} can have*/
	int MAX_PRIORITY = Thread.MAX_PRIORITY;
	/**The minimum priority that a {@code Threadlike} can have*/
	int MIN_PRIORITY = Thread.MIN_PRIORITY;
	/**The default priority that is assigned to a {@code Threadlike}*/
	int NORM_PRIORITY = Thread.NORM_PRIORITY;

	/**
	 * After call to it some work have to be started asynchronously.
	 * User must assume that it can be called only once, if not stated otherwise.
	 * @throws IllegalThreadStateException if start was already called on that object,
	 * while realisation forbids it
	 */
	void start() throws	IllegalThreadStateException;

	/**
	 * Asynchronously send signal to stop the job.
	 * Should be written in a corresponding way to {@link #start()}.
	 * Calling {@link #join()} after this must not result in infinite block.
	 */
	void interrupt();

	/**
	 * Calling thread is blocked until {@code Threadlike} is stopped.
	 * It's bad idea to write this in active-waiting way.
	 * @throws InterruptedException if calling thread is interrupted while waiting for
	 * {@code Threadlike} stop.
	 */
	void join() throws InterruptedException;

	/**
	 * Sets priority (of some kind) to the {@code Threadlike}.
	 *
	 * If a class is wrapper around {@code Thread}(s), this have to proxied into it(them).
	 * Otherwise can be noop or used for some custom stuff.
	 *
	 * @param priority the priority between {@link #MIN_PRIORITY} and {@link #MAX_PRIORITY}(including both).
	 * @throws IllegalArgumentException if {@code priority} is not
	 * in range between {@link #MIN_PRIORITY} and {@link #MAX_PRIORITY} (including both).
	 */
	void setPriority(int priority);

	/* WANNA void join(long millis) throws InterruptedException;*/

	/* WANNA void setName(String name); */

	/* WANNA boolean isAlive();*/
}
