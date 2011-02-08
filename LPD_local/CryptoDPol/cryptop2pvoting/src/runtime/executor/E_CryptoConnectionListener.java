package runtime.executor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;

import protocol.communication.Message;
import protocol.node.CryptoNode;
import runtime.Receiver;
import runtime.Task;
import runtime.TaskManager;

public class E_CryptoConnectionListener implements Runnable {

	private int port;

	private Receiver r;

	private TaskManager tm;

	public E_CryptoConnectionListener(int port, TaskManager tm, Receiver r) {
		this.port = port;
		this.tm = tm;
		this.r = r;
	}

	public void run() {
		// Start TCP
		new Thread(new TCPListener()).start();

		// Start UDP
		new Thread(new UDPListener()).start();
	}

	private class UDPListener implements Runnable {
		public void run() {
			try {

				try {
					DatagramSocket socket = new DatagramSocket(port);
					while (true) {
						byte[] buffer = new byte[0];
						try {
							buffer = new byte[80000];
						} catch (Throwable t) {
							System.out.println(((CryptoNode) r).toString()
									+ " : " + t.getMessage());
							t.printStackTrace();
						}
						// Receive UDP from socket
						DatagramPacket packet = new DatagramPacket(buffer,
								buffer.length);
						socket.receive(packet);
						// System.out.println(" E_ConnectionListener(UDP)
						// received data of size: " + packet.getLength() + " at
						// " + System.currentTimeMillis());
						// Store for later use
						tm.registerTask(new MessageReceiver(packet));
						// new MessageReceiver(packet).execute();
					}
				} catch (java.net.BindException b) {
					System.out.println(r + ": UDP node manager stopped, "
							+ b.getMessage());
					System.exit(0);
				}
			} catch (IOException e) {
				System.err.println(e);
			}
		}
	}

	private class TCPListener implements Runnable {
		public void run() {
			ServerSocket ss = null;
			try {
				ss = new ServerSocket(port);
				while (true) {
					Socket cs = ss.accept();
					// tm.registerTask(new MessageReceiver(cs));
					new TCPThreadMessageReceiver(cs).execute();
					// new MessageReceiver(cs).execute();
					// System.out.println(" E_ConnectionListener(TCP) received
					// data at " + System.currentTimeMillis());
					// No need to sleep. ss.accept is blocking
					// Thread.sleep(100);
				}
			} catch (Exception e) {
				System.out.println(r + ": TCP node manager stopped, "
						+ e.getMessage());
				System.exit(0);
			} finally {
				try {
					ss.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public class MessageReceiver implements Task { // or extends Thread
		private Socket cs;

		private DatagramPacket packet;

		public MessageReceiver(Socket cs) {
			this.cs = cs;
		}

		public MessageReceiver(DatagramPacket packet) {
			this.packet = packet;
		}

		public void execute() {
			try {
				Message msg = null;
				if (this.cs != null) {
					// TCP
					ObjectInputStream in = new ObjectInputStream(cs
							.getInputStream());
					msg = (Message) in.readObject();
					cs.close();

					// ByteArrayOutputStream baos = new ByteArrayOutputStream();
					// ObjectOutputStream oos = new ObjectOutputStream(baos);
					// oos.writeObject(msg);
					// oos.flush();
					// byte[] bytes = baos.toByteArray();
					//
					// Node.nbMsgReceived++;
					// Node.nbBytesReceived += bytes.length;
				} else if (this.packet != null) {
					// UDP
					ByteArrayInputStream bais = new ByteArrayInputStream(packet
							.getData());
					ObjectInputStream in = new ObjectInputStream(bais);
					msg = (Message) in.readObject();

					// ByteArrayOutputStream baos = new ByteArrayOutputStream();
					// ObjectOutputStream oos = new ObjectOutputStream(baos);
					// oos.writeObject(msg);
					// oos.flush();
					// byte[] bytes = baos.toByteArray();

					// Node.nbMsgReceived++;
					// Node.nbBytesReceived += bytes.length;
				}
				// Propagate
				if (msg != null) {
					r.receive(msg);
				}
			} catch (IOException e) {
				System.out.println("IOException receiving message..."
						+ e.getMessage());
				e.printStackTrace();
				// System.exit(0);
			} catch (ClassNotFoundException cnfe) {
                            System.out.println("Message Receiver");
				System.out.println("ClassNotFoundException  receiving message 1..."
								+ cnfe.getMessage());
			}
		}
	}

	public class TCPThreadMessageReceiver extends Thread {
		private Socket cs;

		public TCPThreadMessageReceiver(Socket cs) {
			this.cs = cs;
		}

		public void execute() {
			try {
				Message msg = null;
				// TCP
				ObjectInputStream in = new ObjectInputStream(cs
						.getInputStream());
				msg = (Message) in.readObject();
				cs.close();
				// Propagate
				if (msg != null) {
					r.receive(msg);
				}
			} catch (IOException e) {
				System.out.println("IOException receiving message..."
						+ e.getMessage());
				// e.printStackTrace();
				// System.exit(0);
			} catch (ClassNotFoundException cnfe) {
                            System.out.println("TCP Thread Receiver");
				System.out.println("ClassNotFoundException  receiving message 2..."
								+ cnfe.getMessage());
			}
		}
	}
}
