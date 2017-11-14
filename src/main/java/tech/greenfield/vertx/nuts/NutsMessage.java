package tech.greenfield.vertx.nuts;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import io.nats.client.*;

public class NutsMessage extends Message{
	
	private Message msg;
	private Connection client;
	
	public NutsMessage(Message outerMessage) {
		msg.setSubject(msg.getSubject() + "." + outerMessage.getSubject());
	}

	public NutsMessage(Connection newClient) {
		msg = new Message();
		client = newClient;
	}
	
	public NutsMessage(String subject, String data, Connection newClient) {
        msg = new Message(subject, null, data.getBytes());
        client = newClient;
    }
	
	public NutsMessage(String subject, Connection newClient) {
        msg = new Message(subject, null, null);
        client = newClient;
    }
	
	public NutsMessage(String subject, String reply, byte[] data, Connection newClient) {
        msg = new Message(subject, reply, data);
        client = newClient;
    }
	
	
	public void reply(byte[] replyContent) {
		client.subscribe(msg.getSubject(), message -> {
		    try {
		    	client.publish(message.getReplyTo(), replyContent);
		    } catch (Exception e) {
		        e.printStackTrace();
		    }
		});
	}
	
	public void reply(String replyContent) {
		reply(replyContent.getBytes());
	}

	
	public void send(String subject, byte[] sendContent) {
		try {
			client.publish(subject, sendContent);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void send(String subject, String sendContent) {
		send(subject, sendContent.getBytes());
	}
	
	public void send(String sendContent) {
		Objects.requireNonNull(msg.getSubject(), "The message doesn't have a subject");
		msg.setData(sendContent.getBytes());
		send(msg.getSubject(), msg.getData());
	}
	
	public void send() {
		Objects.requireNonNull(msg.getSubject(), "The message doesn't have a subject");
		Objects.requireNonNull(msg.getData(), "getSubject");
		send(msg.getSubject(), msg.getData());
	}
	
	public CompletableFuture<Message> recieve(String subject) {
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
	
}
