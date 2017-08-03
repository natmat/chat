package chat;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import javax.swing.SwingUtilities;

public class Chat {
	private static ChatServer server;
	private static ArrayList<ChatClient> clientList;

	public static void main(String[] args) {
		try 
		{
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					ChatGui.createAndShowGUI();
				}
			});
		} 
		catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
		}
		new Chat();
	}

	public Chat() {
		System.out.println("Chat()");
		server = ChatServer.getInstance();
		clientList = new ArrayList<ChatClient>(1);
	}

	public static void serverEvent() {
		server.handleEvent();
	}

	public static void clientEvent() {
		ChatClient client = new ChatClient();
		clientList.add(client);
		client.action();
	}
}

