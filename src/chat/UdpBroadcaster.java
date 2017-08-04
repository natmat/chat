package chat;

import java.awt.Color;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;

final public class UdpBroadcaster implements Runnable {
	private static boolean isBroadcasting = false;
	
	@Override
	public void run() {
		if (isBroadcasting) {
			return;
		}
		
		System.out.println("startUDPBroadcast");
		isBroadcasting = true;
		
		InetAddress broadcastAddress = getBroadcastAddress();
		if (null == broadcastAddress) {
			System.out.println("ERROR: broadcastAddress null");
			return;
		}

		DatagramSocket broadcastSocket = null;
		try {
			broadcastSocket = new DatagramSocket(8300);
			broadcastSocket.setBroadcast(true);
		} catch (SocketException e1) {
			e1.printStackTrace();
		}

		int i = 1;
		while (isBroadcasting) {
			byte[] buf = { (byte) i, (byte) i, (byte) i, (byte) i };
			i++;
			DatagramPacket packet = new DatagramPacket(buf, buf.length, broadcastAddress, 8304);
			try {
				System.out.println("packet " + i + ":" + Arrays.toString(packet.getData()));
				broadcastSocket.send(packet);
				Thread.sleep(1000);
			} catch (InterruptedException | IOException e) {
				e.printStackTrace();
			}
		}
		broadcastSocket.close();
		ChatGui.setUdpState(Color.RED);
	}
	
	private static InetAddress getBroadcastAddress() {
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
							//							return(broadcast);
						}
					}
				}
			}
		} catch (SocketException ex) {
			System.err.println("Error while getting network interfaces");
			ex.printStackTrace();
		}

		InetAddress ba = null;
		if (null != listOfBroadcasts.iterator()) {
			ba = listOfBroadcasts.iterator().next();
		}
		return(ba);
	}

	public static void stop() {
		if (!isBroadcasting) {
			System.out.println("ERROR: not braoadcasting");
		}
		isBroadcasting = false;
	}
}
