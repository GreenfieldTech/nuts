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
		try {
			client = Nats.connect();
		} catch (IOException e) {
			logger.fatal(e.getStackTrace());
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
	
	public Nuts setupController(Controller... apis) throws InvalidRouteConfiguration {
		for (Controller api : apis)
			configure(api);
		return this;
	}
	
	public Connection getNATSClient() {
		return client;
	}
	
	private void configure(Controller api) throws InvalidRouteConfiguration {
		configure(api, "");
	}
	
	private void configure(Controller api, String messageSubject) throws InvalidRouteConfiguration {
		for (RouteConfiguration conf : api.getRoutes()) {
			configureMessage(conf, Subscribe.class, messageSubject);
		}
	}
	
	private <T extends Annotation> void configureMessage(RouteConfiguration conf, Class<T> anot, String messageSubject) throws InvalidRouteConfiguration {
		for (String postfixSubject : conf.subjectForAnnotation(anot))
			configureMessage(postfixSubject, conf, messageSubject);
	}

	private void configureMessage(String postfixSubject, RouteConfiguration conf, String messageSubject) throws InvalidRouteConfiguration {
		if (Objects.isNull(postfixSubject))
			return;
		
		String subject = messageSubject + "." + postfixSubject;
			
		if(conf.isController()) {
			configure(conf.getController(), subject);
			return;
		}
		
		//reached a leaf
		logger.info("Trying subscribing message: " + subject.replaceFirst("^\\.", ""));
		
		try {
			Handler<NutsMessage> handler = conf.getHandler();
			client.subscribe(subject.replaceFirst("^\\.", ""), msg -> {
				handler.handle(new NutsMessage(client, msg));
			});
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		} 
		
		logger.info("Subscribed message: " + subject.replaceFirst("^\\.", ""));
		
	}
	
}
