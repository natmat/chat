package chat;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.print.attribute.standard.PrinterState;

public class ChatServer implements Runnable {

	private static ServerSocket chatServer = null;
	private final ExecutorService clientPool;
	private final static int clientPoolSize = 1;
	private static ArrayList<Socket> clientSockets;
	private static ChatServer instance = null;
	private static String hostAddr;

	public final static int acceptPort = 8304;

	public static void main(String[] args) throws IOException {
		instance = ChatServer.getInstance();
		startChatServer();
	}

	static void displayInterfaceInformation(NetworkInterface netint) throws SocketException {
		System.out.printf("Display name: %s\n", netint.getDisplayName());
		System.out.printf("Name: %s\n", netint.getName());
		Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
		for (InetAddress inetAddress : Collections.list(inetAddresses)) {
			System.out.printf("InetAddress: %s\n", inetAddress);
		}
		System.out.printf("\n");
	}

	public static ChatServer getInstance() {
		if (null == instance) {
			instance = new ChatServer();
		}
		return(instance);
	}

	private ChatServer() {
		try {
			chatServer = new ServerSocket();
			chatServer.bind(new InetSocketAddress("localhost", acceptPort));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		clientPool = Executors.newFixedThreadPool(clientPoolSize);
		clientSockets = new ArrayList<>(clientPoolSize);
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
				Socket client = chatServer.accept(); 
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
			int quit = 10;
			boolean running = true;
			Random toss = new Random();
			while (running && (quit-- > 0)) {
				for (Socket client : clientSockets) {
					String msg;
//					if (0 == toss.nextInt(5)) {
//						msg = "QUIT";
//						running = false;
//					}
//					else {
						msg = Long.toString(System.currentTimeMillis());
//					}
					try {
						PrintStream outStream = new PrintStream(client.getOutputStream());
						System.out.println("Out: " + msg);
						outStream.println(msg);
					} catch (IOException e) {
						e.printStackTrace();
					}
					try {
						Thread.sleep((long)(Math.random()*1000));
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
		return(acceptPort);
	}

	/**
	 * Returns the IP address string in textual presentation.
	 * @return host address
	 */
	public static String getHostAddress() {
		return(hostAddr);
	}
}
