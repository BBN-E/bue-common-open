package com.bbn.bue.common.converters;

import com.google.common.annotations.Beta;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Decodes "true" and "false" values and disallows others.
 * @author rgabbard
 *
 */
@Beta
public class StrictStringToBoolean implements StringConverter<Boolean> {

	/**
	 * If <code>s</code> is "true" returns <code>true</code>; if "false",
	 * returns <code>false</code>; otherwise, throws a ValidationException.
	 * @param s May not be null.
	 */
	@Override
	public Boolean decode(final String s) {
		checkNotNull(s);
		if (s.equals("true")) {
			return Boolean.TRUE;
		} else if (s.equals("false")) {
			return Boolean.FALSE;
		} else {
			throw new ConversionException(String.format("Strictly-converted booleans must be either 'true' or 'false', not %s", s));
		}
	}
}
