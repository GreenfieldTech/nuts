package tech.greenfield.vertx.nuts;

import tech.greenfield.vertx.nuts.exceptions.InvalidRouteConfiguration;

import java.io.IOException;

import io.nats.client.*;

public class Nuts {

	private Connection client;
	
	public Nuts() {
		try {
			client = Nats.connect();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Cannot connect to server because of: " + e.getMessage());
		}
	}

	public Nuts(String url) {
		try {
			client = Nats.connect(url);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Cannot connect to server because of: " + e.getMessage());
		}
	}
	
	public Nuts(Connection con) {
		client = con;
	}
	
	public void executePath(Controller... apis) throws InvalidRouteConfiguration {
		NutsClient nutsClient = new NutsClient(client);
		for (Controller api : apis)
			nutsClient.configure(api);
	}
	
	public Connection getClient() {
		return client;
	}
	
}
