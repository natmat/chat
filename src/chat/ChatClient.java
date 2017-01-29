package chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class ChatClient extends Thread {

	private int port;
	private Socket clientSocket = null;

	public static void main(String[] args) throws UnknownHostException, IOException {
		Socket server = new Socket("localhost", ChatServer.acceptPort);
        InetAddress addr = server.getInetAddress();
        System.out.println("Connected to " + addr);
        System.out.println("Port:"  + server.getPort());
	}

	public ChatClient(final Socket clientSocket) {
		this.clientSocket = clientSocket;
		this.port = clientSocket.getPort();
		System.out.println("Client:" + this.port);
	}

	public void receive() throws IOException {
		BufferedReader inputBR = null;
		try {
			inputBR = new BufferedReader(
					new InputStreamReader(clientSocket.getInputStream()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while (true) {
			String in = inputBR.readLine();
			System.out.println("IN: " + in);
			if ("QUIT".equals(in)) {
				break;
			}
		}
		inputBR.close();
	}
}
