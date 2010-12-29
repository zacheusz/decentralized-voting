package runtime.executor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import protocol.communication.Message;
import runtime.NetworkSend;

public class E_NetworkSend implements NetworkSend {

	private final static int STIMEOUT = 2000; // socket connection timeout

	private DatagramSocket udpSocket;

	public E_NetworkSend() {
		try {
			udpSocket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void sendTCP(Message msg) throws UnknownHostException, IOException {
		// // ByteArrayOutputStream baos = new ByteArrayOutputStream();
		// // ObjectOutputStream oos = new ObjectOutputStream(baos);
		// // oos.writeObject(msg);
		// // oos.flush();
		// // byte[] bytes = baos.toByteArray();
		//
		// Node.nbMsgSent++;
		// // Node.nbBytesSent += bytes.length;
		//
		// // Receiver
		// E_NodeID id = (E_NodeID) msg.getDest();
		// Socket sc = new Socket();
		// // try {
		// InetSocketAddress isc = new
		// InetSocketAddress(InetAddress.getByName(id
		// .getName()), id.getPort());
		// sc.connect(isc, STIMEOUT);
		// // } catch (Exception e) {
		// // // System.out.println(msg.src + ":" + msg.dest
		// // // + " exception (in sendTCP) : " + e.getMessage());
		// // throw e;
		// // // e.printStackTrace();
		// // }
		// // Serialize msg
		// ObjectOutputStream brtClient = new ObjectOutputStream(sc
		// .getOutputStream());
		// brtClient.writeObject(msg);
		// // Send
		// // System.out.println("E_NetworkSend.sendTCP() trying to send
		// // data");
		// brtClient.flush();
		// brtClient.close();
		// sc.close();
		new sendTCPThread(msg).execute();
	}

	public void sendUDP(Message msg) throws UnknownHostException, IOException {

		// Receiver
		E_CryptoNodeID id =   msg.getDest();
		InetAddress client = Inet4Address.getByName(id.getName());
		// Serialize msg
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(msg);
		oos.flush();
		byte[] bytes = baos.toByteArray();
		try {
			// Node.nbMsgSent++;
			// Node.nbBytesSent += bytes.length;

			// if (bytes.length > 64000)
			// {
			// || msg.getHeader() == msg.BRIEF){
			// more than 64k should be via TCP
			// sendTCP(msg);
			// }
			// else
			// {
			// Send max 64k bytes via UDP
			// System.out.println("E_NetworkSend.sendUDP() trying to send
			// data
			// of size:"+bytes.length);
			if (bytes.length > 65000) {
				System.out
						.println("#######################\nE_NetworkSend.sendUDP() trying to send data of size:"
								+ bytes.length + "\n#######################");
			}
			DatagramPacket packet = new DatagramPacket(bytes, bytes.length,
					client, id.getPort());
			udpSocket.send(packet);
			oos.close();
			// }
		} finally {
			oos.flush();
			oos.close();
		}
	}

	private class sendTCPThread extends Thread {
		Message msg = null;

		public sendTCPThread(Message msg) {
			this.msg = msg;
		}

		public void execute() throws UnknownHostException, IOException {
			// Node.nbMsgSent++;
			E_CryptoNodeID id =  msg.getDest();
			Socket sc = new Socket();
			try {
				InetSocketAddress isc = new InetSocketAddress(InetAddress
						.getByName(id.getName()), id.getPort());
				sc.connect(isc, STIMEOUT);
				ObjectOutputStream brtClient = new ObjectOutputStream(sc
						.getOutputStream());
				brtClient.writeObject(msg);
				brtClient.flush();
				brtClient.close();
				sc.close();
			} finally {
				sc.close();
			}
		}
	}
}