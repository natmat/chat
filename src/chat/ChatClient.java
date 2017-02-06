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
			clientSocket = new Socket(ChatServer.getHostName(), ChatServer.getAcceptPort());
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
			try {
				inputLine = br.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			long delta = System.currentTimeMillis() - Long.parseLong(inputLine);
			System.out.println("client< " + delta + "ms\n");
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
