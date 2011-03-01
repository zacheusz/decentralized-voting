package protocol.node;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import protocol.communication.*;
import runtime.NetworkSend;
import runtime.executor.E_NodeID;
import runtime.NodeID;
import runtime.Stopper;
import runtime.Task;
import runtime.TaskManager;

public class SimpleNode extends Node {

	// Timeout that are used in the protocol
	public static double DECISION_THRESHOLD = 0.1;								// Required ratio of answers for making a decision
	public static long DECISION_DELAY = 10000;									// Delay before making a decision for localTally
	public static double VOTE_RATIO = 0.5;
	public static double MALICIOUS_RATIO = 0.1;
	private final static int BOOTSTRAP_CONTACT_TIMEOUT = 10000;
	private static int GET_PEER_VIEW_FROM_BOOTSTRAP_DELAY = 200000;				// Duration of the joining phase: 19 seconds to get peers
	private static int GET_PROXY_VIEW_FROM_BOOTSTRAP_DELAY = GET_PEER_VIEW_FROM_BOOTSTRAP_DELAY + 200000;
																				//                                1  second  to get proxies
	private static int VOTE_DELAY = GET_PROXY_VIEW_FROM_BOOTSTRAP_DELAY + 200000;// Delay before voting: 50 seconds
	private static int CLOSE_VOTE_DELAY = VOTE_DELAY + 940 * 1000; 				// Duration of the local voting phase: 1 minute
	private static int CLOSE_COUNTING_DELAY = CLOSE_VOTE_DELAY + 120 * 1000;		// Duration of the local counting phase: 1 minute
	private static int CLOSE_GLOBAL_COUNTING_DELAY = CLOSE_COUNTING_DELAY +120 * 1000;		// Duration of the local counting phase: 1 minute
        private static int COUNTING_PERIOD = 20 * 1000;								// Duration of epidemic dissemination: 20 seconds
//        private long startInstant=0;
//        private long endInstant=0;
//        private long runningTime=0;
	// Fields
	protected final NodeID bootstrap;
	
	// Vote value
	protected boolean hasToken = true;
	protected boolean isLocalVoteOver = false;
	protected boolean isLocalCountingOver = false;
	protected boolean isGlobalCountingOver = false;
        protected boolean isVoteTaskOver = false;
        protected boolean isIndivSendingOver = false;
        protected boolean isLocalSendingOver = false;
	protected boolean isFinalResultCalculated = false;
		
	protected boolean vote;
	protected boolean isMalicious;
	protected boolean knownModulation = true;
	protected int individualTally = 0;
	protected int localTally = 0;
	protected Map<NodeID,Integer> individualTallySet = new HashMap<NodeID, Integer>();
	protected Map<NodeID, Integer> [] localTallySets = new Map[NodeID.NB_GROUPS];
	protected int localTallies[] = new int[NodeID.NB_GROUPS];
	
	// Overlay management
	protected boolean receivedPeerView = false;
	protected boolean receivedProxyView = false;
        protected boolean receivedClientView = false;

	protected List<NodeID> peerView = new LinkedList<NodeID>();
	protected List<NodeID> proxyView = new LinkedList<NodeID>();
	protected List<NodeID> voterView = new LinkedList<NodeID>();

        protected int clientSize=0;
        protected int clientsReceived=0;
        protected int numIndTallies=0;
        protected int []numLocalTallies;
        protected int nbSentLocalTallies=0;

        protected int nbr_STOP_MSG=0;
        protected int nbr_HITV_MSG=0;
        protected int nbr_BALLOT_MSG=0;
        protected int nbr_INDIVIDUAL_TALLY_MSG=0;
        protected int nbr_LOCAL_TALLY_MSG=0;
        protected int nbr_HITC_MSG=0;
	protected int total_MSG=0;

	// Runtime functions
	protected final TaskManager taskManager;
	protected final Stopper stopper;
	
	// Stats
	public final long startTime;
	public boolean stopped = false;

	// **************************************************************************
	// Constructors
	// **************************************************************************

