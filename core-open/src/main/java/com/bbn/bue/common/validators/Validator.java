package com.bbn.bue.common.validators;

import com.google.common.annotations.Beta;

/**
 * Checks if the supplied argument is valid according to some criterion.  
 * If not, throws an exception.
 * @author rgabbard
 *
 * @param <T>
 */
@Beta
public interface Validator<T> {
	
	/**
	 * Throws an exception if <code>arg</code> is not valid. Otherwise,
	 * does nothing.
	 * @param arg May not be null.
	 * @throws ValidationException If <code>arg</code> is not valid.
	 */
	public void validate(T arg) throws ValidationException;
}
