package com.jelte.norii.multiplayer;

import java.util.concurrent.ConcurrentLinkedQueue;

import com.github.czyzby.websocket.WebSocket;
import com.github.czyzby.websocket.WebSockets;
import com.jelte.norii.multiplayer.NetworkMessage.MessageType;

public class ServerCommunicator {
	private static final String APP_LINK = "norii-ipmpb.ondigitalocean.app";
	private final ConcurrentLinkedQueue<NetworkMessage> receivedMessages = new ConcurrentLinkedQueue<>();
	private WebSocket socket;
	private static ServerCommunicator instance = null;
	private String clientID;

	private ServerCommunicator() {
		initMultiplayer();
	}

	private void initMultiplayer() {
		socket = WebSockets.newSocket(WebSockets.toSecureWebSocketUrl(APP_LINK, 443));
		socket.setSendGracefully(true);
		socket.addListener(new MyWebSocketAdapter());
		socket.connect();
	}

	public void addMessageFromServer(NetworkMessage message) {
		if (preProcessMessage(message)) {
			receivedMessages.add(message);
		}
	}

	private boolean preProcessMessage(NetworkMessage message) {
		if (message.getType() == MessageType.CONNECTED) {
			clientID = message.getSender();
			return false;
		}
		return true;
	}

	public NetworkMessage getOldestMessageFromServer() {
		return receivedMessages.poll();
	}

	public boolean isNewMessage() {
		return (receivedMessages.peek() != null);
	}

	public void sendMessage(NetworkMessage message) {
		socket.send(message.messageToString());
	}

	public String getClientID() {
		return clientID;
	}

	public static ServerCommunicator getInstance() {
		if (instance == null) {
			instance = new ServerCommunicator();
		}

		return instance;
	}
}
