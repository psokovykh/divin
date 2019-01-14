package io.github.psokovykh.divin.core;


import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Most naive realisation of {@link PriorityMessage}. We can't use it for
 * every purpose, but it's good point to start extending.
 *
 * Note: this class has a natural ordering that is inconsistent with equals
 */
public class BasicPriorityMessage implements PriorityMessage {
	private static Logger logger = LoggerFactory.getLogger( BasicPriorityMessage.class );

	/**
	 * Priority, according to which messages are sorted. Fits within range
	 * {@link PriorityMessage#MIN_PRIORITY} to {@link PriorityMessage#MAX_PRIORITY}
	 * including ends.
	 * @see #getPriority()
	 */
	protected int priority;

	/**
	 * Text of the message
	 * @see #getText()
	 */
	protected String text;

	/**
	 * @param text see {@link #text}
	 * @param priority see {@link #priority}
	 *
	 * @throws IllegalArgumentException if {@code text} is null
	 * @throws IllegalArgumentException if {@code priority} is not in range
	 * {@link PriorityMessage#MIN_PRIORITY} to {@link PriorityMessage#MAX_PRIORITY}
	 * including ends.
	 */
	public BasicPriorityMessage(String text, int priority){
		this.setText(text);
		this.setPriority(priority);
	}

	/**
	 * Defaults {@code priority} of {@link #BasicPriorityMessage(String, int)} to
	 * {@link PriorityMessage#NORM_PRIORITY}
	 *
	 * @param text see {@link #text}
	 *
	 * @throws IllegalArgumentException if {@code text} is null
	 */
	public BasicPriorityMessage(String text){
		this(text, NORM_PRIORITY);
	}

	@Override
	public String getText() {
		return this.text;
	}

	@Override
	public int getPriority() {
		return this.priority;
	}

	@Override
	public void setPriority(int priority) {
		DataChecker.checkIntInRange(
				priority, MIN_PRIORITY, MAX_PRIORITY,
				"priority", "setPriority", logger
		);
		this.priority = priority;
	}

	/**
	 * @param text see {@link #text}
	 * @throws IllegalArgumentException if {@code text} is null
	 */
	private void setText(String text){
		DataChecker.checkNull(
				text,
				"text", "setText", logger
		);
		this.text = text;
	}

	/**
	 *  Note, that object with <b>higher</b> priority is <b>lesser</b> one.
	 *  It made so, cuz sorting is made by rule "lowest first" and I don't want
	 *  ReverseOrder to be used in all collections, cuz it's unreadable, and needs changes
	 *  in multiple places.
	 */
	@Override
	public int compareTo(@NotNull PriorityMessage o) {
		//Damn IDEA says that o is @NotNull. Hey, I still need to check to be
		//sure exception is thrown and log is logged! So:
		//noinspection ConstantConditions
		if(o == null){
			String errmsg = "Attempt to compare with null";
			logger.error(errmsg);
			//I'd want to use InvalidParameter here too, but
			//we have to follow docs of Comparable
			throw new NullPointerException(errmsg);
		}
		//See docs for that minus!
		return -(this.getPriority() - o.getPriority());
	}

	public String toString(){
		return this.getText();
	}
}
