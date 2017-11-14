package tech.greenfield.vertx.nuts;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import tech.greenfield.vertx.nuts.exceptions.InvalidRouteConfiguration;

import java.io.IOException;

import io.nats.client.*;

public class Nuts {

	private Vertx vertx;
	private Connection client;
	
	public Nuts(Vertx vertx) {
		this.vertx = vertx;
		client = null;
//		try {
//			client = Nats.connect();
//		} catch (IOException e) {
//			e.printStackTrace();
//			throw new RuntimeException("Cannot connect to server because of: " + e.getMessage());
//		}
	}

	public Nuts(Vertx vertx, String url) {
		this.vertx = vertx;
		try {
			client = Nats.connect(url);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Cannot connect to server because of: " + e.getMessage());
		}
	}
	
	public Nuts(Vertx vertx, Connection con) {
		this.vertx = vertx;
		client = con;
	}
	
	public Handler<HttpServerRequest> setupRequestHandler(Controller... apis) throws InvalidRouteConfiguration {
		Router router = new Router(vertx);
		for (Controller api : apis)
			router.configure(api);
		return router::accept;
	}
	
	public Connection getClient() {
		return client;
	}
	
}
