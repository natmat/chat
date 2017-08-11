package chat;

import java.awt.Color;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import chat.ChatCommon.ServerEvent;
import chat.ChatCommon.ServerState;

public class ChatServer implements Runnable {

	private static ServerSocket socketAccept = null;
	private ExecutorService clientPool;
	public static boolean transmitting;
	private static String hostName;
	private final static int clientPoolSize = 10;
	private static ArrayList<Socket> clientSockets;
	private static ChatServer instance = null; // Singleton
	public final static int acceptPort = 8304;

	static ServerState state;
	private static boolean active;
	private static Thread serverThread;

	public static void main(String[] args) throws IOException {
		instance = ChatServer.getInstance();

		startChatServer();
		startUDPBroadcast();

		exitTimeout(1000);
	}

	private static void exitTimeout(final int timeoutS) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(1000 * timeoutS);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				System.out.println("TIMEOUT");

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

	ChatServer() {
		active = false;
		state = ServerState.IDLE;
		ChatGui.setServerState(Color.RED);
		ChatGui.setUdpState(Color.RED);

		clientPool = Executors.newFixedThreadPool(clientPoolSize);
		clientSockets = new ArrayList<>(clientPoolSize);

		newSocketAccept();
	}

	private void newSocketAccept() {
		try {
			socketAccept = new ServerSocket();
			hostName = Inet4Address.getLocalHost().getHostName();
			socketAccept.bind(new InetSocketAddress(hostName, acceptPort));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void serverEvent(final ServerEvent event) {
		System.out.println("State > " + state);
		switch(state) {
		case IDLE:
			setState(ServerState.ACTIVE);
			break;
		case ACTIVE:
			if (active) {
				setState(ServerState.CONNECTED);
			}
			else {
				setState(ServerState.IDLE);
			}
			break;
		case CONNECTED:
			if (UdpBroadcaster.isBeaconing()) {
				System.out.println("CONNECTED & beaconing!");
			}
			else {
				setState(ServerState.ACTIVE);
			}
			break;
		default:
			System.out.println("State ERROR");
			break;
		}
		System.out.println("State < " + state);
	}

	/**
	 * Pulse UDP broadcast with increment byte[] data
	 */
	public static void startUDPBroadcast() {
		serverEvent(ServerEvent.BROADCASTING_START);
		new Thread(new UdpBroadcaster()).start();
	}

	public static void udpBroadcastStopped() {
		serverEvent(ServerEvent.BROADCASTING_STOP);
	}

	public static void startChatServer() {
		serverThread = new Thread(ChatServer.getInstance());
		serverThread.start();
	}

	private static void stopChatServer() {
		if (UdpBroadcaster.isBeaconing()) {
			UdpBroadcaster.stop();
		}

		try {
			serverThread.interrupt();
			socketAccept.close();
			ChatGui.setClientCount(0);
			transmitting = false;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		System.out.println("Starting server");

		if (socketAccept.isClosed()) {
			newSocketAccept();
		}

		boolean accepting = true;
		while (accepting) {
			try {
				System.out.println("Waiting to accept...");
				serverEvent(ServerEvent.ACCEPT_OPEN); // idle > active
				Socket client = socketAccept.accept();
				if (!clientPool.isShutdown()) {
					clientPool = Executors.newFixedThreadPool(clientPoolSize);
				}
				System.out.println("S: accepted " + client.getPort());
				clientPool.execute(new ClientConnectionHandler(client));
			} catch (IOException e) {
				e.printStackTrace();
				accepting = false;
				clientPool.shutdown();
			}
		}
		serverEvent(ServerEvent.ACCEPT_SHUT);
	}

	private class ClientConnectionHandler implements Runnable {

		public ClientConnectionHandler(Socket acceptSocket) {
			clientSockets.add(acceptSocket);
			ChatGui.setClientCount(clientSockets.size());
		}

		@Override
		public void run() {
			serverEvent(ServerEvent.CLIENT_CONNECT);

			boolean transmitting = true;
			while (transmitting) {
				for (Socket client : clientSockets) {
					PrintStream outStream = null;
					try {
						outStream = new PrintStream(client.getOutputStream());
					} catch (IOException e) {
						e.printStackTrace();
					}

					long now = System.currentTimeMillis(); 
					System.out.println("SERVER> " + client.getPort() + " : " + msToHMS(now));
					outStream.println(now);
				}
				try {
					Thread.sleep(4000L);
				} catch (InterruptedException e) {
					e.printStackTrace();
					transmitting = false;
				}
			}

			closeClientSockets();
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
			e.printStackTrace();
			pool.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}

	public void closeClientSockets() {
		for (Iterator<Socket> it = clientSockets.iterator() ; it.hasNext() ; ) {
			Socket client = it.next();
			try {
				client.close();
				System.out.println("Sever closing client: " + client.getPort());
				it.remove();
			} catch (IOException e) {
				e.printStackTrace();
			}
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
		return (hostName);
	}

	public void handleGuiServerEvent() {
		switch(state) {
		case IDLE: 
			startChatServer();
			break;
		case ACTIVE:
		case CONNECTED:
			stopChatServer();
			break;
		default:
			// ERROR
			break;
		}
	}

	public void handleGuiUdpEvent() {
		if (!active) {
			// Needs server connection or does nothing
			System.out.println("SERVER !connected");
			return;
		}

		if (!UdpBroadcaster.isBeaconing()) {
			startUDPBroadcast();
		}
		else {
			stopUDPBroadcast();
		}
	}

	private void stopUDPBroadcast() {
		UdpBroadcaster.stop();
	}

	public ServerState getState() {
		return(state);
	}

	private String msToHMS(long ms) {
		String hms = String.format("%02d:%02d:%02d", 
				TimeUnit.MILLISECONDS.toHours(ms)%24,
				TimeUnit.MILLISECONDS.toMinutes(ms) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(ms)),
				TimeUnit.MILLISECONDS.toSeconds(ms) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(ms)));
		return(hms);
	}

	public static int countClients() {
		return(clientSockets.size());
	}		

	private static void setState(final ChatCommon.ServerState inState) {
		ChatServer.state = inState;
		switch(inState) {
		case IDLE:
			ChatGui.setServerState(Color.RED);
			break;
		case ACTIVE:
			ChatGui.setServerState(Color.YELLOW);
			break;
		case CONNECTED:
			ChatGui.setServerState(Color.GREEN);
			break;
		default:
			break;
		}
	}
	
	public static int getClientPoolSize() {
		return(clientPoolSize);
	}
}

