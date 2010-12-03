package com.markupartist.iglaset.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class ListUtils {

	/**
	 * @param <K> Key type.
	 * @param <V> Value type.
	 * @param list List of objects to map.
	 * @return A multimap with the objects sorted using the key from {@code method}.
	 * 
	 * Convert a list of objects to a multimap using the objects' {@code getKey}
	 * return value as key. This means that the class being sorted must implement
	 * the {@link HasKey} interface.
	 * 
	 * <pre>
	 * {@code
	 * class Animal implements HasKey<String> {
	 *    private String name;
	 *    private String type;
	 *    
	 *    public Animal(String name, String type) {
	 *       this.name = name;
	 *       this.type = type;
	 *    }
	 *    public String getName() {
	 *       return name;
	 *    }
	 *    public String getType() {
	 *       return type;
	 *    }
	 *    public String getKey() {
	 *    	 return this.type;
	 *    }
	 * }
	 * 
	 * ArrayList<Animal> animalList = new ArrayList<Animal>();
	 * animalList.add(new Animal("Hercules", "Mouse"));
	 * animalList.add(new Animal("Fido", "Dog"));
	 * animalList.add(new Animal("Brutus", "Dog"));
	 * animalList.add(new Animal("Sebastian", "Cat"));
	 * MultiHashMap<String, Animal> = ListUtils.toMultiHashMap(animalList));
	 * }
	 * </pre>
	 * 
	 * This will give you the following map:
	 * <pre>
	 * {@code
	 * * Cat
	 *   - Sebastian object
	 * * Dog
	 *   - Fido object
	 *   - Brutus object
	 * * Mous
	 *   - Hercules object
	 * }
	 * </pre>
	 * 
	 * Note that this call is not type safe and exceptions thrown within it
	 * will be caught and silently ignored for ease of use. 
	 */
	public static <K, V extends HasKey<K>> MultiHashMap<K, V> toMultiHashMap(ArrayList<V> list) {
		MultiHashMap<K, V> map = new MultiHashMap<K, V>();
		
		for(V object : list) {
			map.put(object.getKey(), object);
		}
		
		return map;
	}
}
