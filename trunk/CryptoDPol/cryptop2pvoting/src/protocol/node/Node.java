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
import runtime.Receiver;
import runtime.Task;
import runtime.executor.E_CryptoNodeID;

public abstract class Node implements Receiver {

	// Constants
	public static PrintStream out = null;
	public static final int VIEW_SIZE = Integer.MAX_VALUE;
	public static final int NB_BALLOTS = 1;
	protected static final int SELF_DESTRUCT_DELAY = 12 * 60 * 1000;	// Maximum duration of the simulation: 8 minutes
	protected long startInstant=0;
        protected long endInstant=0;
        protected long runningTime=0;
	// Fields
	protected final E_CryptoNodeID nodeId;
	protected final NetworkSend networkSend;
	// Mutual exclusion
	protected final Object LOCK = new Object();
	
	public Node(E_CryptoNodeID nodeId, NetworkSend networkSend) {
		this.nodeId = nodeId;
		this.networkSend = networkSend;
 
	}
	
	// **************************************************************************
	// Utility methods
	// **************************************************************************
	public static int getGroupId(E_CryptoNodeID id) {
		/*int hash = id.hashCode();
		hash = (hash<0)?-hash:hash;
		return hash % E_CryptoNodeID.NB_GROUPS;*/
               return id.groupId;
	}
	
	public static int getNextGroupId(E_CryptoNodeID id) {
		return (getGroupId(id) + 1) % E_CryptoNodeID.NB_GROUPS;
	}
	
	public static int getPreviousGroupId(E_CryptoNodeID id) {
		return (getGroupId(id) + E_CryptoNodeID.NB_GROUPS - 1) % E_CryptoNodeID.NB_GROUPS;
	}
	
	public int getGroupId() {
		return getGroupId(nodeId);
	}
	
	public int getNextGroupId() {
		return getNextGroupId(nodeId);
	}
	
	public int getPreviousGroupId() {
		return getPreviousGroupId(nodeId);
	}
	
	protected  void printView(Set<E_CryptoNodeID> subView) {
		for(E_CryptoNodeID id: subView) {
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
	//	if (!nodeId.name.contains("06"))
                 return;
		
	/*	String msg = "Node " + nodeId + " (" + getGroupId(nodeId) +  "): " + message;
		if(out != null) {
			synchronized(out) {
				out.println(msg);
				out.flush();
			}
		}
		synchronized(System.out) {
			System.out.println(msg);
		}
*/

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
                        runningTime=endInstant-startInstant;
                      	System.out.println("Running Time: "+runningTime);
			//dump(finalMessage());
			receiveSTOP(new STOP_MSG(nodeId, nodeId, "self destruct"));
		}
	}

}
