package tech.greenfield.vertx.nuts;

import java.lang.annotation.Annotation;
import java.util.Objects;

import io.nats.client.Connection;
import tech.greenfield.vertx.nuts.annotations.Endpoint;
import tech.greenfield.vertx.nuts.annotations.Post;
import tech.greenfield.vertx.nuts.annotations.Put;
import tech.greenfield.vertx.nuts.exceptions.InvalidRouteConfiguration;

public class NutsClient {

	private Connection client;
	
	public NutsClient(Connection client) {
		this.client = client;
	}
	
	public void configure(Controller api) throws InvalidRouteConfiguration {
		configure(api, new NutsMessage(client));
	}
	
	private void configure(Controller api, NutsMessage messageWrapper) throws InvalidRouteConfiguration {
		for (RouteConfiguration conf : api.getRoutes()) {
			tryConfigureRoute(conf, Endpoint.class, messageWrapper);
			tryConfigureRoute(conf, Put.class, messageWrapper);
			tryConfigureRoute(conf, Post.class, messageWrapper);
		}
	}
	
	private <T extends Annotation> void tryConfigureRoute(RouteConfiguration conf, Class<T> anot, NutsMessage message) throws InvalidRouteConfiguration {
		for (String uri : conf.uriForAnnotation(anot))
			tryConfigureRoute(uri, conf, message);
	}

	private void tryConfigureRoute(String uri, RouteConfiguration conf, NutsMessage message) throws InvalidRouteConfiguration {
		if (Objects.isNull(uri))
			return;
		
		if(Objects.isNull(message.getSubject()))
			message.setSubject(uri);
		else
			message.setSubject(message.getSubject() + "." + uri);
			
		if(conf.isController()) {
			configure(conf.getController(), message);
			return;
		}
		
		try {
			conf.getHandler(); // handle/execute this code with that message;
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
			
		
	}
}
