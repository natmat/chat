package chat;

import java.io.BufferedReader;
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
		BufferedReader inputBR = null;
		try {
			inputBR = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while (true) {
			System.out.println("readline()...");
			String in = "EMPTY";
			try {
				in = inputBR.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("read in:" + in);
			if ("QUIT".equals(in)) {
				break;
			}
		}
		try {
			inputBR.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
