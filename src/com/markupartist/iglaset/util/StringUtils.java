package com.markupartist.iglaset.util;

import java.util.Iterator;
import java.util.List;

public class StringUtils {
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
