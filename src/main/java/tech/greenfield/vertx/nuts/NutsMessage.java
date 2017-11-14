package tech.greenfield.vertx.nuts;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import io.nats.client.*;

public class NutsMessage extends Message{
	
	private Message msg;
	private Connection client;
	
	public NutsMessage(Connection newClient) {
		// subject can't be null
		msg = new Message("null", null, null);
		client = newClient;
	}
	
	public NutsMessage(Connection newClient, Message message) {
		msg = message;
		client = newClient;
	}
	
	@Override
	public String getSubject() {
		if(Objects.isNull(msg))
			return null;
		return msg.getSubject();
	}
	
	@Override
	public void setSubject(String subject) {
		msg.setSubject(subject);
	}
	
	public void reply(byte[] replyContent) {
		client.subscribe(msg.getSubject(), message -> {
		    try {
		    	Objects.requireNonNull(message.getReplyTo(), "The message doesn't know who to reply to!");
		    	client.publish(message.getReplyTo(), replyContent);
		    } catch (Exception e) {
		        e.printStackTrace();
		    }
		});
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
		Objects.requireNonNull(msg.getSubject(), "The message doesn't have a subject");
		msg.setData(sendContent.getBytes());
		publish(msg.getSubject(), msg.getData());
	}
	
	public void publish() {
		Objects.requireNonNull(msg.getSubject(), "The message doesn't have a subject");
		Objects.requireNonNull(msg.getData(), "getSubject");
		publish(msg.getSubject(), msg.getData());
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
