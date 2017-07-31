package chat;

import java.util.ArrayList;

import javax.swing.SwingUtilities;
	
public class Chat {
	
	private static ChatServer server;
	private static ArrayList<ChatClient> clientList;
	
	public static void main(String[] args) {
		new Chat();
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				ChatGui.createAndShowGUI();
			}
		});
	}
	
	public Chat() {
		init();
	}

	private static void init() {
		new ChatGui();
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

