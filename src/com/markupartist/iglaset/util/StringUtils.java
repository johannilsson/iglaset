package com.markupartist.iglaset.util;

/**
 * @author marco
 * Utility class for string manipulation.
 */
public class StringUtils {
	
	/**
	 * Concatenate a list of objects into a delimiter separated string. The objects must be
	 * convertible to String.
	 * @param list List to concatenate.
	 * @param delimiter Optional delimiter
	 * @return Concatenated string containing the String representations of the objects in list.
	 */
	public static String join(Object[] list, String delimiter) {
		StringBuilder builder = new StringBuilder();
		for(int i=0; i<list.length; ++i) {
			builder.append(list[i]);
			if(i < (list.length -1)) {
				builder.append(delimiter);
			}
		}

    	return builder.toString();
	}
}
