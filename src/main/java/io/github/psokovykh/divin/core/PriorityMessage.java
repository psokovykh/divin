package io.github.psokovykh.divin.core;

/**
 * Used for communications between modules.
 */
public interface PriorityMessage extends Comparable<PriorityMessage>{
	/**The maximum priority that a {@code PriorityMessage} can have*/
	int MAX_PRIORITY = Thread.MAX_PRIORITY*10+Thread.MAX_PRIORITY;
	/**The minimum priority that a {@code PriorityMessage} can have*/
	int MIN_PRIORITY = Thread.MIN_PRIORITY*10+Thread.MIN_PRIORITY;
	/**The default priority that is assigned to a {@code PriorityMessage}*/
	int NORM_PRIORITY = Thread.NORM_PRIORITY*10+Thread.NORM_PRIORITY;

	/**Returns priority of the message*/
	int getPriority();

	/**
	 * Set priority to a message, according to which they must be sorted.
	 *
	 * Priority constants are written in that way, to easily accumulated priority of module
	 * with priority of message itself (e.g. msg.setPriority(this.getPriority()*10+NORM_PRIORITY)
	 *
	 * @param prior the priority itself, must be in range between
	 * {@link #MIN_PRIORITY} and {@link #MAX_PRIORITY}
	 * @throws IllegalArgumentException if {@code prior} is not in range between
	 * {@link #MIN_PRIORITY} and {@link #MAX_PRIORITY}
	 */
	void setPriority(int prior);

	/**Returns data, that had to be sent using message in form of a string*/
	String getText();
}
