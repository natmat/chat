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

import chat.ChatStates.ServerState;

public class ChatServer implements Runnable {

	private static ServerSocket socketAccept = null;
	private ExecutorService clientPool;
	public static boolean transmitting;
	private static String hostName;
	private final static int clientPoolSize = 2;
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
					// TODO Auto-generated catch block
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

	private static void updateState() {
		System.out.println("State > " + state);
		switch(state) {
		// comment
		case IDLE:
			state = ServerState.ACTIVE;
			ChatGui.setServerState(Color.YELLOW);
			break;
		case ACTIVE:
			if (active) {
				state = ServerState.CONNECTED;
				ChatGui.setServerState(Color.GREEN);
			}
			else {
				state = ServerState.IDLE;
				ChatGui.setServerState(Color.RED);
			}
			break;
		case CONNECTED:
			if (UdpBroadcaster.isBeaconing()) {
				System.out.println("CONNECTED & beaconing!");
			}
			else {
				state = ServerState.ACTIVE;
				ChatGui.setServerState(Color.YELLOW);
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
		updateState();
		new Thread(new UdpBroadcaster()).start();
	}

	public static void udpBroadcastStopped() {
		updateState();
	}

	public static void startChatServer() {
		if ((null != serverThread) && serverThread.isAlive()) {
			System.out.println("ERROR server alive");
			return;
		}

		serverThread = new Thread(ChatServer.getInstance());
		serverThread.start();
	}

	private static void stopChatServer() {
		if (UdpBroadcaster.isBeaconing()) {
			UdpBroadcaster.stop();
		}

		try {
			socketAccept.close();
			transmitting = false;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		System.out.println("Starting server");
		active = true;
		updateState();

		if (socketAccept.isClosed()) {
			newSocketAccept();
		}

		while (active) {
			try {
				System.out.println("Waiting to accept...");
				Socket client = socketAccept.accept();
				if (!clientPool.isShutdown()) {
					clientPool = Executors.newFixedThreadPool(clientPoolSize);
				}
				clientPool.execute(new ClientConnectionHandler(client));
			} catch (IOException e) {
				System.out.println("socketAccept closed: " + e.getMessage());
				active = false;
				clientPool.shutdown();
			}
		}
		updateState();
	}

	private class ClientConnectionHandler implements Runnable {

		public ClientConnectionHandler(Socket acceptSocket) {
			System.out.println("Handler socket: " + acceptSocket);
			clientSockets.add(acceptSocket);
		}

		@Override
		public void run() {
			System.out.println("Accepted...");
			active = true;
			updateState();

			transmitting = true;
			while (transmitting) {
				for (Socket client : clientSockets) {
					PrintStream outStream = null;
					try {
						outStream = new PrintStream(client.getOutputStream());
					} catch (IOException e) {
						e.printStackTrace();
					}

					System.out.print("Server> ");
					String hms = msToHMS(System.currentTimeMillis());
					System.out.println(hms);
					outStream.println(client.getPort() +":" + hms);
				}
				try {
					Thread.sleep(1000L);
				} catch (InterruptedException e) {
					e.printStackTrace();
					transmitting = false;
				}
			}

			closeClientSocket();
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

	public void closeClientSocket() {
		for (Iterator<Socket> it = clientSockets.iterator() ; it.hasNext() ; ) {
			Socket client = it.next();
			try {
				client.close();
				it.remove();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static int getAcceptPort() {
		System.out.println("SERVER: acceptPort=" + acceptPort);
		return (acceptPort);
	}

	/**
	 * Returns the IP address string in textual presentation.
	 * 
	 * @return host address
	 */
	public static String getHostName() {
		System.out.println("SERVER: hostName=" + hostName);
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
}
