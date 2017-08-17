package chat;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;

import chat.ChatCommon.ClientEvent;
import chat.ChatCommon.ClientState;

public class ChatClient implements Runnable {

	private static String serverAddress;
	private static DatagramSocket broadcastSocket;
	private Socket clientSocket = null;
	private String name;
	private ChatCommon.ClientState state;
	
	public ChatClient() {
		state = ClientState.IDLE;
	}

	public static void main(String[] args) throws UnknownHostException, IOException {
		ChatClient.findServer();
		ChatClient client = new ChatClient();

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		new Thread(client).start();
	}

	private static void findServer() {
		System.out.println("FindServer()");
		broadcastSocket = null;
		try {
			broadcastSocket = new DatagramSocket(8300, InetAddress.getByName("0.0.0.0"));
			broadcastSocket.setBroadcast(true);
			broadcastSocket.setSoTimeout(5000);

		} catch (SocketException | UnknownHostException e1) {
			e1.printStackTrace();
		}

		new Thread(new Runnable( ) {
			@Override
			public void run() {
				try {
					Thread.sleep(2000);
					System.out.println("close()");
					broadcastSocket.close();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();

		while (serverAddress == null) {
			byte[] buf = new byte[4];
			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			try {
				System.out.println("receive()...");
				broadcastSocket.receive(packet);
				serverAddress = packet.getAddress().getHostAddress();
				System.out.println("<< " 
						+ serverAddress + ":" + Arrays.toString(packet.getData()));
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}
		}
		broadcastSocket.close();
	}

	public void connectClient() throws IOException {
		clientSocket = new Socket(ChatServer.getHostName(), ChatServer.getAcceptPort());
		clientEvent(ClientEvent.CONNECTED);
	}

	@Override
	public void run() {

		BufferedReader br = null;
		try {
			name = String.valueOf(clientSocket.getLocalPort());
			System.out.println(name);
			br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}

		String inputLine = null;
		boolean connected = true;
		while (connected) {
			clientEvent(ClientEvent.WAITING);
			try {
				inputLine = br.readLine();
				if (null == inputLine ) {
					connected = false;
					break;
				}
								
				clientEvent(ClientEvent.READING);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.println("C: " + inputLine + " * " + name);
			} catch (IOException e) {
				System.out.println("Client: " + clientSocket.getPort() + " " + e.getMessage());
				if (!e.getMessage().toUpperCase().equals(ChatCommon.CONNECTION_REFUSED)) {
					e.printStackTrace();
				}
			}

			double delta = System.currentTimeMillis() - Double.parseDouble(inputLine)*1E6;
			System.out.println("C: delta " + delta + "us\n");
			if ("QUIT".equals(inputLine)) {
				break;
			}
		}

		System.out.println("C: closing " + clientSocket.getLocalPort());
		try {
			clientSocket.close();
			br.close();
			ChatGui.setClientState(Color.RED);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void startClient() {
		new Thread(this).start();
	}

	public static void handleEvent() {
		ChatClient client = new ChatClient();
		try {
			client.connectClient();
			client.startClient();
		}
		catch(IOException e) {
			System.out.println("C: " + e.getMessage());
			if (!e.getMessage().toUpperCase().equals(ChatCommon.CONNECTION_REFUSED)) {
				e.printStackTrace();
			}
		}
	}

	private void clientEvent(ClientEvent ev) {
		switch(state){
		case IDLE:
			state = ClientState.CONNECTED;
			ChatGui.setClientState(Color.YELLOW);
			break;
		case CONNECTED:
			switch(ev) {
			case WAITING:
				ChatGui.setClientState(Color.YELLOW);
				break;
			case READING:
				ChatGui.setClientState(Color.GREEN);
				break;
			default:
				break;
			}
			break;
		default:
			break;
		}

	}
}


