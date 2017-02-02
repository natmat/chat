package chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

public class ChatClient extends Thread {

	private Socket clientSocket = null;
	private String serverAddress;

	public static void main(String[] args) throws UnknownHostException, IOException {
		ChatClient client = new ChatClient();		
		ChatServer.startChatServer();
		client.receive();
	}

	public ChatClient() {
		this.serverAddress = ChatServer.getHostAddress();
		try {
			clientSocket = new Socket(serverAddress, ChatServer.getAcceptPort());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("clientSocket:" + clientSocket);
	}

	public void receive() throws IOException {
		BufferedReader inputBR = null;
		try {
			inputBR = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while (true) {
			String in = inputBR.readLine();
			System.out.println("Client:" + this.getName() + " IN: " + in);
			if ("QUIT".equals(in)) {
				break;
			}
		}
		inputBR.close();
	}
}
