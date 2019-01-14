package io.github.psokovykh.divin.model;

import io.github.psokovykh.divin.core.RequestMessage;
import io.github.psokovykh.divin.core.Threadlike;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Subclasses of that interface represent simple Model.
 * They run in separate Thread, so we want them to have Threadlike capabilities
 */
public interface Model extends Threadlike {

	/**
	 * Tells the module, that it have to perform some actions due to request.
	 * @param req the request to be performed later
	 *
	 * @throws IllegalArgumentException if null is passed
	 */
	@Contract("null->fail;")
	void addJob(RequestMessage req);

	/**
	 * Must not be empty nor null
	 *
	 * @return list of names, by which the module can be called. Those names assumed
	 * to be be unique over all names of all Model of applications (otherwise
	 * behavior is undefined)
	 */
	@NotNull
	List<String> getNames();
}
