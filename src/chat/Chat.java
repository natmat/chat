package chat;

import java.lang.reflect.InvocationTargetException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class Chat {
	private static final int MAX_CLIENT_COUNT = 1;
	private static ChatServer server;

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
	}

	public static void serverEvent() {
		server.handleGuiServerEvent();
	}

	public static void clientEvent() {
		if (ChatServer.countClients() == MAX_CLIENT_COUNT) {
			JOptionPane.showMessageDialog(null, "Max clients reached");
		}
		else {
			ChatClient.handleEvent();
		}
	}

	public static void udpEvent() {
		server.handleGuiUdpEvent();
	}
}

