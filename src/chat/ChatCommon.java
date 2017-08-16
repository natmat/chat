package chat;

<<<<<<< HEAD
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;

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
	
	public static  InetAddress getBroadcastAddress() {
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

		// Return first (or any) BcastAddr
		InetAddress broadcastAddress = null;
		if (null != listOfBroadcasts.iterator()) {
			broadcastAddress = listOfBroadcasts.iterator().next();
		}
		
		System.out.println("bcastAddr=" + broadcastAddress);
		return(broadcastAddress);
	}
}

=======
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
>>>>>>> branch 'master' of https://github.com/natmat/chat.git
