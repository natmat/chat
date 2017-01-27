package chat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer {

	private static final int SERVER_PORT = 8304;
	private static ServerSocket serverSocket;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			serverSocket = new ServerSocket(SERVER_PORT);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		while (true) {
			Socket clientSocket = null;
			try {
				clientSocket = serverSocket.accept();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			final ClientThread client = new ClientThread(clientSocket);
			try {
				client.receive();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
