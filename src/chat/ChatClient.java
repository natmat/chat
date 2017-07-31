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
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.text.AbstractDocument.BranchElement;

public class ChatClient implements Runnable {

	private static String serverAddress;
	private static DatagramSocket broadcastSocket;
	private Socket clientSocket = null;

	public static void main(String[] args) throws UnknownHostException, IOException {
		//		ChatServer.startChatServer();
		//		ChatServer.startUDPBroadcast();

		System.out.println(ChatClient.josephusSurvivor(7, 3));

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

	public ChatClient() {
		try {
			clientSocket = new Socket(ChatServer.getHostName(), ChatServer.getAcceptPort());
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("clientSocket:" + clientSocket);
	}

	@Override
	public void run() {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}

		String inputLine = null;
		while (true) {
			try {
				inputLine = br.readLine();
			} catch (IOException e) {
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
			e.printStackTrace();
		}
	}

	public static int josephusSurvivor(final int n, final int k) {
		ArrayList<Integer> a = new ArrayList<>(n);
		if (n == 1) {
			return(1);
		}
		
		// Fill array 0..n-1 with values 1..n
		int i;
		for (i = 0 ; i < n ; i++) {
			a.add(i, i+1);
		}
		
		i = 1;
		while (a.size() > 2) {
			i = (i + (k-1))%(a.size()-1);
			i = (i == 0) ? (a.size()-1) : i;
			a.remove(i);
		}  
		return(a.get(1));
	}

	public void action() {
		new Thread().start();
	}
}


