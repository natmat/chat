package chat;

import java.util.ArrayList;

import javax.swing.SwingUtilities;

public class Chat {

	private static ChatServer server;
	private static ArrayList<ChatClient> clientList;

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				ChatGui.createAndShowGUI();
			}
		});

		new Chat();
	}

	public Chat() {
		server = ChatServer.getInstance();
		clientList = new ArrayList<ChatClient>(1);
	}

	public static void actionServer() {
		ChatServer.action();
	}

	public static void actionClient() {
		ChatClient client = new ChatClient();
		clientList.add(client);
		client.action();
	}
}

