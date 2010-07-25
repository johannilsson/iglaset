package com.markupartist.iglaset.util;

import java.util.Iterator;
import java.util.List;

public class StringUtils {
	public static String join(List<String> list, String delimiter) {
		StringBuilder builder = new StringBuilder();
		Iterator<String> iter = list.iterator();
    	while(iter.hasNext()) {
    		builder.append(iter.next());
    		if(iter.hasNext()) {
    			builder.append(delimiter);
    		}
    	}
    	
    	return builder.toString();
	}
}
