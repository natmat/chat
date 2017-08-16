package chat;

import java.awt.Color;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;

final public class UdpBroadcaster implements Runnable {
	private static boolean beaconing = false;
	
	@Override
	public void run() {
		if (beaconing) {
			return;
		}
		
		System.out.println("startUDPBroadcast");
		beaconing = true;
		
		InetAddress broadcastAddress = ChatCommon.getBroadcastAddress();
		if (null == broadcastAddress) {
			System.out.println("ERROR: broadcastAddress null");
			beaconing = false;
			return;
		}

		DatagramSocket broadcastSocket = null;
		try {
			broadcastSocket = new DatagramSocket(8300);
			broadcastSocket.setBroadcast(true);
		} catch (SocketException e) {
			System.out.println("ERROR no braodcast socket");
			e.printStackTrace();
			beaconing = false;
			return;
		}

		ChatGui.setUdpState(Color.GREEN);
		int i = 1;
		while (beaconing) {
			byte[] buf = { (byte) i, (byte) i, (byte) i, (byte) i };
			i++;
			DatagramPacket packet = new DatagramPacket(buf, buf.length, broadcastAddress, 8304);
			try {
				System.out.println("packet " + i + ":" + Arrays.toString(packet.getData()));
				broadcastSocket.send(packet);
				Thread.sleep(1000);
			} catch (InterruptedException | IOException e) {
				e.printStackTrace();
				beaconing = false;
			}
		}		
		broadcastSocket.close();
		ChatGui.setUdpState(Color.RED);
		ChatServer.udpBroadcastStopped();
	}

	public static void stop() {
		if (!beaconing) {
			System.out.println("ERROR: not braoadcasting");
		}
		beaconing = false;
	}

	public static boolean isBeaconing() {
		return(beaconing);
	}
}
