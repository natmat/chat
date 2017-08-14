package chat;

public class ChatCommon {
	
	public static final String CONNECTION_REFUSED = "CONNECTION REFUSED";
	public static final String SOCKET_CLOSED = "SOCKET CLOSED";
	public static final String CONNECTION_RESET = "CONNECTION RESET";
	public static final String SLEEP_INTERRUPTED = "sleep interrupted";
	
	public enum ServerState {
		IDLE, 
		ACTIVE,
		CONNECTED
	}
	
	public enum ClientState {
		IDLE, 
		WAITING,
		CONNECTED
	}
	
	public enum ServerEvent {
		ACCEPT_OPEN, 
		ACCEPT_CLOSED,
		BEACONING, 
		NOT_BEACONING,
		BROADCASTING_START, 
		BROADCASTING_STOP, 
		CLIENT_CONNECT,
		CLIENT_DISCONNECT
	}
	
	public enum ClientEvent {
		CONNECTION_RESET, 
		WAITING,
		READING, 
		CONNECTED
	}
}