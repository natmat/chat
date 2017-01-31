package chat;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.sun.org.apache.xml.internal.utils.SystemIDResolver;

public class ChatServer implements Runnable {

	private static ServerSocket chatServer = null;
	private final ExecutorService clientPool;
	private final static int clientPoolSize = 1;
	private static ChatServer instance = null;
	private static ArrayList<Socket> clientSockets;
	
	public final static int acceptPort = 8304;

	public static void main(String[] args) throws IOException {
		instance = new ChatServer();
		startChatServer();
	}

	public ChatServer() throws IOException {
		chatServer = new ServerSocket(acceptPort);
		clientPool = Executors.newFixedThreadPool(clientPoolSize);
		clientSockets = new ArrayList<>(clientPoolSize);
	}
	
	public static void startChatServer() {
		if (null == instance) {
			try {
				instance = new ChatServer();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		new Thread(instance).start();
	}
	
	@Override
	public void run() {
		System.out.println("Running server");
		for(;;) {
			try {
				clientPool.execute(new Handler(chatServer.accept()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				clientPool.shutdown();
			}
			
		}
	}

	private class Handler implements Runnable {

		public Handler(Socket acceptSocket) {
			System.out.println("Accepted: " + acceptSocket);
			clientSockets.add(acceptSocket);
		}

		@Override
		public void run() {
			int quit = 10;
			BufferedWriter outputBW = null;
			boolean running = true;
			Random toss = new Random();
			while (running && (quit-- > 0)) {
				for (Socket client : clientSockets) {
					try {
						String msg;
						outputBW = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
						if (0 == toss.nextInt(5)) {
							msg = "QUIT";
							running = false;
						}
						else {
							msg = Long.toString(System.currentTimeMillis());
						}
						outputBW.write(msg);
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

	public static String getInetAddress() {
		return chatServer.getInetAddress().toString();
	}
}
