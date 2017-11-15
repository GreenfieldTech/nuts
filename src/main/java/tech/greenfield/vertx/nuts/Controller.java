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
	 * Helper method for {@link Router} to discover routing endpoints
	 * @return list of fields that are routing endpoints
	 */
	List<RouteConfiguration> getRoutes() {
		return Stream.concat(
				Arrays.stream(getClass().getDeclaredFields()).map(f -> RouteConfiguration.wrap(this, f)),
				Arrays.stream(getClass().getDeclaredMethods()).map(f -> RouteConfiguration.wrap(this, f)))
				.filter(RouteConfiguration::isValid)
				.collect(Collectors.toList());
	}

	/**
	 * Helper method for {@link Router} to create the appropriate request
	 * handler for Vert.X
	 * @param field routing endpoint handler exposed by this controller
	 * @return a handler that takes a Vert.x original routing context and
	 *  wraps it in a local request context before delegating to the routing endpoint
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
	 * Helper method for {@link Router} to mount sub-controllers
	 * @param field routing endpoint exposed by this controller
	 * @return Controller instance if the routing endpoint is a sub-controller,
	 * null otherwise 
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
