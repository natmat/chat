package chat;

public class ChatCommon {
	
	final static String connectionReset = "CONNECTION_RESET";
	
	public enum ServerState {
		IDLE, 
		ACTIVE,
		CONNECTED
	}
	
	public enum ServerEvent {
		ACCEPT_SHUT, 
		BROADCASTING_START, 
		BROADCASTING_STOP, 
		ACCEPT_OPEN, 
		CLIENT_CONNECT
	}
	
	public enum ClientEvent {
		CONNECTION_RESET
	}
}
