package protocol.node;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import protocol.communication.GMAV_MSG;
import protocol.communication.HITV_MSG;
import protocol.communication.IAM_MSG;
import protocol.communication.Message;
import protocol.communication.STOP_MSG;
import runtime.NetworkSend;
import runtime.NodeID;
import runtime.TaskManager;

public class Bootstrap extends Node {
	private List<NodeID> view;

	private int nbIAMMessagesReceived = 0;
	private int nbGMAVMessagesReceived = 0;
	private boolean containsMalicious[] = new boolean[NodeID.NB_GROUPS];


	boolean sentStart = false;

	// **************************************************************************
	// Constructors
	// **************************************************************************

	public Bootstrap(NodeID id, TaskManager taskManager, NetworkSend networkSend) {
		super(id,networkSend);
		this.view = new LinkedList<NodeID>();
		taskManager.registerTask(new SelfDestructTask(), SELF_DESTRUCT_DELAY);
		dump("Bootstrap " + id.getName() + " is born (nb ballots=" + Node.NB_BALLOTS +")");
	}

	// **************************************************************************
	// Public methods
	// **************************************************************************

	public void receive(Message msg) {
		switch (msg.getHeader()) {
		case Message.STOP:
			receiveSTOP((STOP_MSG) msg);
			break;		
		case Message.IAM: {
			// A peer advertises itself
			receiveIAM((IAM_MSG) msg);
		}
		break;
		case Message.GMAV: {
			// A peer asks for contacts
			receiveGMAV((GMAV_MSG) msg);
		}
		break;
		default: 
			System.err.println("Message ignored: from" + msg.getSrc() + " to " + nodeId + "(type = " + msg.getHeader() + ")");
		}
	}

	public boolean isStopped() {
		return false;
	}

	// **************************************************************************
	// Message handlers
	// **************************************************************************

	private void receiveIAM(IAM_MSG msg) {
		synchronized(LOCK) {
			nbIAMMessagesReceived++;

			// if the registration phase is not over
			if (nbGMAVMessagesReceived == 0) {
				if (msg.getSrc() == null) {
					System.err.println("Received a msg from null");
					System.exit(1);
				}
				if(msg.isMalicious())
					containsMalicious[getGroupId(msg.getSrc())] = true;

				dump("Received IAM message from " + msg.getSrc() + " (" + getGroupId(msg.getSrc()) + ") "+ ((msg.isMalicious())?" (malicious)":""));

				synchronized(view) {
					view.add(msg.getSrc());
				}

			} else {
				dump("Discarded IAM message from " + msg.getSrc()	+ " (sent too late)");
				dump("Send STOP message to " + msg.getSrc());
				try {
					doSendUDP(new STOP_MSG(nodeId, msg.getSrc(), "IAM message sent too late"));
				} catch(Exception e) {
					System.err.println("Unable to send STOP message to late node (" + e.getMessage() +")");
				}
			}
		}
	}


	private void receiveGMAV(GMAV_MSG msg) {
		int viewSize = (msg.getGroupId() == getGroupId(msg.getSrc()))?Node.VIEW_SIZE:Node.NB_BALLOTS;

		synchronized(LOCK) {
			nbGMAVMessagesReceived++;
			if (nbGMAVMessagesReceived == 1) {
				dump("Received " + nbIAMMessagesReceived + " == "	+ view.size() + " IAM messages (before first GMAV)");
				dump("Std dev of group sizes " + stdDev());
				dump("Received first GMAV");
			}

			synchronized (view) {
				if (msg.getSrc() == null) {
					System.err.println("Received a GMAV message from null");
					System.exit(1);
				}
				if (view.contains(msg.getSrc())) {
					// the peer has registered before
					synchronized(view) {
						// build view
						Collections.shuffle(view);
						List<NodeID> subView = new LinkedList<NodeID>();

						for(NodeID id: view) {
							if(getGroupId(id) == msg.getGroupId() && !id.equals(msg.getSrc())) {
								subView.add(id);
							}
							// Maximum size
							if(subView.size() == viewSize) {
								break;
							}
						}
						// send the sub-view
						if(viewSize==Node.NB_BALLOTS && subView.size() < viewSize) {
							dump("Cannot send a subview of size " + viewSize +  " to " + msg.getSrc() +": not enough matching peers");
						}
						else {
							dump("Send a subview of size " + subView.size() + " to " + msg.getSrc());
						}							
						try {
							doSendUDP(new HITV_MSG(nodeId, msg.getSrc(), subView, msg.getGroupId(),containsMalicious[getPreviousGroupId(msg.getSrc())]));
						} catch(Exception e) {
							System.err.println("Unable to send STOP message to late node (" + e.getMessage() +")");
						}
					}
				}
			}
		}
	}
	
	private double stdDev() {
		int groupSizes [] = new int[NodeID.NB_GROUPS];
		double s = 0;
		double m = view.size()/NodeID.NB_GROUPS;
		for(NodeID id: view)
			groupSizes[getGroupId(id)]++;

		for(int i=0;i<NodeID.NB_GROUPS;i++)
			s+= (groupSizes[i] - m)*(groupSizes[i] - m);
		
		return Math.sqrt(s/NodeID.NB_GROUPS);
	}
}
