package com.markupartist.iglaset.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author marco
 *
 * Poor man's version of a Multimap. This allows you to store multiple values
 * per key.
 *
 * @param <K> Key type
 * @param <V> Value type
 */
public class MultiHashMap<K, V> {

	private HashMap<K, List<V>> map = new HashMap<K, List<V>>();
	private int totalSize = 0;
	
	public List<V> put(K key, V value) {
		List<V> list = map.get(key);
		if(list == null) {
			list = new ArrayList<V>();
			map.put(key, list);
		}
		
		list.add(value);
		totalSize++;
		return list;
	}
	
	/**
	 * Returns the number of elements in the map. Note that this is the number
	 * of values and not the number of keys.
	 * @return the number of elements in the map.
	 */
	public int size() {
		return totalSize;
	}
	
	public List<V> get(K key) {
		return map.get(key);
	}
	
	public Set<Map.Entry<K, List<V>>> entrySet() {
		return map.entrySet();
	}
}
