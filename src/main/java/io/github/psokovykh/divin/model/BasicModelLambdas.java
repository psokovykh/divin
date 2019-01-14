package io.github.psokovykh.divin.model;

import io.github.psokovykh.divin.core.DataChecker;
import io.github.psokovykh.divin.core.RequestMessage;
import io.github.psokovykh.divin.core.ResponseMessage;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 *
 * Provides a way to create instance of {@link BasicModel}, without creating
 * subclass, but just by passing lambdas in ctor. That lambdas are then invoked in methods.
 */

public class BasicModelLambdas extends BasicModel {
	private static Logger logger = LoggerFactory.getLogger( BasicModelLambdas.class );

	/**
	 * Lambda to be invoked in {@link #doJob(RequestMessage)}
	 * @see #doJob(RequestMessage)
	 */
	private LambdaDoJob doJobLambda;

	/** Names, by which the PM can be recognized (e.g. ["TestPM", "tst"])
	 * @see #getNames()
	 */
	private List<String> names;

	/**
	 * @param threadPullSize number of threads to be run
	 * @param doJobLambda see {@link #doJobLambda}
	 * @param names names to be return in {@link #getNames()}
	 * @throws IllegalArgumentException if threadPullSize is not in right (see below) range
	 * @throws IllegalArgumentException if doJobLambda is null
	 * @throws IllegalArgumentException if names is null or empty
	 */
	public BasicModelLambdas(int threadPullSize,
	                         LambdaDoJob doJobLambda,
	                         String[] names
	){
		super(threadPullSize); //performs check on argument
		this.setDoJobLambda(doJobLambda);
		this.setNames(names);

	}
	//Wanna ctor with List as names param

	//TODO docs
	public BasicModelLambdas(LambdaDoJob doJobLambda, String[] names){
		this(1, doJobLambda, names);

	}
	
	@Override
	protected ResponseMessage doJob(RequestMessage req) throws InterruptedException {
		return doJobLambda.apply(req);
	}

	@Override
	public @NotNull List<String> getNames() {
		return this.names;
	}

	/**
	 * @param names see {@link #names}
	 *
	 * @throws IllegalArgumentException if param is null or empty
	 */
	private void setNames(String[] names){
		DataChecker.checkEmptyArray(
				names,
				"names", "setNames", logger
		);
		this.names = List.of(names);
	}
	//Wanna setNames with List as a param

	/**
	 * @param doJobLambda see {@link #doJobLambda}
	 *
	 * @throws IllegalArgumentException if doJobLambda is null
	 */
	private void setDoJobLambda(LambdaDoJob doJobLambda){
		DataChecker.checkNull(
				doJobLambda,
				"doJobLambda", "setDoJobLambda", logger
		);
		this.doJobLambda = doJobLambda;
	}
}
