package tech.greenfield.vertx.nuts;

import tech.greenfield.vertx.nuts.annotations.Subscribe;
import tech.greenfield.vertx.nuts.exceptions.InvalidRouteConfiguration;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Objects;

import io.nats.client.*;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import io.vertx.core.Handler;

public class Nuts {
	
	protected final Logger logger = LoggerFactory.getLogger(Nuts.class);

	private Connection client;
	
	public Nuts() {
		client = null;
		try {
			client = Nats.connect();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Cannot connect to server because of: " + e.getMessage());
		}
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
	
	private void configure(Controller api, NutsMessage message) throws InvalidRouteConfiguration {
		for (RouteConfiguration conf : api.getRoutes()) {
			configureMessage(conf, Subscribe.class, message);
		}
	}
	
	private <T extends Annotation> void configureMessage(RouteConfiguration conf, Class<T> anot, NutsMessage message) throws InvalidRouteConfiguration {
		for (String uri : conf.uriForAnnotation(anot))
			configureMessage(uri, conf, message);
	}

	private void configureMessage(String uri, RouteConfiguration conf, NutsMessage message) throws InvalidRouteConfiguration {
		if (Objects.isNull(uri))
			return;
		
		if(message.getSubject().equals("null"))
			message.setSubject(uri);
		else
			message.setSubject(message.getSubject() + "." + uri);
			
		if(conf.isController()) {
			configure(conf.getController(), message);
			deleteFromPath(message);
			return;
		}
		
		//reached a leaf
		logger.info("Subscribing message: " + message.getSubject());
		
		try {
			Handler<NutsMessage> handler = conf.getHandler();
			client.subscribe(message.getSubject(), msg -> {
				handler.handle(new NutsMessage(client, msg));
			});
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		} 
		
		deleteFromPath(message);
	}

	private void deleteFromPath(NutsMessage message) {
		if(message.getSubject().contains("."))
			message.setSubject(message.getSubject().substring(0, message.getSubject().lastIndexOf(".")));
		else
			message.setSubject("null");
	}
	
}
