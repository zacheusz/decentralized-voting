package runtime.simulator;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import protocol.communication.Message;
import runtime.NetworkSend;
import runtime.NodeID;
import runtime.NodeIDReader;
import runtime.Receiver;
import runtime.Stopper;
import runtime.Task;
import runtime.TaskManager;
import runtime.Time;

public class S_Simulator implements NetworkSend, TaskManager, NodeIDReader,
		Time, Stopper {
	LinkedList<LinkedList<Task>> tasks = new LinkedList<LinkedList<Task>>();
	LinkedList<LinkedList<Message>> messages = new LinkedList<LinkedList<Message>>();

	ArrayList<Receiver> nodes = new ArrayList<Receiver>();
	ArrayList<Receiver> stoppedNodes = new ArrayList<Receiver>();

	int cycle = 0;
	public boolean activeNodes = true;
	public Thread thread;

	public void sendUDP(Message msg) throws UnknownHostException, IOException {
		Message copy = msg.copy();
		LinkedList<Message> l;
		if (messages.isEmpty()) {
			l = new LinkedList<Message>();
			messages.add(l);
		} else {
			l = messages.get(0);
		}
		l.add(copy);
		// System.out.println("Added "+copy.getHeader()+" for "+copy.dest+ " to
		// message queue");
	}

	public void registerTask(Task t, long timeout) {
		// System.out.println("Register: "+t.getClass() + " in "+timeout);
		if (timeout <= 0) {
			registerTask(t);
		} else {
			int nbCycles = (int) timeout;

			LinkedList<Task> l;
			for (int i = tasks.size(); i < nbCycles; i++) {
				l = new LinkedList<Task>();
				tasks.addLast(l);
			}
			l = tasks.get(nbCycles - 1);
			l.add(t);
		}
	}

	public void registerTask(Task t, long timeout, long period) {
		System.err.println("This method is not yet implemented");
		System.exit(-1);
	}

	public void registerTask(Task t) {
		// System.out.println("Register: "+t.getClass());
		LinkedList<Task> l;
		if (tasks.isEmpty()) {
			l = new LinkedList<Task>();
			tasks.add(l);
		} else {
			l = tasks.get(0);
		}
		l.add(t);
	}

	public void addNode(Receiver n) {
		nodes.add(n);
	}

	public void start() {
		thread = new Thread(new SimulatorThread());
		thread.start();
	}

	public NodeID readBootstrapNodeID(String str) {
		String[] data = str.split(" ");
		if (data.length < 1) {
			System.err.println("error!");
			System.exit(1);
		}
		S_NodeID id = new S_NodeID(Integer.parseInt(data[0]), Integer
				.parseInt(data[1]));
		return id;
	}

	@Override
	// Never used
	public NodeID readNodeID(String str, int port) {
		S_NodeID id = new S_NodeID(Integer.parseInt(str), -1);
		return id;
	}

	// public int getNodeIDSize() {
	// return 1;
	// }

	private class SimulatorThread implements Runnable {
		public void run() {
			while (activeNodes) {
				// We need to remove tasks and messages at the beginning of the
				// cycle
				Iterator<Task> tasksIterator = null;
				Iterator<Message> messageIterator = null;
				if (!tasks.isEmpty()) {
					// System.out.println("*************************************");
					// System.out.println("* STARTING TASK CYCLE " + cycle);
					// System.out.println("*************************************");
					tasksIterator = tasks.removeFirst().iterator();
				}
				if (!messages.isEmpty()) {
					// System.out.println("*************************************");
					// System.out.println("* STARTING MESSAGE CYCLE " + cycle);
					// System.out.println("*************************************");
					messageIterator = messages.removeFirst().iterator();
				}
				if (tasksIterator != null) {
					while (tasksIterator.hasNext()) {
						tasksIterator.next().execute();
					}
				}
				if (messageIterator != null) {
					while (messageIterator.hasNext()) {
						Message m = messageIterator.next();
						// System.out.println("Sending "+m.getHeader()+" for
						// "+m.dest+ " tonode");
						nodes.get(((S_NodeID) m.getDest()).id).receive(m);
					}
				}
				cycle++;
				// System.out.println(".");
			}
		}
	}

	public long getCurrentTime() {
		return cycle;
	}

	public void stopNode(Receiver node) {
		if (stoppedNodes.contains(node)) {
			System.err.println("Error! The node already stopped");
			System.exit(1);
		}
		stoppedNodes.add(node);
		if (stoppedNodes.size() == nodes.size()) {
			activeNodes = false;
		}
	}

	public void sendTCP(Message msg) throws UnknownHostException, IOException {
		sendUDP(msg);
	}

	public void stopNode(Receiver node, String message) {
		stopNode(node);
	}

	public void stopNode(String message) {
		System.out
				.println("SHOULD ONLY BE USED FOR DEPLOYMENT, NOT SIMULATION!!!");
	}
}
