package protocol.node;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Set;

import protocol.communication.Message;
import protocol.communication.STOP_MSG;
import runtime.NetworkSend;
import runtime.NodeID;
import runtime.Receiver;
import runtime.Task;
import runtime.executor.E_NodeID;

public abstract class Node implements Receiver {

	// Constants
	public static PrintStream out = null;
	public static final int VIEW_SIZE = Integer.MAX_VALUE;
//	public static int NB_BALLOTS = 3;
	public static int NB_BALLOTS_MAX = 3; // Maximum BALLOT number
	protected static final int SELF_DESTRUCT_DELAY = 12 * 60 * 1000;	// Maximum duration of the simulation: 8 minutes
	protected long startInstant=0;
        protected long endInstant=0;
        protected long runningTime=0;
	// Fields
	protected final NodeID nodeId;
	protected final NetworkSend networkSend;
	
	// Mutual exclusion
	protected final Object LOCK = new Object();
	
	public Node(NodeID nodeId, NetworkSend networkSend) {
		this.nodeId = nodeId;
		this.networkSend = networkSend;
	}
	
	// **************************************************************************
	// Utility methods
	// **************************************************************************
	public static int getGroupId(NodeID id) {
//		int hash = id.hashCode();
//		hash = (hash<0)?-hash:hash;
//		return hash % NodeID.NB_GROUPS;
                return ((E_NodeID)id).groupId;

	}
	
	public static int getNB_BALLOTS(NodeID id) { 		// This function return NB_BALLOT for a node
		return ((E_NodeID)id).NB_BALLOTS;
	}
	
	public static int getNextGroupId(NodeID id) {
		return (getGroupId(id) + 1) % NodeID.NB_GROUPS;
	}
	
	public static int getPreviousGroupId(NodeID id) {
		return (getGroupId(id) + NodeID.NB_GROUPS - 1) % NodeID.NB_GROUPS;
	}
	
	public int getGroupId() {
		return getGroupId(nodeId);
	}
	
	public int getNB_BALLOTS() {			//	NB_BALLOTS of a node
		return getNB_BALLOTS(nodeId);
	}
	
	public int getNextGroupId() {
		return getNextGroupId(nodeId);
	}
	
	public int getPreviousGroupId() {
		return getPreviousGroupId(nodeId);
	}
	
	protected static void printView(Set<NodeID> subView) {
		for(NodeID id: subView) {
			System.out.print("" + id + " (" + getGroupId(id) + ") ");
		}
		System.out.println();
	}
	
	protected void doSendUDP(Message msg) throws UnknownHostException, IOException {
		try {
			synchronized (LOCK) {
				networkSend.sendUDP(msg);
			}
		} catch (SocketTimeoutException e) {
			dump("UDP: " + nodeId + ":" + msg.getDest() + " might be dead!");
		} catch (ConnectException e) {
			dump("UDP: " + nodeId + ":" + msg.getDest() + " is dead!");
		}
	}

	protected void doSendTCP(Message msg) throws UnknownHostException, IOException {
		try {
			synchronized (LOCK) {
				networkSend.sendTCP(msg);
			}
		} catch (SocketTimeoutException e) {
			dump("TCP: " + nodeId + ":" + msg.getDest() + " might be dead!");
		} catch (ConnectException e) {
			dump("TCP: " + nodeId + ":" + msg.getDest() + " is dead!");
		}
	}
	
	protected void receiveSTOP(STOP_MSG msg) {
		synchronized (LOCK) {
			dump("Received a STOP message from " + msg.getSrc() + " (reason: " + msg.getMessage() + ")");
			if(out != null) {
				synchronized (out) {
					out.close();
				}
			}
			System.exit(0);
		}
	}

	public void dump(String message) {
	//	return;
		String msg = "Node " + nodeId + " (" + getGroupId(nodeId) +  "): " + message;
		if(out != null) {
			synchronized(out) {
				out.println(msg);
				out.flush();
			}
		}
		synchronized(System.out) {
			System.out.println(msg);
		}

	}
	
	public String finalMessage() {
		return "Bye bye";
	}
	
	public String toString() {
		return nodeId.toString();
	}
	
	protected class SelfDestructTask implements Task {
		public void execute() {
			endInstant = (new Date ()).getTime ();
			System.out.println(finalMessage());
                        runningTime=endInstant-startInstant;
                        System.out.println("Running Time: "+runningTime);
			receiveSTOP(new STOP_MSG(nodeId, nodeId, "self destruct"));
		}
	}

}
