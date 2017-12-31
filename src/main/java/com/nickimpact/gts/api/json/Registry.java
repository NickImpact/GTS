package com.nickimpact.gts.api.json;

import com.google.common.collect.Maps;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * This class serves as an in-memory storage facility for class registration in call for
 * GSON serialization along with deserialization. To register a class, we must first
 * specify the type of the class, then follow up with a Class object.
 *
 * <p>Example: We have an abstracted Car class. Inheriting these classes could be Lexus and Honda.
 * We want this registry to know of these classes. So, we would perhaps say the Lexus typing is
 * "lexus", and then the class representation would be Lexus.class. This allows us to query for
 * the "lexus" typing, and have the matching class be returned.</p>
 *
 * @author NickImpact
 */
public class Registry<E> {
	/** The registry mapping holding a class to its typing */
	private final Map<String, Class<? extends E>> typings = Maps.newHashMap();

	/**
	 * Registers a class to the registry. If we are missing the {@link Typing} annotation,
	 * we will throw an exception.
	 *
	 * @param clazz The class being added to the registry
	 * @throws Exception Thrown when the target class is missing its typing annotation
	 */
	public void register(Class<? extends E> clazz) throws Exception {
		if(!clazz.isAnnotationPresent(Typing.class))
			throw new Exception("Missing typing annotation, class registration cancelled");

		this.register(clazz.getAnnotation(Typing.class).value(), clazz);
	}

	/**
	 * Registers a class to the registry.
	 *
	 * @param typing The typing of the class
	 * @param clazz The class being added to the registry
	 * @throws Exception Thrown during any invalid registry attempt
	 */
	public void register(String typing, Class<? extends E> clazz) throws Exception {
		if(typings.containsKey(typing))
			throw new Exception("Identical type found, ignoring input...");

		typings.put(typing, clazz);
	}

	/**
	 * Fetches the class in the registry based on the typing id.
	 *
	 * @param id The typing id of a registered class
	 * @return The class in the registry matching the typing, if any.
	 */
	@Nullable public Class<? extends E> get(String id) {
		return typings.get(id);
	}

	public Map<String, Class<? extends E>> getTypings() {
		return this.typings;
	}
}
