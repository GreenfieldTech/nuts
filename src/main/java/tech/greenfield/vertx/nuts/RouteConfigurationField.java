package tech.greenfield.vertx.nuts;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import io.vertx.core.Handler;
import tech.greenfield.vertx.nuts.exceptions.InvalidRouteConfiguration;

public class RouteConfigurationField extends RouteConfiguration {

	private Field field;

	public RouteConfigurationField(Controller impl, Field f) {
		super(impl, f.getAnnotations());
		field = f;
	}

	@Override
	protected <T extends Annotation> T[] getAnnotation(Class<T> anot) {
		return field.getDeclaredAnnotationsByType(anot);
	}

	@Override
	public boolean isController() {
		return Controller.class.isAssignableFrom(field.getType());
	}

	@Override
	Controller getController() {
		return impl.getController(field);
	}

	@SuppressWarnings("unchecked")
	@Override
	Handler<NutsMessage> getHandler() throws IllegalArgumentException, IllegalAccessException, InvalidRouteConfiguration {
		if (!Handler.class.isAssignableFrom(field.getType()))
			throw new InvalidRouteConfiguration(this + " is not a valid handler or controller");
		field.setAccessible(true);
		return (Handler<NutsMessage>)field.get(impl);
	}

}
