package chat;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class ChatClient implements Runnable {

	private Socket clientSocket = null;

	public static void main(String[] args) throws UnknownHostException, IOException {
		ChatServer.startChatServer();
		
		ChatServer.startUDPBroadcast();
		
		ChatClient.findServer();
		ChatClient client = new ChatClient();
		
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		new Thread(client).start();
	}

	private static void findServer() {
		System.out.println("FindServer()");
		DatagramSocket broadcastSocket = null;
		try {
			broadcastSocket = new DatagramSocket(8300, InetAddress.getByName("0.0.0.0"));
			broadcastSocket.setBroadcast(true);

		} catch (SocketException | UnknownHostException e1) {
			e1.printStackTrace();
		}
		
		int i = 10;
		while (i-- > 0) {
			byte[] buf = new byte[4];
			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			try {
				broadcastSocket.receive(packet);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			System.out.println(">>>Discovery packet received from: " 
					+ packet.getAddress().getHostAddress());
			
	        System.out.println(">>>Packet received; data: " 
	        		+ new String(packet.getData()));
		}
		broadcastSocket.close();
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
