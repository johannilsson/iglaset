package com.markupartist.iglaset.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class ListUtils {

	/**
	 * @param <K> Key type.
	 * @param <V> Value type.
	 * @param list List of objects to map.
	 * @param method Method to call on {@code K}.
	 * @return A multimap with the objects sorted using the key from {@code method}.
	 * 
	 * Convert a list of objects to a multimap by using the return value of a
	 * method call to the objects as key.
	 * 
	 * <pre>
	 * {@code
	 * class Animal {
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
	 * }
	 * 
	 * ArrayList<Animal> animalList = new ArrayList<Animal>();
	 * animalList.add(new Animal("Hercules", "Mouse"));
	 * animalList.add(new Animal("Fido", "Dog"));
	 * animalList.add(new Animal("Brutus", "Dog"));
	 * animalList.add(new Animal("Sebastian", "Cat"));
	 * MultiHashMap<String, Animal> = ListUtils.toMultiHashMap(animalList, "getType"));
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
	 * There are some limitations to the signature of the function to call to
	 * get the key.
	 * <ul>
	 * <li>It cannot return void</li>
	 * <li>It cannot accept any parameters</li>
	 * <li>It must be public</li>
	 * <li>It must actually exist</li>
	 * </ul>
	 * 
	 * Note that this call is not type safe and exceptions thrown within it
	 * will be caught and silently ignored for ease of use. 
	 */
	@SuppressWarnings("unchecked")
	public static <K, V> MultiHashMap<K, V> toMultiHashMap(ArrayList<V> list, String method) {
		MultiHashMap<K, V> map = new MultiHashMap<K, V>();
		
		Method call;
		K key;
		for(V object : list) {
			try {
				call = object.getClass().getDeclaredMethod(method);
				key = (K) call.invoke(object);
				map.put(key, object);
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return map;
	}
}
