package tech.greenfield.vertx.nuts;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import io.nats.client.*;
import io.vertx.core.json.JsonObject;

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
	
	/**
	 * Helper method for Nuts to get the data of the message as a string
	 * @return The data of the message
	 */
	public String getDataString() {
		return new String(msg.getData());
	}
	
	/**
	 * Helper method for Nuts to get the NATS Message object of the message
	 * @return the NATS message object 
	 */
	public Message getMessage() {
		return msg;
	}
	
	/**
	 * Replies to that message using it's "replyTo" field as a subject
	 * @param replyContent  the content to be sent with the reply
	 * @throws RuntimeException if the replyTo field of the message is empty
	 */
	public void reply(byte[] replyContent) {
		if (Objects.isNull(msg.getReplyTo()))
			throw new IllegalArgumentException("The message doesn't know who to reply to!");
		try {
			client.publish(msg.getReplyTo(), replyContent);
		} catch (IOException e) {
			throw new IllegalArgumentException("Unexpected error replying",e);
		}
	}
	
	/**
	 * Replies to that message using it's "replyTo" field as a subject
	 * @param replyContent  the content to be sent with the reply
	 * @throws RuntimeException if the replyTo field of the message is empty
	 */
	public void reply(String replyContent) {
		reply(replyContent.getBytes());
	}
	
	/**
	 * Replies to that message using it's "replyTo" field as a subject
	 * @param replyContent  the content to be sent with the reply
	 * @throws RuntimeException if the replyTo field of the message is empty
	 */
	public void reply(JsonObject replyContent) {
		reply(replyContent.toString());
	}

	/**
	 * Publishes a message to the given subject
	 * @param subject  the subject to which the message will be published to
	 * @param sendContent  the content of the message
	 * @param reply  the subject to which subscribers should send responses
	 */
	public void publish(String subject, byte[] sendContent, String reply) {
		try {
			if(Objects.nonNull(reply))
				client.publish(subject, reply, sendContent);
			else
				client.publish(subject, sendContent);
		} catch (IOException e) {
			throw new IllegalArgumentException("Unexpected error publishing content",e);
		}
	}
	
	/**
	 * Publishes a message to the given subject
	 * @param subject  the subject to which the message will be published to
	 * @param sendContent  the content of the message
	 */
	public void publish(String subject, byte[] sendContent) {
		publish(subject, sendContent, null);
	}
	
	/**
	 * Publishes a message to the given subject
	 * @param subject  the subject to which the message will be published to
	 * @param sendContent  the content of the message
	 */
	public void publish(String subject, String sendContent) {
		publish(subject, sendContent.getBytes(), null);
	}
	
	/**
	 * Publishes a message to the given subject
	 * @param subject  the subject to which the message will be published to
	 * @param sendContent  the content of the message
	 */
	public void publish(String subject, JsonObject sendContent) {
		publish(subject, sendContent.toString());
	}
	
	/**
	 * Publishes a message to it's existing subject
	 * @param sendContent  the content of the message
	 * @throws RuntimeException if the subject field of the message is empty
	 */
	public void publish(String sendContent) {
		if(Objects.isNull(msg.getSubject()))
			throw new RuntimeException("The message doesn't have a subject");
		publish(msg.getSubject(), sendContent.getBytes(), null);
	}
	
	/**
	 * Publishes a message to it's existing subject
	 * @param sendContent  the content of the message
	 * @throws RuntimeException if the subject field of the message is empty
	 */
	public void publish(JsonObject sendContent) {
		publish(sendContent.toString());
	}
	
	/**
	 * Subscribes to a subject
	 * @param subject  the subject to subscribe to
	 * @return a completable future that will resolve when a message will be received on this subject, containing the message received.
	 */
	public CompletableFuture<NutsMessage> subscribeAsync(String subject) {
		return CompletableFuture.supplyAsync(() -> {
			SyncSubscription sub = client.subscribeSync(subject);
			Message message = null;
			try {
				message = sub.nextMessage();
			} catch (IOException | InterruptedException e) {
				throw new IllegalArgumentException("Unexpected error retrieving a message",e);
			}
			return new NutsMessage(client, message);
		});
	}
	
	/**
	 * Subscribes to a subject
	 * @return a completable future that will resolve when a message will be received on this subject, containing the message received.
	 * @throws RuntimeException if the subject field of the message is empty
	 */
	public CompletableFuture<NutsMessage> subscribeAsync() {
		if(Objects.isNull(msg.getSubject()))
			throw new RuntimeException("The message doesn't have a subject");
		return subscribeAsync(msg.getSubject());
	}
	
	/**
	 * Handle errors. Replies to the message ReplyTo() with the error message
	 * @param thr  the error throwable object
	 * @throws RuntimeException if the replyTo field of the message is empty
	 */
	public void errorReply(Throwable thr) {
		reply(new JsonObject().put("status", "false").put("message", thr.getMessage()));
	}
	
	
	/**
	 * Publishes a message to it's existing subject and receiving the response message
	 * @param sendContent  the content of the message
	 * @return a completable future that will resolve when a message will be received, containing the message received.
	 * @throws IOException | InterruptedException if the request fails to send
	 * 
	 */
	public CompletableFuture<NutsMessage> request(byte[] sendContent) {
		return request(msg.getSubject(), sendContent);
	}
	
	/**
	 * Publishes a message to it's existing subject and receiving the response message
	 * @param sendContent  the content of the message
	 * @return a completable future that will resolve when a message will be received, containing the message received.
	 * @throws IOException | InterruptedException if the request fails to send
	 * 
	 */
	public CompletableFuture<NutsMessage> request(String sendContent) {
		return request(msg.getSubject(), sendContent.getBytes());
	}
	
	/**
	 * Publishes a message to it's existing subject and receiving the response message
	 * @param sendContent  the content of the message
	 * @return a completable future that will resolve when a message will be received, containing the message received.
	 * @throws IOException | InterruptedException if the request fails to send
	 * 
	 */
	public CompletableFuture<NutsMessage> request(JsonObject sendContent) {
		return request(sendContent.toString());
	}
	
	/**
	 * Publishes a message to it's existing subject and receiving the response message
	 * @param sendContent  the content of the message
	 * @param timeOut  the time (in milliseconds) to wait for the reply message to arrive
	 * @return a completable future that will resolve when a message will be received, containing the message received.
	 * @throws IOException | InterruptedException if the request fails to send
	 * 
	 */
	public CompletableFuture<NutsMessage> request(byte[] sendContent, long timeOut) {
		return request(msg.getSubject(), sendContent, timeOut);
	}
	
	/**
	 * Publishes a message to it's existing subject and receiving the response message
	 * @param sendContent  the content of the message
	 * @param timeOut  the time (in milliseconds) to wait for the reply message to arrive
	 * @return a completable future that will resolve when a message will be received, containing the message received.
	 * @throws IOException | InterruptedException if the request fails to send
	 * 
	 */
	public CompletableFuture<NutsMessage> request(String sendContent, long timeOut) {
		return request(msg.getSubject(), sendContent.getBytes(), timeOut);
	}
	
	/**
	 * Publishes a message to it's existing subject and receiving the response message
	 * @param sendContent  the content of the message
	 * @param timeOut  the time (in milliseconds) to wait for the reply message to arrive
	 * @return a completable future that will resolve when a message will be received, containing the message received.
	 * @throws IOException | InterruptedException if the request fails to send
	 * 
	 */
	public CompletableFuture<NutsMessage> request(JsonObject sendContent, long timeOut) {
		return request(sendContent.toString(), timeOut);
	}
	
	/**
	 * Publishes a message to the given subject and receiving the response message
	 * @param sendContent  the content of the message
	 * @return a completable future that will resolve when a message will be received, containing the message received.
	 * @throws IOException | InterruptedException if the request fails to send
	 * 
	 */
	public CompletableFuture<NutsMessage> request(String subject, String sendContent){
		return request(subject, sendContent.getBytes());
	}
	
	/**
	 * Publishes a message to the given subject and receiving the response message
	 * @param sendContent  the content of the message
	 * @return a completable future that will resolve when a message will be received, containing the message received.
	 * @throws IOException | InterruptedException if the request fails to send
	 * 
	 */
	public CompletableFuture<NutsMessage> request(String subject, JsonObject sendContent){
		return request(subject, sendContent.toString());
	}
	
	/**
	 * Publishes a message to the given subject and receiving the response message
	 * @param sendContent  the content of the message
	 * @return a completable future that will resolve when a message will be received, containing the message received.
	 * @throws IOException | InterruptedException if the request fails to send
	 * 
	 */
	public CompletableFuture<NutsMessage> request(String subject, byte[] sendContent){
		return request(subject, sendContent, -1);
	}
	
	/**
	 * Publishes a message to the given subject and receiving the response message
	 * @param sendContent  the content of the message
	 * @param timeOut  the time (in milliseconds) to wait for the reply message to arrive
	 * @return a completable future that will resolve when a message will be received, containing the message received.
	 * @throws IOException | InterruptedException if the request fails to send
	 * 
	 */
	public CompletableFuture<NutsMessage> request(String subject, byte[] sendContent, long timeOut) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				return client.request(subject, sendContent);
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
			return null;
		}).thenApply(msg -> {
			return new NutsMessage(client, msg);
		});
	}
	
}
