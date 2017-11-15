package tech.greenfield.vertx.nuts;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.vertx.core.Handler;

public class Controller {

	protected interface RawVertxHandler extends Handler<NutsMessage> {}
	protected interface NutsHandler extends Handler<NutsMessage> {}
	
	/**
	 * Helper method for Nuts to discover subject paths
	 * @return list of fields that are subject paths
	 */
	List<RouteConfiguration> getRoutes() {
		return Stream.concat(
				Arrays.stream(getClass().getDeclaredFields()).map(f -> RouteConfiguration.wrap(this, f)),
				Arrays.stream(getClass().getDeclaredMethods()).map(f -> RouteConfiguration.wrap(this, f)))
				.filter(RouteConfiguration::isValid)
				.collect(Collectors.toList());
	}

	/**
	 * Helper method for Nuts to create the appropriate handler for the message
	 * @param field message handler exposed by this controller
	 * @return a handler that takes a message and handles it
	 */
	@SuppressWarnings("unchecked")
	Handler<NutsMessage> getHandler(Field field) {
		try {
			field.setAccessible(true);
			if (Handler.class.isAssignableFrom(field.getType()))
				return (Handler<NutsMessage>)field.get(this);
			return null;
		} catch (IllegalArgumentException | IllegalAccessException e) {
			// shouldn't happen
			throw new RuntimeException("Error accessing field " + field + ": " + e, e);
		}
	}

	/**
	 * Helper method for Nuts to mount sub-controllers
	 * @param field message handler exposed by this controller
	 * @return Controller instance if's is a sub-controller, null otherwise 
	 */
	Controller getController(Field field) {
		try {
			field.setAccessible(true);
			if (Controller.class.isAssignableFrom(field.getType()))
				return (Controller)field.get(this);
			return null;
		} catch (IllegalArgumentException | IllegalAccessException e) {
			// shouldn't happen
			throw new RuntimeException("Error accessing field " + field + ": " + e, e);
		}
	}

}
