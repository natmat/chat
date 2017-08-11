package chat;

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

public class ChatClient implements Runnable {

	private static String serverAddress;
	private static DatagramSocket broadcastSocket;
	private Socket clientSocket = null;
	private boolean isAlive;

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
	}

	@Override
	public void run() {
		BufferedReader br = null;
		try {
			System.out.println("clientSocket localPort: " + clientSocket.getLocalPort());
			br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}

		String inputLine = null;
		isAlive = true;
		while (isAlive) {
			try {
				inputLine = br.readLine();
				if (null == inputLine ) {
					isAlive = false;
					break;
				}
				System.out.println("inputLine: " + inputLine);
			} catch (IOException e) {
				System.out.println("Client: " + clientSocket.getPort());
				if (!e.getMessage().equals(ChatCommon.connectionReset)) {
					e.printStackTrace();
				}
			}
			
			long delta = System.currentTimeMillis() - Long.parseLong(inputLine);
			System.out.println("client< " + delta + "ms\n");
			if ("QUIT".equals(inputLine)) {
				break;
			}
		}
		
		try {
			br.close();
			System.out.println("Client closing " + clientSocket.getPort());
			clientSocket.close();
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
			e.printStackTrace();
//			if (e.getMessage().equals("Connection refused")) {
//				System.out.println("ERROR " + e.getMessage());
//			}
		}
	}
}


