package chat;

import java.awt.Color;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import chat.ChatStates.ServerState;

public class ChatServer implements Runnable {

	private static ServerSocket socketAccept = null;
	private final ExecutorService clientPool;
	private static String hostName;
	private final static int clientPoolSize = 1;
	private static ArrayList<Socket> clientSockets;
	private static ChatServer instance = null; // Singleton
	public final static int acceptPort = 8304;

	static ServerState state;
	private static boolean isConnected;
	private static Thread serverThread;

	public static void main(String[] args) throws IOException {
		instance = ChatServer.getInstance();

		startChatServer();
		startUDPBroadcast();

		exitTimeout(1000);
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
		isConnected = false;
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
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		System.out.println("Starting server");
		isConnected = true;
		updateState();
		
		if (socketAccept.isClosed()) {
			newSocketAccept();
		}
		
		while (isConnected) {
			try {
				System.out.println("Waiting to accept...");
				Socket client = socketAccept.accept();
				clientPool.execute(new ClientConnectionHandler(client));
			} catch (IOException e) {
				System.out.println("socketAccept closed");
				isConnected = false;
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
			isConnected = true;
			updateState();

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

					System.out.print("Server> ");
					System.out.println(msToHMS(System.currentTimeMillis()));
					String msString = Long.toString(System.currentTimeMillis());
					outStream.println(msString);

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
		if (!isConnected) {
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
				TimeUnit.MILLISECONDS.toHours(ms),
				TimeUnit.MILLISECONDS.toMinutes(ms) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(ms)),
				TimeUnit.MILLISECONDS.toSeconds(ms) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(ms)));
		return(hms);
	}		
}