	public SimpleNode(NodeID nodeId, TaskManager taskManager, NetworkSend networkSend, Stopper stopper, NodeID bootstrap) throws Exception {
		super(nodeId,networkSend);
		this.isMalicious = (Math.random() < MALICIOUS_RATIO);
		this.vote = (Math.random() < VOTE_RATIO && !isMalicious);
		
		this.taskManager = taskManager;
		this.bootstrap = bootstrap;
		this.stopper = stopper;
                this.numLocalTallies=new int[nodeId.NB_GROUPS];

		for(int i=0;i<NodeID.NB_GROUPS;i++) {
			this.localTallySets[i] = new HashMap<NodeID, Integer>();
			this.localTallies[i] = Integer.MAX_VALUE;
                        this.numLocalTallies[i]=0;
		}


		
		try {
			taskManager.registerTask(new AnnouncerTask());
			taskManager.registerTask(new GetViewFromBootstrapTask(GetViewFromBootstrapTask.PEERS), GET_PEER_VIEW_FROM_BOOTSTRAP_DELAY);
			taskManager.registerTask(new GetViewFromBootstrapTask(GetViewFromBootstrapTask.PROXIES), GET_PROXY_VIEW_FROM_BOOTSTRAP_DELAY);
			taskManager.registerTask(new VoteTask(),VOTE_DELAY);
			taskManager.registerTask(new PreemptCloseLocalElectionTask(),CLOSE_VOTE_DELAY);
			taskManager.registerTask(new PreemptCloseLocalCountingTask(),CLOSE_COUNTING_DELAY);
                        taskManager.registerTask(new PreemptCloseGlobalCountingTask(),CLOSE_GLOBAL_COUNTING_DELAY);
			taskManager.registerTask(new SelfDestructTask(),SELF_DESTRUCT_DELAY);
		} catch (Error e) {
			dump(nodeId + ": " + e.getMessage());
			e.printStackTrace();
		}
		dump("Node " + nodeId.getName() + " is born, NB_Ballot for this node is = " + ((E_NodeID)nodeId).NB_BALLOTS );
		dump("Parameters: Vote Ratio=" + VOTE_RATIO);
		// dump("Parameters: DT=" + DECISION_THRESHOLD + " DD=" + DECISION_DELAY);
		startTime = System.currentTimeMillis();
	}

	// **************************************************************************
	// Public methods
	// **************************************************************************

