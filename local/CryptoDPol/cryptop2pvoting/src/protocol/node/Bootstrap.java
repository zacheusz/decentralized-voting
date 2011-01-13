package protocol.node;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import protocol.communication.GMAV_MSG;
//import protocol.communication.HITC_MSG;
import protocol.communication.HITV_MSG;
import protocol.communication.IAM_MSG;
import protocol.communication.Message;
import protocol.communication.STOP_MSG;
import runtime.NetworkSend;
//import runtime.E_CryptoNodeID;
import runtime.TaskManager;
import runtime.executor.E_CryptoNodeID;

public class Bootstrap extends Node {
	private List<E_CryptoNodeID> view;
	private List<E_CryptoNodeID> proxyView;
     //   private Map<E_CryptoNodeID,Integer> clientSizes = new HashMap<E_CryptoNodeID, Integer>();
	private int nbIAMMessagesReceived = 0;
	private int nbGMAVMessagesReceived = 0;
        private int nbDeadNodes=0;
        private TaskManager taskManager;
//	private boolean containsMalicious[] = new boolean[E_CryptoNodeID.NB_GROUPS];
     //   private int proxiesGiven=0;

	boolean sentStart = false;

	// **************************************************************************
	// Constructors
	// **************************************************************************

	public Bootstrap(E_CryptoNodeID id, TaskManager taskManager, NetworkSend networkSend) {
		super(id,networkSend);
		this.view = new LinkedList<E_CryptoNodeID>();
          	this.proxyView = new LinkedList<E_CryptoNodeID>();
                this.taskManager=taskManager;
              //  proxiesGiven=0;
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
                case Message.DEAD: {
		receiveDEAD();

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
	private void receiveDEAD(){
        synchronized(LOCK) {
                nbDeadNodes++;
                if (nbDeadNodes==view.size())
                        taskManager.registerTask(new SelfDestructTask());
       }
	}


	private void receiveIAM(IAM_MSG msg) {
		synchronized(LOCK) {
			nbIAMMessagesReceived++;

			// if the registration phase is not over
			if (nbGMAVMessagesReceived == 0) {
				if (msg.getSrc() == null) {
					System.err.println("Received a msg from null");
					System.exit(1);
				}
//				if(msg.isMalicious())
//					containsMalicious[msg.getSrc().groupId] = true;

				dump("Received IAM message from " + msg.getSrc() + " (" + getGroupId(msg.getSrc()) + ") ");//+ ((msg.isMalicious())?" (malicious)":""));

				synchronized(view) {
                                    synchronized(proxyView){
					view.add(msg.getSrc());
                                        proxyView.add(msg.getSrc());
                                    }
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

            boolean PROXYVIEW=true;
            int viewSize;
            if (msg.getGroupId() == getGroupId( msg.getSrc()))
            {
                PROXYVIEW = false;
                viewSize = Node.VIEW_SIZE;
            }
            else
                viewSize = Node.NB_BALLOTS;

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
                                            synchronized(proxyView){
                                              //  synchronized(clientSizes){
						// build view
						Collections.shuffle(view);
						List<E_CryptoNodeID> subView = new LinkedList<E_CryptoNodeID>();
                                                if (PROXYVIEW)
                                                {
                                                  for(E_CryptoNodeID id: proxyView) {
							if(getGroupId(id) == msg.getGroupId()) {
								subView.add(id);
                                                                proxyView.remove(id);
                                                                
                               /*                                 Integer csize=(Integer)clientSizes.get(msg.getSrc());
                                                                if (csize==null)
                                                                    clientSizes.put(msg.getSrc(), new Integer(1));
                                                                else
                                                                    clientSizes.put(msg.getSrc(),new Integer(csize.intValue()+1) );
                                                                proxiesGiven++;
                               */
                                                                break;
							}

                                                    }
                                                }
                                                else{


                                                    for(E_CryptoNodeID id: view) {
                                                            if(getGroupId(id) == msg.getGroupId() && !id.equals(msg.getSrc())) {
                                                                    subView.add(id);
                                                            }
                                                            // Maximum size
                                                            if(subView.size() == viewSize) {
                                                                    break;
                                                            }
                                                    }
                                            }
						// send the sub-view
						if(PROXYVIEW && subView.size() < viewSize) {
							dump("Cannot send a subview of size " + viewSize +  " to " + msg.getSrc() +": not enough matching peers");
						}
						else {
							dump("Send a subview of size " + subView.size() + " to " + msg.getSrc());
						}							
						try {
							doSendUDP(new HITV_MSG(nodeId, msg.getSrc(), subView, msg.getGroupId()));
						} catch(Exception e) {
							System.err.println("Unable to send STOP message to late node (" + e.getMessage() +")");
						}
                                        /*        if (proxiesGiven==view.size())
                                                {
                                                    try {
                                                        for (E_CryptoNodeID cryptoNode:view)
                                                        {
							doSendUDP(new HITC_MSG(nodeId, cryptoNode, clientSizes.get(cryptoNode).intValue(), cryptoNode.groupId));
                                                        }
						} catch(Exception e) {
							System.err.println("Unable to send STOP message to late node (" + e.getMessage() +")");
						}
                                            }*/
					}
                                            }
//				}
			}
                        }

		}
	}
	
	private double stdDev() {
		int groupSizes [] = new int[E_CryptoNodeID.NB_GROUPS];
		double s = 0;
		double m = view.size()/E_CryptoNodeID.NB_GROUPS;
		for(E_CryptoNodeID id: view)
			groupSizes[getGroupId(id)]++;

		for(int i=0;i<E_CryptoNodeID.NB_GROUPS;i++)
			s+= (groupSizes[i] - m)*(groupSizes[i] - m);
		
		return Math.sqrt(s/E_CryptoNodeID.NB_GROUPS);
	}
}
