package tech.greenfield.vertx.nuts;

import tech.greenfield.vertx.nuts.annotations.Subscribe;
import tech.greenfield.vertx.nuts.exceptions.InvalidRouteConfiguration;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Objects;

import io.nats.client.*;

public class Nuts {

	private Connection client;
	
	public Nuts() {
		client = null;
//		try {
//			client = Nats.connect();
//		} catch (IOException e) {
//			e.printStackTrace();
//			throw new RuntimeException("Cannot connect to server because of: " + e.getMessage());
//		}
	}
	
	public Nuts(Connection con) {
		configureClient(con);
	}

	public Nuts(String url) {
		configureClient(url);
	}
	
	public void configureClient(String url) {
		try {
			client = Nats.connect(url);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Cannot connect to server because of: " + e.getMessage());
		}
	}
	
	public void configureClient(Connection con) {
		client = con;
	}
	
	public Nuts executePath(Controller... apis) throws InvalidRouteConfiguration {
		for (Controller api : apis)
			configure(api);
		return this;
	}
	
	public Connection getNATSClient() {
		return client;
	}
	
	private void configure(Controller api) throws InvalidRouteConfiguration {
		configure(api, new NutsMessage(client));
	}
	
	private void configure(Controller api, NutsMessage messageWrapper) throws InvalidRouteConfiguration {
		for (RouteConfiguration conf : api.getRoutes()) {
			tryConfigureRoute(conf, Subscribe.class, messageWrapper);
		}
	}
	
	private <T extends Annotation> void tryConfigureRoute(RouteConfiguration conf, Class<T> anot, NutsMessage message) throws InvalidRouteConfiguration {
		for (String uri : conf.uriForAnnotation(anot))
			tryConfigureMessage(uri, conf, message);
	}

	private void tryConfigureMessage(String uri, RouteConfiguration conf, NutsMessage message) throws InvalidRouteConfiguration {
		if (Objects.isNull(uri))
			return;
		
		if(message.getSubject().equals("null"))
			message.setSubject(uri);
		else
			message.setSubject(message.getSubject() + "." + uri);
			
		if(conf.isController()) {
			configure(conf.getController(), message);
			return;
		}
		
		//reached a leaf
		message.subscribe();
	}
	
}
