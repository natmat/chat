package chat;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

public class ChatClient implements Runnable {

	private Socket clientSocket = null;
	private String serverAddress;

	public static void main(String[] args) throws UnknownHostException, IOException {
		ChatServer.startChatServer();
		ChatClient client = new ChatClient();
		new Thread(client).start();
	}

	public ChatClient() {
		try {
			clientSocket = new Socket(ChatServer.getHostAddress(), ChatServer.getAcceptPort());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("clientSocket:" + clientSocket);
	}

	@Override
	public void run() {
		DataInputStream inputStream = null;
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String inputLine = null;
		while (true) {
			System.out.println("readline()...");
			try {
				inputLine = br.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("client< " + inputLine);
			if ("QUIT".equals(inputLine)) {
				break;
			}
		}
		try {
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
