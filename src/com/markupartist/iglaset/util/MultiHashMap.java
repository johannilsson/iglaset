package com.markupartist.iglaset.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author marco
 *
 * Poor man's version of a Multimap. 
 *
 * @param <K> Key type
 * @param <V> Value type
 */
public class MultiHashMap<K, V> extends HashMap<K, List<V>>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public List<V> putItem(K key, V value) {
		List<V> list = get(key);
		if(list == null) {
			list = new ArrayList<V>();
			put(key, list);
		}
		
		list.add(value);
		return list;
	}
}
