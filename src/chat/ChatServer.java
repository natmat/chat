package chat;

import java.awt.Color;
import java.io.IOException;
import java.io.PrintStream;
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
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ChatServer implements Runnable {

	private static ServerSocket socketAccept = null;
	private final ExecutorService clientPool;
	private static String hostName;
	private final static int clientPoolSize = 1;
	private static ArrayList<Socket> clientSockets;
	private static ChatServer instance = null; // Singleton
	private static boolean isBeaconing;

	public final static int acceptPort = 8304;

	private static enum ServerState {
		IDLE, 
		ACTIVE,
		CONNECTED
	};
	static ServerState state;
	private static boolean isConnected;

	public static void main(String[] args) throws IOException {
		instance = ChatServer.getInstance();

		System.out.println("startChatServer");
		startChatServer();

		System.out.println("startUDPBroadcast");
		startUDPBroadcast();

		exitTimeout(10);
	}

	private static void exitTimeout(int timeoutS) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(1000 * timeoutS);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				ChatServer.getInstance();
				if (ChatServer.clientSockets.isEmpty()) {
					System.out.println("No clients connnected. EXIT");
					System.exit(0);
				}
			}
		}).start();
	}

	public static ChatServer getInstance() {
		if (null == instance) {
			instance = new ChatServer();
		}
		return (instance);
	}

	private ChatServer() {
		state = ServerState.IDLE;
		ChatGui.setServerState(Color.RED);
		isConnected = false;
		isBeaconing = false;

		try {
			socketAccept = new ServerSocket();
			hostName = Inet4Address.getLocalHost().getHostName();
			socketAccept.bind(new InetSocketAddress(hostName, acceptPort));
		} catch (IOException e) {
			e.printStackTrace();
		}

		clientPool = Executors.newFixedThreadPool(clientPoolSize);
		clientSockets = new ArrayList<>(clientPoolSize);
	}

	private static void setState() {
		switch(state) {
		case IDLE:
			state = ServerState.ACTIVE;
			ChatGui.setServerState(Color.YELLOW);
			break;
		case ACTIVE:
			if (isConnected) {
				state = ServerState.CONNECTED;
				ChatGui.setServerState(Color.GREEN);
			}
			else {
				state = ServerState.IDLE;
				ChatGui.setServerState(Color.RED);
			}
			break;
		default:
			System.out.println("State ERROR");
			break;
		}
	}

	private static InetAddress getBroadcastAddress() {
		HashSet<InetAddress> listOfBroadcasts = new HashSet<>();
		Enumeration<NetworkInterface> list;
		try {
			list = NetworkInterface.getNetworkInterfaces();

			while (list.hasMoreElements()) {
				NetworkInterface iface = list.nextElement();
				if (iface == null)
					continue;

				if (!iface.isLoopback() && iface.isUp()) {
					Iterator<InterfaceAddress> it = iface.getInterfaceAddresses().iterator();
					while (it.hasNext()) {
						InterfaceAddress address = it.next();
						if (address == null)
							continue;

						InetAddress broadcast = address.getBroadcast();
						if (broadcast != null) {
							listOfBroadcasts.add(broadcast);
						}
					}
				}
			}
		} catch (SocketException ex) {
			System.err.println("Error while getting network interfaces");
			ex.printStackTrace();
		}

		InetAddress ba = null;
		if (null == listOfBroadcasts.iterator()) {
			ba = listOfBroadcasts.iterator().next();
		}
		return(ba);
	}

	/**
	 * Pulse UDP broadcast with increment byte[] data
	 */
	public static void startUDPBroadcast() {
		isBeaconing = true;
		setState();

		new Thread(new Runnable() {
			@Override
			public void run() {
				System.out.println("startUDPBroadcast");
				InetAddress broadcastAddress = getBroadcastAddress();
				if (null == broadcastAddress) {
					System.out.println("ERROR: broadcastAddress null");
					return;
				}

				DatagramSocket broadcastSocket = null;
				try {
					broadcastSocket = new DatagramSocket(8300);
					broadcastSocket.setBroadcast(true);
				} catch (SocketException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				int i = 1;
				while (ChatServer.clientSockets.size() == 0) {
					byte[] buf = { (byte) i, (byte) i, (byte) i, (byte) i };
					i++;
					DatagramPacket packet = new DatagramPacket(buf, buf.length, broadcastAddress, 8300);
					try {
						System.out.println("packet " + i + ":" + Arrays.toString(packet.getData()));
						broadcastSocket.send(packet);
						Thread.sleep(1000);
					} catch (InterruptedException | IOException e) {
						e.printStackTrace();
					}
				}
				broadcastSocket.close();
			}
		}).start();
	}

	public static void startChatServer() {
		Thread serverThread = new Thread(ChatServer.getInstance());
		serverThread.start();
	}

	@Override
	public void run() {
		System.out.println("Starting server");

		startUDPBroadcast();

		while (true) {
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
			isConnected = true;
			setState();

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

	public static int getAcceptPort() {
		return (acceptPort);
	}

	/**
	 * Returns the IP address string in textual presentation.
	 * 
	 * @return host address
	 */
	public static String getHostName() {
		System.out.println("hostName=" + hostName);
		return (hostName);
	}

	public void handleEvent() {
		switch(state) {
		case IDLE: 
			startChatServer();
			break;
		case ACTIVE:
			// Stop server
			break;
		case CONNECTED:
			// Terminate connection
			break;
		default:
			// ERROR
			break;
		}
	}
}
