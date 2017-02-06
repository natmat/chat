package chat;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ChatServer implements Runnable {

	private static ServerSocket socketAccept = null;
	private final ExecutorService clientPool;
	private static String hostName;
	private final static int clientPoolSize = 1;
	private static ArrayList<Socket> clientSockets;
	private static ChatServer instance = null;

	public final static int acceptPort = 8304;

	public static void main(String[] args) throws IOException {
		instance = ChatServer.getInstance();
		startChatServer();

		exitTimeout();
	}

	private static void exitTimeout() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				ChatServer.getInstance();
				if (ChatServer.clientSockets.isEmpty()) {
					System.out.println("No clients. EXIT");
					System.exit(0);
				}
			}
		}).start();
	}

	public static ChatServer getInstance() {
		if (null == instance) {
			instance = new ChatServer();
		}
		return(instance);
	}

	private ChatServer() {
		try {
			socketAccept = new ServerSocket();
			hostName = Inet4Address.getLocalHost().getHostName();
			socketAccept.bind(new InetSocketAddress(hostName, acceptPort));
		} catch (IOException e) {
			e.printStackTrace();
		}

		clientPool = Executors.newFixedThreadPool(clientPoolSize);
		clientSockets = new ArrayList<>(clientPoolSize);

		startUDPBroadcast();
	}

	private void startUDPBroadcast() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				System.out.println("startUDPBroadcast");
				DatagramSocket broadcastSocket;
				try {
					broadcastSocket = new DatagramSocket(8300);
					broadcastSocket.setBroadcast(true);

					InetAddress ia = Inet4Address.getLocalHost();
					System.out.println(ia);
					NetworkInterface ni = NetworkInterface.getByInetAddress(ia);
					System.out.println(ni);
					InetAddress bcast = ni.getInterfaceAddresses().get(1).getBroadcast();
					System.out.println(bcast);
				} catch (SocketException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				byte[] buf = {(byte)0x12, (byte)0x34, (byte)0x56, (byte)0x78};
				DatagramPacket packet = new DatagramPacket(buf, 4, bcast, 8300);

				Integer i = 1;
				i++;
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}

	public static void startChatServer() {
		Thread serverThread = new Thread(getInstance());
		serverThread.start();
	}

	@Override
	public void run() {
		System.out.println("Running server");
		for(;;) {
			try {
				System.out.println("Waiting to accept...");
				Socket client = socketAccept.accept(); 
				clientPool.execute(new Handler(client));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				clientPool.shutdown();
			}

		}
	}

	private class Handler implements Runnable {

		public Handler(Socket acceptSocket) {
			System.out.println("Handler socket: " + acceptSocket);
			clientSockets.add(acceptSocket);
		}

		@Override
		public void run() {
			System.out.println("Accepted...");
			boolean running = true;
			while (running) {
				running = (clientSockets.size() > 0);
				for (Socket client : clientSockets) {
					PrintStream outStream = null;
					try {
						outStream = new PrintStream(client.getOutputStream());
					} catch (IOException e) {
						e.printStackTrace();
					}

					String msg = Long.toString(System.currentTimeMillis());
					System.out.println("Server> " + msg);
					outStream.println(msg);

					try {
						Thread.sleep(500L);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}		
	}	

	void shutdownAndAwaitTermination(ExecutorService pool) {
		try {
			if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
				pool.shutdown();
				if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
					System.err.println("Pool did not terminate");
				}
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			pool.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}

	private int numberOfClient() {
		return(clientSockets.size());
	}

	public static int getAcceptPort() {
		return(acceptPort);
	}

	/**
	 * Returns the IP address string in textual presentation.
	 * @return host address
	 */
	public static String getHostName() {
		System.out.println("hostName=" + hostName);
		return(hostName);
	}
}