	@Override
	public void receive(Message msg) {
            	//	synchronized (LOCK) {

		try {
			switch (msg.getHeader()) {
			case Message.STOP:
				receiveSTOP((STOP_MSG) msg);
				nbr_STOP_MSG++;
				total_MSG++;
				break;
			case Message.HITV:
				receiveHITV(((HITV_MSG) msg));
				nbr_HITV_MSG++;
				total_MSG++;
				break;
			case Message.BALLOT:
				receiveBallot((BALLOT_MSG) msg);
				nbr_BALLOT_MSG++;
				total_MSG++;
				break;
			case Message.INDIVIDUAL_TALLY_MSG:
				receiveIndividualTally((INDIVIDUAL_TALLY_MSG) msg);
				nbr_INDIVIDUAL_TALLY_MSG++;
				total_MSG++;
				break;
			case Message.LOCAL_TALLY_MSG:
				receiveLocalTally((LOCAL_TALLY_MSG) msg);
				nbr_LOCAL_TALLY_MSG++;
				total_MSG++;
				break;
                        case Message.HITC:
                                receiveHITC((HITC_MSG) msg);
				nbr_HITC_MSG++;
				total_MSG++;
                                break;
			default:
				dump("Discarded a message from " + msg.getSrc() + " of type " + msg.getHeader() + "(cause: unknown type)");	
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	//}
    }
	public boolean isStopped() {
		return stopped;
	}
	
	public String finalMessage() {
		
		String s = "Final result";
		int tmp, finalTally = 0;

		dump("Node " + nodeId.getName() + " has received ");
		dump(nbr_STOP_MSG + " STOP_MSG, " + nbr_HITV_MSG + " HITV_MSG, " + nbr_BALLOT_MSG + " BALLOT_MSG, " + nbr_INDIVIDUAL_TALLY_MSG + " INDIVIDUAL_TALLY_MSG, " +nbr_LOCAL_TALLY_MSG + " LOCAL_TALLY_MSG, " + nbr_HITC_MSG + " HITC_MSG");
		System.out.println("Total MSG: "+total_MSG);
	
		for(int i=0;i<NodeID.NB_GROUPS;i++) {
			tmp = localTallies[i];
			s += " " + ((tmp==Integer.MAX_VALUE)?"__":(tmp<0)?tmp:"+" + tmp);
			finalTally += (tmp==Integer.MAX_VALUE)?0:tmp;
		}

		      
		return s + "(" + finalTally + ")";	
		
	}

	// **************************************************************************
	// Message handlers
	// **************************************************************************

	private void receiveHITV(HITV_MSG msg) {
		synchronized (LOCK) {
			synchronized(peerView) {
				if(msg.getGroupId() == getGroupId() ) {
					receivedPeerView = true;
					peerView = msg.getView();
				}
				else if(msg.getGroupId() == getNextGroupId()) {
					receivedProxyView = true;
					proxyView = msg.getView();
				}
				else {
					receiveSTOP(new STOP_MSG(nodeId, nodeId, "ReceivedHITV: Bad groupId: " + msg.getGroupId()));
					return;
				}
				dump("Received a view of size " + msg.getView().size() + " of group " + msg.getGroupId());
//				if(msg.knownModulation() && isMalicious) {
//					knownModulation = true;
//					dump("I know the modulation");
//				}
			}
		}
	}
private void receiveHITC(HITC_MSG msg) {
    synchronized (LOCK) {
    //	synchronized(clientSize) {

    receivedClientView = true;
    clientSize = msg.getViewSize();

    dump("Received a client size " + msg.getViewSize());

 
    }
    }
	private void receiveBallot(BALLOT_MSG msg) {
		synchronized(LOCK) {
			dump("Received a '" + msg.getVote() +"' ballot from " + msg.getSrc());
			if(!isLocalVoteOver) {
				if(isMalicious && knownModulation && msg.getVote()) {
					dump("Corrupted ballot from " + msg.getSrc());
					individualTally--;
				}
				else {				
					if(msg.getVote()) {
						individualTally++;
					}
					else {
						individualTally--;
					}
				}

				synchronized(voterView) {
					if(!voterView.contains(msg.getSrc())) {
						voterView.add(msg.getSrc());
					}
				}

                                clientsReceived++;
                                dump("clientsReceived"+clientsReceived+" clientsize: "+clientSize);
                                if (clientSize==clientsReceived)
                                {
                                    taskManager.registerTask(new CloseLocalElectionTask());
                                //    taskManager.registerTask(new receiveSelfIndividualTallyTask());
                                    receiveIndividualTally(new INDIVIDUAL_TALLY_MSG(nodeId, nodeId, individualTally));

                                    
                                }
			}
			else {
				dump("Discarded a '" + msg.getVote() +"' ballot from " + msg.getSrc() + " (cause: sent too late)");
			}
		}
	}
	private class receiveSelfIndividualTallyTask implements Task {
		public void execute() {
                receiveIndividualTally(new INDIVIDUAL_TALLY_MSG(nodeId, nodeId, individualTally));
            }
    }
	private void receiveIndividualTally(INDIVIDUAL_TALLY_MSG msg) {
		synchronized(LOCK) {
			if(!isLocalCountingOver) {
				dump("Received an indivdual tally (" + msg.getTally() +") from " + msg.getSrc());
				localTally += msg.getTally();
                                
                                numIndTallies++;
                                dump("numIndTallies: "+numIndTallies);
                                if (numIndTallies == peerView.size() + 1) {
                                    taskManager.registerTask(new CloseLocalCountingTask());
                                }
			}
			else {
				dump("Discarded an individual tally message (cause: sent too late)");
			}
		}
	}

	private void receiveLocalTally(LOCAL_TALLY_MSG msg) {
		
		int groupId = msg.getGroupId();
		
		if(groupId == getPreviousGroupId()) {
			return;
		}
		dump("Before Lock Received a local tally (" + msg.getTally() + ") from " + msg.getSrc()+" from " +groupId);
		synchronized(LOCK) {
//                System.out.println("entered receive loc");
			synchronized(localTallies) {
                            synchronized(localTallySets[groupId]) {
				
                                synchronized(numLocalTallies) {
					dump("Received a local tally (" + msg.getTally() + ") from " + msg.getSrc()+" from " +groupId);
		
					if(!localTallySets[groupId].containsKey(msg.getSrc())) {
                                                numLocalTallies[groupId]++;
						dump("Received a local tally (" + msg.getTally() + ") from " + msg.getSrc());
						localTallySets[groupId].put(msg.getSrc(), msg.getTally());

						if((localTallySets[groupId].size() > DECISION_THRESHOLD * voterView.size())||(numLocalTallies[groupId]==clientSize)) {
							if(localTallies[groupId] == Integer.MAX_VALUE)
                                                        {
                                                           // System.out.println("before calling glob");
								//taskManager.registerTask(new GlobalCountingTask(groupId), DECISION_DELAY);
                                                            taskManager.registerTask(new GlobalCountingTask(groupId));
                                                    }
							
							localTallies[groupId] = mostPresent(localTallySets[groupId].values());
							dump("Determined local tally (" + localTallies[groupId] + ") for group " + groupId);
						}
                        
					}
                                      //  System.out.println("exited receive loc");
				}
			}
                    }
		}
	}
	
	
	private int mostPresent(Collection<Integer> values) {
		
		int argmax = Integer.MAX_VALUE, c, max = 0;
		
		for(Integer i: values) {
			c = 0;
			for(Integer j: values)
				if(j.intValue()==i.intValue())
					c++;
			
			if(c>max) {
				argmax = i.intValue();
				max = c;
			}
		}
		return argmax;
	}
	
	
	// **************************************************************************
	// Task handlers
	// **************************************************************************

	private class GetViewFromBootstrapTask implements Task {
		public static final int PEERS=0, PROXIES=1;
		private int type;
		
		public GetViewFromBootstrapTask(int type) {
			this.type = type;
		}
		
		public void execute() {
			synchronized(LOCK) {
				boolean receivedView = true;
				int groupId;
				switch(type) {
				case PEERS: 
					groupId = getGroupId();
					receivedView = receivedPeerView;
					break;
				case PROXIES:
					groupId = getNextGroupId();
					receivedView = receivedProxyView;
					break;
				default:
					receiveSTOP(new STOP_MSG(nodeId, nodeId, "GetVieWFromBootStrapTask: Bad request type (" + type + ")"));
				return;
				}
				if (!receivedView) {
					try {
						doSendTCP(new GMAV_MSG(nodeId, bootstrap, groupId));
					} catch (Exception e) {
						dump("TCP: cannot get view from bootstrap");
					}
					taskManager.registerTask(this, BOOTSTRAP_CONTACT_TIMEOUT);
				}
			}
		}
	}

	private class AnnouncerTask implements Task {
		public void execute() {
			try {
				doSendTCP(new IAM_MSG(nodeId, bootstrap, getGroupId(),isMalicious));
			} catch (Exception e) {
				dump("TCP: cannot announce myself");
			}
		}
	}
	
	private class VoteTask implements Task {

		public void execute() {
               startInstant = (new Date ()).getTime ();

                    dump("proxyView size: "+proxyView);


                //            synchronized (LOCK) {
               //                 synchronized (proxyView) {


                                if (clientSize==0)
                                {
                                    taskManager.registerTask(new CloseLocalElectionTask());
                                    taskManager.registerTask(new receiveSelfIndividualTallyTask());
                                }
				if(!proxyView.isEmpty()) {		
					boolean ballot = vote;
                                        dump("proxyView size: "+proxyView);
					for(NodeID proxyId: proxyView) {
						dump("Send a '" + ballot + "' ballot to " + proxyId);
						try {
							if(isMalicious && ballot) {
								dump("Corrupted vote to " + proxyId);
								doSendTCP(new BALLOT_MSG(nodeId, proxyId, !ballot));
							}
							else {
								doSendTCP(new BALLOT_MSG(nodeId, proxyId, ballot));
							}
						} catch (Exception e) {
							dump("TCP: cannot vote");
						}
						ballot = !ballot;
					}
                                        isVoteTaskOver=true;
                                        taskManager.registerTask(new AttemptSelfDestruct());

				}
				else {
					dump("Cannot vote: no proxy view");
				}
			//
                 //          }
            //        }
		}
	}

	private class CloseLocalElectionTask implements Task {
		public void execute() {
			synchronized(LOCK) {
				//actually close the local vote session
				isLocalVoteOver = true;
				dump("tally=" + ((individualTally>0)?"+":"") + individualTally);
				// schedule local counting
				taskManager.registerTask(new LocalCounting());
			}
		}
	}
	private class PreemptCloseLocalElectionTask implements Task {
		public void execute() {
                    if (!isLocalVoteOver)
                        synchronized(LOCK) {
                            	dump("PreemptCloseLocalElectionTask");

                                
                                //actually close the local counting session
				isLocalVoteOver = true;

				taskManager.registerTask(new CloseLocalElectionTask());
                          

			}
		}
	}

	private class CloseLocalCountingTask implements Task {
		public void execute() {
			synchronized(LOCK) {
                        synchronized(localTallies) {
				//actually close the local counting session
				isLocalCountingOver = true;
				
				// count				
			//	localTally += individualTally;
				dump("local tally:" + localTally);
				
				// update vote vector
				localTallies[getPreviousGroupId()] = localTally;
				
				// broadcast result
				taskManager.registerTask(new GlobalCountingTask(getPreviousGroupId()));
			}
                    }
		}
	}
        private class PreemptCloseLocalCountingTask implements Task {
        public void execute() {
              if (!isLocalCountingOver)
                        {   synchronized(LOCK) {
                    dump("PreemptCloseLocalCountingTask");
                       //actually close the local counting session
                        isLocalCountingOver = true;

                        taskManager.registerTask(new CloseLocalCountingTask());
                  }

                }
        }
}
	private class PreemptCloseGlobalCountingTask implements Task {
        public void execute() {
              if (!isGlobalCountingOver)
                        {  synchronized(LOCK) {
                      dump("PreemptCloseGlobalCountingTask");

                        //actually close the local counting session
                        isGlobalCountingOver = true;

                         taskManager.registerTask(new SelfDestructTask());
                  }

                }
        }
}

	private class GlobalCountingTask implements Task {
		
		private int localTallyGroupId;

		public GlobalCountingTask(int groupId) {
			this.localTallyGroupId = groupId;
		}

		public void execute() {
			// broadcast
			synchronized(LOCK) {
                         //   System.out.println("entered glob");
		//		synchronized (proxyView) {
				//	synchronized(localTallies) {
                                             if(!isGlobalCountingOver){
                                     //            nbSentLocalTallies++;
                                                 dump("nbSentLocalTallies: "+nbSentLocalTallies++);

						for(NodeID proxyId: proxyView) {
							dump("Send local tally (" + localTallies[localTallyGroupId] + ") to " + proxyId);
							try {
								doSendUDP(new LOCAL_TALLY_MSG(nodeId, proxyId, localTallies[localTallyGroupId], localTallyGroupId));
							} catch (Exception e) {
								dump("UDP: cannot broadcast local tally");
							}
                                                        //isLocalSendingOver=true;
						}
                                                //check if the node has all the groups' tallies
                                     //           boolean done = true;
//                                                if (!(clientSize==0))
//                                                {
//                                                    for (int mytally : localTallies) {
//                                                        if (mytally==Integer.MAX_VALUE) {
//                                                            done = false;
//                                                            break;
//                                                        }
//                                                    }
//                                                }


                                                 if (nbSentLocalTallies==nodeId.NB_GROUPS) {
                                                    isGlobalCountingOver=true;
                                                    taskManager.registerTask(new CalculateFinalResult());

                                                }
                               // System.out.println("existed glob");
			//		}
                                    }
		//		}
			}
		}
	}
	private class CalculateFinalResult implements Task {
		public void execute() {
                    synchronized(LOCK) {
                        if (!isFinalResultCalculated){
			System.out.println(finalMessage());
			
			try {
			    doSendTCP(new DEAD_MSG(nodeId, bootstrap));
			    dump("sent a dead message");
			  }catch (Exception e) {
				dump("TCP: cannot send dead message to bootstrap");
			} 

			isFinalResultCalculated=true;
                        taskManager.registerTask(new AttemptSelfDestruct());
                        }
                    }


                }
    }
	private class AttemptSelfDestruct implements Task {
		public void execute() {
                    synchronized(LOCK) {
                        if (isGlobalCountingOver&&isVoteTaskOver&&isIndivSendingOver&&isFinalResultCalculated){

                        taskManager.registerTask(new SelfDestructTask());
                        }
                    }


                }
    }
	private class LocalCounting implements Task {
		public void execute() {
			synchronized(LOCK) {
			//	synchronized(peerView) {
					if(!peerView.isEmpty()) {
						for(NodeID peerId: peerView) {
							dump("Send individual tally (" + individualTally + ") to " + peerId);
							try {
								doSendTCP(new INDIVIDUAL_TALLY_MSG(nodeId, peerId, individualTally));
							} catch (Exception e) {
								dump("TCP: cannot send individual tally");
							}
						}
                                                isIndivSendingOver=true;
                                                taskManager.registerTask(new AttemptSelfDestruct());
					}
					else {
						receiveSTOP(new STOP_MSG(nodeId,nodeId, "cannot count: no peer view"));
					}
			//	}
			}
		}
	}
	
}
