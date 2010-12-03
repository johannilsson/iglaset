package com.markupartist.iglaset.util;

/**
 * @author marco
 *
 * Interface for implementing keys in objects. This can, for example, be used
 * when converting a list of objects to a map in {@link ListUtils}. Note that
 * any given class can only have one key.
 *
 * @param <K> Type of key returned by {@code getKey}.
 */
public interface HasKey<K> {

	public K getKey();
}
