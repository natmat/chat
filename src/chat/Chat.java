package chat;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class Chat {
	private static final int MAX_CLIENT_COUNT = 1;
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
		clientList = new ArrayList<ChatClient>();
	}

	public static void serverEvent() {
		server.handleGuiServerEvent();
	}

	public static void clientEvent() {
		if (clientList.size() == MAX_CLIENT_COUNT) {
			JOptionPane.showMessageDialog(null, "Max clients reached");
		}
		else {
			ChatClient.handleEvent();
		}
	}

	public static void udpEvent() {
		server.handleGuiUdpEvent();
	}

	public static void addClient(ChatClient client) {
		clientList.add(client);
	}
}

