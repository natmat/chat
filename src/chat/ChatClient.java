package chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class ChatClient extends Thread {

	private int acceptPort;
	private Socket clientSocket = null;
	private String serverInetAddress;

	public static void main(String[] args) throws UnknownHostException, IOException {
		ChatServer server = new ChatServer();
		ChatClient client = new ChatClient();
	}

	public ChatClient() {
		this.acceptPort = ChatServer.getAcceptPort();
		this.serverInetAddress = ChatServer.getInetAddress();
		try {
			clientSocket = new Socket(serverInetAddress, acceptPort);
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
