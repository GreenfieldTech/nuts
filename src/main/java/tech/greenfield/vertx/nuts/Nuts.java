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

	public Nuts(Connection con) {
		configureClient(con);
	}

	public Nuts(String url) {
		configureClient(url);
	}
	
	public Nuts() {
		this(Nats.DEFAULT_URL);
	}
	
	/**
	 * Setup the client by connecting to a server in the url. If a client already exists, it will be replaced
	 * @param url  the path to the server to connect to
	 * @throws RuntimeException  if the server cannot be connected to
	 */
	public void configureClient(String url) {
		try {
			client = Nats.connect(url);
		} catch (IOException e) {
			logger.fatal("Unexpected error from NATS",e);
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Setup the client. If a client already exists, it will be replaced
	 * @param con  the client to setup
	 */
	public void configureClient(Connection con) {
		client = con;
	}
	
	/**
	 * Execute the methods and/or fields in the controllers
	 * @param apis  the controllers to execute
	 * @return the Nuts object that contains the client
	 */
	public Nuts setupController(Controller... apis) throws InvalidRouteConfiguration {
		for (Controller api : apis)
			configure(api);
		return this;
	}
	
	/**
	 * Get the current NATS client
	 * @return Get the current NATS client
	 */
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
		logger.debug("Trying subscribing message: " + subject.replaceFirst("^\\.", ""));
		
		try {
			Handler<NutsMessage> handler = conf.getHandler();
			client.subscribe(subject.replaceFirst("^\\.", ""), msg -> {
				NutsMessage message = new NutsMessage(client, msg);
				try {
					handler.handle(message);
				} catch (Throwable e) {
					logger.error("Error handling message in controller " + conf, e);
					message.errorReply(e);
				}
			});
		} catch (IllegalArgumentException | IllegalAccessException e) {
			logger.error("Unexpected error in calling controller",e);
		} 
		
		logger.debug("Subscribed message: " + subject.replaceFirst("^\\.", ""));
		
	}
	
}
