package chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientThread extends Thread {

	private int port;
	private Socket clientSocket = null;

	public static void main(String[] args) {
	}

	public ClientThread(final Socket inClientSocket) {
		this.clientSocket = inClientSocket;
		port = inClientSocket.getPort();
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
