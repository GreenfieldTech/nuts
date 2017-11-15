package tech.greenfield.vertx.nuts;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import io.nats.client.*;

public class NutsMessage extends Message{
	
	private Message msg;
	private Connection client;
	
	public NutsMessage(Connection newClient, Message message) {
		msg = message;
		client = newClient;
	}
	
	@Override
	public String getSubject() {
		return msg.getSubject();
	}
	
	@Override
	public void setSubject(String subject) {
		msg.setSubject(subject);
	}
	
	public void reply(byte[] replyContent) {
		if(Objects.isNull(msg.getReplyTo()))
			throw new RuntimeException("The message doesn't know who to reply to!");
	    try {
	    	client.publish(msg.getReplyTo(), replyContent);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	public void reply(String replyContent) {
		reply(replyContent.getBytes());
	}

	
	public void publish(String subject, byte[] sendContent) {
		try {
			client.publish(subject, sendContent);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void publish(String subject, String sendContent) {
		publish(subject, sendContent.getBytes());
	}
	
	public void publish(String sendContent) {
		if(Objects.isNull(msg.getSubject()))
			throw new RuntimeException("The message doesn't have a subject");
		publish(msg.getSubject(), sendContent.getBytes());
	}
	
	public CompletableFuture<Message> subscribe(String subject) {
		return CompletableFuture.supplyAsync(() -> {
			SyncSubscription sub = client.subscribeSync(subject);
			Message message = null;
			try {
				message = sub.nextMessage();
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
			return message;
		});
	}
	
	public CompletableFuture<Message> subscribe() {
		return subscribe(msg.getSubject());
	}
	
}
