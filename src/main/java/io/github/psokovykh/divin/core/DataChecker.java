package io.github.psokovykh.divin.core;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.security.InvalidParameterException;

/**
 * Class with static methods to avoid duplicate code during checks.
 * Note, no additional check performed here (only that, which was requested). Be careful.
 */
@SuppressWarnings("WeakerAccess")
public class DataChecker {

	/**
	 * Checks input data of a method for null
	 *
	 * @param value        value to check for null
	 * @param variableName variable name to error message if fail
	 * @param methodName   caller method name to error message if fail
	 * @param logger       logger to output error message into
	 * @throws IllegalArgumentException if check fails
	 */
	public static void checkNull(
			Object value, String variableName, String methodName, Logger logger
	) throws InvalidParameterException {
		if (value == null) {
			screamAndThrow(
					"null passed to " + methodName + "() as " + variableName + ".",
					logger
			);
		}
	}

	/**
	 * Checks input string of a method for null or empty
	 *
	 * @param str          value to check for emptiness
	 * @param variableName variable name to error message if fail
	 * @param methodName   caller method name to error message if fail
	 * @param logger       logger to output error message into
	 * @throws IllegalArgumentException if check fails
	 */
	public static void checkEmptyStr(
			String str, String variableName, String methodName, Logger logger
	) throws IllegalArgumentException {
		if (StringUtils.isEmpty(str)) {
			screamAndThrow(
					"An empty string passed to " + methodName + "() as" + variableName + ".",
					logger
			);
		}
	}

	/**
	 * Checks input array of a method for null or empty
	 *
	 * @param array        value to check for emptiness
	 * @param variableName variable name to error message if fail
	 * @param methodName   caller method name to error message if fail
	 * @param logger       logger to output error message into
	 * @throws IllegalArgumentException if check fails
	 */
	public static <T> void checkEmptyArray(
			T[] array, String variableName, String methodName, Logger logger
	) throws IllegalArgumentException {
		if (array == null || array.length == 0) {
			screamAndThrow(
					"An empty array passed to " + methodName + "() as" + variableName + ".",
					logger
			);
		}
	}


	public static void checkIntInRange(
			int number, int min, int max, String variableName, String methodName, Logger logger
	) throws IllegalArgumentException{
		if(number < min || number > max){
			screamAndThrow(
					String.format("Invalid %s passed to %s(): %d"+
							" is not in range %d-%d", variableName, methodName, number, min, max),
					logger
			);
		}
	}

	/**
	 * @param errmsg error message to describe check which failed
	 * @param logger logger to output message into
	 */
	public static void screamAndThrow(String errmsg, Logger logger) {
		logger.error(errmsg);
		throw new IllegalArgumentException(errmsg);
	}
}
