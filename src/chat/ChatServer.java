package chat;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import chat.ChatCommon.ServerEvent;
import chat.ChatCommon.ServerState;

public class ChatServer implements Runnable {

	private static ServerSocket socketAccept = null;
	private static ExecutorService clientPool;
	public static boolean transmitting;
	private static String hostName;
	private final static int clientPoolSize = 10;
	private static ArrayList<Socket> clientSockets;
	private static ChatServer instance = null; // Singleton
	public final static int acceptPort = 8304;

	static ServerState state;
	private static Thread serverThread;
	private static DatagramSocket zigbeeSocket;
	private static Thread macFrameThread;

	public static void main(String[] args) throws IOException {
		instance = ChatServer.getInstance();

		startChatServer();
		startUDPBroadcast();

		exitTimeout(3600);
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
		System.out.println("State >  " + state);
		switch(state) {
		case IDLE:
			setState(ServerState.ACTIVE);
			break;

		case ACTIVE:
			switch(event) {
			case CLIENT_CONNECT:
				setState(ServerState.CONNECTED);				
				break;
			case ACCEPT_CLOSED:
				setState(ServerState.IDLE);				
				break;
			default:
				break;
			}
			break;

		case CONNECTED:
			switch(event) {
			case ACCEPT_CLOSED:
				setState(ServerState.IDLE);
				break;
			default:
				break;
			}
			break;			

		default:
			System.out.println("State ERROR");
			break;
		}
		System.out.println("State >> " + state + "\n");
	}

	public static class MacFrameRunnable implements Runnable {

		@Override
		public void run() {
			byte sn = 0x01;
			
			try {
				zigbeeSocket = new DatagramSocket(8305);
				zigbeeSocket.setBroadcast(true);
			} catch (SocketException e1) {
				e1.printStackTrace();
			}

			while(!Thread.interrupted())
			{
				System.out.println("MACFrame >> " + sn);
				ChatServer.writeMACFrame(sn++);

				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					System.out.println(e.getMessage());
					if (!e.getMessage().equals(ChatCommon.SLEEP_INTERRUPTED)) {
						e.printStackTrace();
					}
					break;
				}
			}
			zigbeeSocket.close();
		}
	}

	public static void eventMAC() {
		if ((null != macFrameThread) && (macFrameThread.isAlive())) {
			System.out.println("Interrupt macFrameThread");
			macFrameThread.interrupt();
			return;
		}		
		
		macFrameThread = new Thread(new MacFrameRunnable());
		macFrameThread.start();
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
			//			serverThread.interrupt();
			socketAccept.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		System.out.println("S: started");

		if (socketAccept.isClosed()) {
			newSocketAccept();
		}
		serverEvent(ServerEvent.ACCEPT_OPEN); // idle > active

		boolean accepting = true;
		while (accepting) {
			try {
				System.out.println("S: waiting to accept");
				Socket client = socketAccept.accept();
				clientPool = Executors.newFixedThreadPool(clientPoolSize);
				clientPool.execute(new ClientConnectionHandler(client));
			} catch (IOException e) {
				System.out.println("Server running E: " + e.getMessage());
				if (!e.getMessage().toUpperCase().equals(ChatCommon.SOCKET_CLOSED)) {
					e.printStackTrace();
				}				

				accepting = false;
				shutdownAndAwaitTermination(clientPool);
			}
		}
		serverEvent(ServerEvent.ACCEPT_CLOSED);
		ChatGui.setClientCount(0);
	}

	private class ClientConnectionHandler implements Runnable {
		private Socket client;

		public ClientConnectionHandler(Socket newClient) {
			System.out.println("\nS: new client " + newClient.getLocalPort() + ">" + newClient.getPort());
			this.client = newClient;

			clientSockets.add(newClient);
			ChatGui.setClientCount(clientSockets.size());
		}

		@Override
		public void run() {
			serverEvent(ServerEvent.CLIENT_CONNECT);

			boolean transmitting = true;
			while (transmitting) {
				PrintStream outStream = null;
				try {
					outStream = new PrintStream(client.getOutputStream());
				} catch (IOException e) {
					e.printStackTrace();
				}

				String now = String.valueOf(System.currentTimeMillis()/1E6);

				System.out.println("S: " + now + " > " + client.getPort() );
				outStream.println(now);

				try {
					Thread.sleep(5000L);
				} catch (InterruptedException e) {
					if (!e.getMessage().equals(ChatCommon.SLEEP_INTERRUPTED)) {
						e.printStackTrace();
					}
					transmitting = false;
				}
			}

			try {
				System.out.println("S close client: " + client.getPort());
				client.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static void shutdownAndAwaitTermination(ExecutorService pool) {
		pool.shutdown();
		try {
			while (!pool.isShutdown()) {
				if (!pool.awaitTermination(1000, TimeUnit.MILLISECONDS)) {
					System.err.println("Pool awaitTerminatation...");
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		pool.shutdownNow();
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

	public void handleGUIUDPEvent() {
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

	@SuppressWarnings("unused")
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

	public static void writeMACFrame(byte seq) {
		// Write MAC frame to socket
		MACFrame frame = new MACFrame(seq);
		ByteArrayOutputStream baos = frame.getData();

		InetAddress broadcastAddress = ChatCommon.getBroadcastAddress();
		if (null == broadcastAddress) {
			System.out.println("ERROR: broadcastAddress null");
			return;
		}

		DatagramPacket macFramePacket = new DatagramPacket(
				baos.toByteArray(), baos.toByteArray().length, broadcastAddress, 8305);
		try {
			zigbeeSocket.send(macFramePacket);
			baos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	private static final int byteArrayToInt(final Byte[] inArray) {
		int result = 0;
		int shift = 0;
		for (int i = 0 ; i < inArray.length ; i++) {
			result += (inArray[i] << shift);
			shift += 8;
		}
		return result;
	}

	// Writes provided 4-byte integer to a 4 element byte array in Little-Endian order.
	public static final byte[] intTo4ByteArray(final int value) {
		return new byte[] {
				(byte)(value & 0xff),
				(byte)(value >> 8 & 0xff),
				(byte)(value >> 16 & 0xff),
				(byte)(value >>> 24)
		};
	}

	public void handleGuiMACEvent() {
		eventMAC();
	}
}

