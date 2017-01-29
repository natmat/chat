package chat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ChatServer implements Runnable {

	private static ServerSocket chatServer = null;
	private final ExecutorService clientPool;
	private final static int clientPoolSize = 10;
	private static ChatServer instance = null;
	
	public final static int acceptPort = 8304;

	public static void main(String[] args) throws IOException {
		instance = new ChatServer();
		startChatServer();
	}

	public ChatServer() throws IOException {
		chatServer = new ServerSocket(acceptPort);
		clientPool = Executors.newFixedThreadPool(clientPoolSize);
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
			new ChatClient(acceptSocket);
		}

		@Override
		public void run() {
			// Read and service request on socket
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
}
