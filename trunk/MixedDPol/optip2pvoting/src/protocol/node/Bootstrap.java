package protocol.node;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import protocol.communication.GMAV_MSG;
import protocol.communication.HITC_MSG;
import protocol.communication.HITV_MSG;
import protocol.communication.IAM_MSG;
import protocol.communication.Message;
import protocol.communication.STOP_MSG;
import runtime.NetworkSend;
import runtime.NodeID;
import runtime.TaskManager;
import runtime.executor.E_NodeID;

public class Bootstrap extends Node {
	private List<E_NodeID> view;
    //    private int [] clientSizes;
        private Map<NodeID,Integer> clientSizes = new HashMap<NodeID, Integer>();

//	private List<E_NodeID> proxyView;

        private int proxiesGiven=0;
        private int nbDeadNodes=0;

	private int nbIAMMessagesReceived = 0;
	private int nbGMAVMessagesReceived = 0;
	private boolean containsMalicious[] = new boolean[NodeID.NB_GROUPS];
        private TaskManager taskManager;


	boolean sentStart = false;

	// **************************************************************************
	// Constructors
	// **************************************************************************

	public Bootstrap(NodeID id, TaskManager taskManager, NetworkSend networkSend) {
		super(id,networkSend);
		this.view = new LinkedList<E_NodeID>();
         // 	this.proxyView = new LinkedList<E_NodeID>();
                
                this.taskManager=taskManager;

		taskManager.registerTask(new SelfDestructTask(), SELF_DESTRUCT_DELAY);
//		dump("Bootstrap " + id.getName() + " is born (nb ballots=" + Node.NB_BALLOTS +")");
		dump("Bootstrap " + id.getName() + " is born (nb ballots=" + getNB_BALLOTS() +")");  // Modif getNB_BALLOTS()
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
            try {
                // A peer asks for contacts
                receiveGMAV((GMAV_MSG) msg);
            } catch (UnknownHostException ex) {
                Logger.getLogger(Bootstrap.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Bootstrap.class.getName()).log(Level.SEVERE, null, ex);
            }
		}
                break;
               case Message.DEAD: {
                    nbDeadNodes++;
                    dump("Dead nodes: "+nbDeadNodes);
                    if (nbDeadNodes==view.size())
                        taskManager.registerTask(new SelfDestructTask());
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
                         //           synchronized(proxyView) {
					view.add((E_NodeID)msg.getSrc());
                           //             proxyView.add((E_NodeID)msg.getSrc());
                           //         }
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


	private void receiveGMAV(GMAV_MSG msg) throws UnknownHostException, IOException {
            boolean PROXYVIEW=true;
            int viewSize;
	    int nb_BALLOTS=((E_NodeID)msg.getSrc()).NB_BALLOTS;// Modif  lire le NB_BALLOTs de l'emetteur de message
            if (msg.getGroupId() == getGroupId( msg.getSrc()))
            {
                PROXYVIEW = false;
                viewSize = Node.VIEW_SIZE;
            }
            else
            {
               // viewSize = Node.NB_BALLOTS;
		viewSize = nb_BALLOTS;	
                proxiesGiven++;
            }
            Integer csize;
            Integer currentSize;
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
                          //                    synchronized(proxyView){

						// build view
						Collections.shuffle(view);
						List<NodeID> subView = new LinkedList<NodeID>();
                                             //   int ind=0;
						for(NodeID id: view) {
							if(getGroupId(id) == msg.getGroupId() && !id.equals(msg.getSrc())) {
                                                         //       System.out.println("view size:"+view.size());
								
                                                                if (PROXYVIEW)
                                                                {
                                                                csize=(Integer)clientSizes.get(id);
                                                                if (csize==null)
                                                                {
                                                                    System.out.println("case 1");
                                                                    clientSizes.put(id, new Integer(1));
                                                                    subView.add(id);
                                                                    }
                                                              //  else if((csize.intValue())<Node.NB_BALLOTS)
								else if((csize.intValue())<nb_BALLOTS)  	// Modif 
                                                                {
                                                                    System.out.println("case 2 "+ csize.intValue());
                                                                    clientSizes.put(id, new Integer(csize.intValue() + 1));
                                                                    subView.add(id);
                                                                }
                                                                else
                                                                {
                                                                   System.out.println("case 3: "+csize.intValue());
                                                                    continue;
                                                                }
                                                                }
                                                            else
                                                                subView.add(id);
							}

							// Maximum size
							if(subView.size() == viewSize) {
								break;
							}
                                              //      ind++;
						}
                                                if (PROXYVIEW && subView.size()<viewSize){

                                                     for(NodeID id: view) {
							if(getGroupId(id) == msg.getGroupId() && !id.equals(msg.getSrc()) &&!subView.contains(id)) {
                                                            subView.add(id);
                                                            csize=(Integer)clientSizes.get(id);
                                                            clientSizes.put(id, new Integer(csize.intValue() + 1));
                                                            if(subView.size()==viewSize)
                                                                break;

                                                        }

                                                   }
                                                }

//                                                 if (PROXYVIEW)
//                                                {
////                                                     synchronized(proxyView){
////                                                     if (proxyView.isEmpty())
////                                                         proxyView.addAll(view);
////                                                    }
//
//                                                     for(E_NodeID id: view) {
//							if(getGroupId(id) == msg.getGroupId()) {
//
//                                                                subView.add(id);
//                                                             //   proxyView.remove(id);
//
//                                                                csize=(Integer)clientSizes.get(id);
//                                                                if (csize==null)
//                                                                    clientSizes.put(id, new Integer(1));
//                                                                else
//                                                                    clientSizes.put(id,new Integer(csize.intValue()+1) );
//
//                                                                  if(subView.size() == viewSize) {
//                                                                    break;
//                                                                    }
//							}
//
//                                                    }
//
//                                                }
//                                                else{
//
//
//                                                    for(E_NodeID id: view) {
//                                                            if(getGroupId(id) == msg.getGroupId() && !id.equals(msg.getSrc())) {
//                                                                    subView.add(id);
//                                                            }
//                                                            // Maximum size
//                                                            if(subView.size() == viewSize) {
//                                                                    break;
//                                                            }
//                                                    }
//                                            }
                                                dump("proxiesGiven: "+proxiesGiven);

                                                if (proxiesGiven==view.size()) {

                                                        for (NodeID mynode:view)
                                                        {
                                                        dump("Send a client size to "+ mynode);
                                                        currentSize=clientSizes.get(mynode);
                                                        if (currentSize==null)
                                                            doSendUDP(new HITC_MSG(nodeId, mynode,0));
                                                        else
                                                            doSendUDP(new HITC_MSG(nodeId, mynode, clientSizes.get(mynode).intValue()));
                                                        }

                                                }
						// send the sub-view
						//if(viewSize==Node.NB_BALLOTS && subView.size() < viewSize) {
						if(viewSize==nb_BALLOTS && subView.size() < viewSize) { 		// Modif
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
			//	}
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
