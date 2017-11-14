package tech.greenfield.vertx.nuts;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import io.vertx.core.Handler;
import tech.greenfield.vertx.nuts.exceptions.InvalidRouteConfiguration;

public class RouteConfigurationMethod extends RouteConfiguration {

	private Method method;

	public RouteConfigurationMethod(Controller impl, Method m) {
		super(impl, m.getAnnotations());
		method = m;
	}

	@Override
	protected <T extends Annotation> T[] getAnnotation(Class<T> anot) {
		return method.getDeclaredAnnotationsByType(anot);
	}

	@Override
	public boolean isController() {
		return false; // a method is always a request handler and never a sub router
	}

	@Override
	Controller getController() {
		throw new RuntimeException("Not implemented");
	}

	@Override
	Handler<NutsMessage> getHandler() throws IllegalArgumentException, IllegalAccessException, InvalidRouteConfiguration {
		method.setAccessible(true);
		Class<?>[] params = method.getParameterTypes();
		if (params.length == 1 || params[0].isAssignableFrom(NutsMessage.class) || 
				// we should support working with methods that take specializations for Request, we'll rely on the specific implementation's
				// getRequest() to provide the correct type
				NutsMessage.class.isAssignableFrom(params[0])) 
			return m -> {
				try {
					method.invoke(impl, m);
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				} catch (IllegalAccessException | IllegalArgumentException e) {
					// shouldn't happen
					throw new RuntimeException("Invalid message handler " + this + ": " + e);
				}
			};
		throw new InvalidRouteConfiguration("Invalid arguments list for " + this);
	}

}
