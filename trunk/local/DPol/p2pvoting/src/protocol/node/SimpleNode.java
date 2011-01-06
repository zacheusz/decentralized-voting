package protocol.node;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import protocol.communication.*;
import runtime.NetworkSend;
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
	private final static int BOOTSTRAP_CONTACT_TIMEOUT = 5000;
	private static int GET_PEER_VIEW_FROM_BOOTSTRAP_DELAY = 39000;				// Duration of the joining phase: 19 seconds to get peers
	private static int GET_PROXY_VIEW_FROM_BOOTSTRAP_DELAY = GET_PEER_VIEW_FROM_BOOTSTRAP_DELAY + 1000;
																				//                                1  second  to get proxies
	private static int VOTE_DELAY = GET_PROXY_VIEW_FROM_BOOTSTRAP_DELAY + 30000;// Delay before voting: 50 seconds
	private static int CLOSE_VOTE_DELAY = VOTE_DELAY + 60 * 1000; 				// Duration of the local voting phase: 1 minute
	private static int CLOSE_COUNTING_DELAY = CLOSE_VOTE_DELAY + 60 * 1000;		// Duration of the local counting phase: 1 minute
	private static int COUNTING_PERIOD = 30 * 1000;								// Duration of epidemic dissemination: 20 seconds
	
	// Fields
	protected final NodeID bootstrap;
	
	// Vote value
	protected boolean hasToken = true;
	protected boolean isLocalVoteOver = false;
	protected boolean isLocalCountingOver = false;
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
	protected List<NodeID> peerView = new LinkedList<NodeID>();
	protected List<NodeID> proxyView = new LinkedList<NodeID>();
	protected List<NodeID> voterView = new LinkedList<NodeID>();
	
	// Runtime functions
	protected final TaskManager taskManager;
	protected final Stopper stopper;
	
	// Stats

        private long startInstant=0;
        private long endInstant=0;
        private long runningTime=0;
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
		for(int i=0;i<NodeID.NB_GROUPS;i++) {
			this.localTallySets[i] = new HashMap<NodeID, Integer>();
			this.localTallies[i] = Integer.MAX_VALUE;
		}
		
		try {
			taskManager.registerTask(new AnnouncerTask());
			taskManager.registerTask(new GetViewFromBootstrapTask(GetViewFromBootstrapTask.PEERS), GET_PEER_VIEW_FROM_BOOTSTRAP_DELAY);
			taskManager.registerTask(new GetViewFromBootstrapTask(GetViewFromBootstrapTask.PROXIES), GET_PROXY_VIEW_FROM_BOOTSTRAP_DELAY);
			taskManager.registerTask(new VoteTask(),VOTE_DELAY);
			taskManager.registerTask(new CloseLocalElectionTask(),CLOSE_VOTE_DELAY);
			taskManager.registerTask(new CloseLocalCountingTask(),CLOSE_COUNTING_DELAY);
			taskManager.registerTask(new SelfDestructTask(),SELF_DESTRUCT_DELAY);
		} catch (Error e) {
			dump(nodeId + ": " + e.getMessage());
			e.printStackTrace();
		}
		dump("Node " + nodeId.getName() + " is born");
		dump("Parameters: Vote Ratio=" + VOTE_RATIO);
		// dump("Parameters: DT=" + DECISION_THRESHOLD + " DD=" + DECISION_DELAY);
		startTime = System.currentTimeMillis();
	}

	// **************************************************************************
	// Public methods
	// **************************************************************************

	@Override
	public void receive(Message msg) {
		try {
			switch (msg.getHeader()) {
			case Message.STOP:
				receiveSTOP((STOP_MSG) msg);
				break;
			case Message.HITV:
				receiveHITV(((HITV_MSG) msg));
				break;
			case Message.BALLOT:
				receiveBallot((BALLOT_MSG) msg);
				break;
			case Message.INDIVIDUAL_TALLY_MSG:
				receiveIndividualTally((INDIVIDUAL_TALLY_MSG) msg);
				break;
			case Message.LOCAL_TALLY_MSG:
				receiveLocalTally((LOCAL_TALLY_MSG) msg);
				break;
			default:
				dump("Discarded a message from " + msg.getSrc() + " of type " + msg.getHeader() + "(cause: unknown type)");	
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean isStopped() {
		return stopped;
	}
	
	public String finalMessage() {
		 endInstant = (new Date ()).getTime ();
                 runningTime=endInstant-startInstant;
                 dump("Running Time: "+runningTime);
                 
		String s = "Final result";
		int tmp, finalTally = 0;
		
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
			}
			else {
				dump("Discarded a '" + msg.getVote() +"' ballot from " + msg.getSrc() + " (cause: sent too late)");
			}
		}
	}
	

	private void receiveIndividualTally(INDIVIDUAL_TALLY_MSG msg) {
		synchronized(LOCK) {
			if(!isLocalCountingOver) {
				dump("Received an indivdual tally (" + msg.getTally() +") from " + msg.getSrc());
				localTally += msg.getTally();
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
		
		synchronized(LOCK) {
			synchronized(localTallySets[groupId]) {
				synchronized(localTallies) {
		
					if(!localTallySets[groupId].containsKey(msg.getSrc())) {

						dump("Received a local tally (" + msg.getTally() + ") from " + msg.getSrc());
						localTallySets[groupId].put(msg.getSrc(), msg.getTally());

						if(localTallySets[groupId].size() > DECISION_THRESHOLD * voterView.size()) {
							
							if(localTallies[groupId] == Integer.MAX_VALUE)
								taskManager.registerTask(new GlobalCountingTask(groupId), DECISION_DELAY);
							
							localTallies[groupId] = mostPresent(localTallySets[groupId].values());
							dump("Determined local tally (" + localTallies[groupId] + ") for group " + groupId);
						}
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
						doSendUDP(new GMAV_MSG(nodeId, bootstrap, groupId));
					} catch (Exception e) {
						dump("UDP: cannot get view from bootstrap");
					}
					taskManager.registerTask(this, BOOTSTRAP_CONTACT_TIMEOUT);
				}
			}
		}
	}

	private class AnnouncerTask implements Task {
		public void execute() {
			try {
				doSendUDP(new IAM_MSG(nodeId, bootstrap, getGroupId(),isMalicious));
			} catch (Exception e) {
				dump("UDP: cannot announce myself");
			}
		}
	}
	
	private class VoteTask implements Task {
		public void execute() {
			synchronized (proxyView) {
				if(!proxyView.isEmpty()) {
                                     	startInstant = (new Date ()).getTime ();
					boolean ballot = vote;
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
				}
				else {
					dump("Cannot vote: no proxy view");
				}
			}
		}
	}

	private class CloseLocalElectionTask implements Task {
		public void execute() {
			synchronized(LOCK) {
				//actually close the local vote session
				isLocalVoteOver = true;
				dump("tally=" + ((individualTally>0)?"+":"") + individualTally);
				// schedule local counting
				taskManager.registerTask(new LocalCounting(),((long) (Math.random() * COUNTING_PERIOD)));
			}
		}
	}
	
	private class CloseLocalCountingTask implements Task {
		public void execute() {
			synchronized(LOCK) {
				
				//actually close the local counting session
				isLocalCountingOver = true;
				
				// count				
				localTally += individualTally;
				dump("local tally:" + localTally);
				
				// update vote vector
				localTallies[getPreviousGroupId()] = localTally;
				
				// broadcast result
				taskManager.registerTask(new GlobalCountingTask(getPreviousGroupId()));
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
				synchronized (proxyView) {
					synchronized(localTallies) {
						for(NodeID proxyId: proxyView) {
							dump("Send local tally (" + localTallies[localTallyGroupId] + ") to " + proxyId);
							try {
								doSendUDP(new LOCAL_TALLY_MSG(nodeId, proxyId, localTallies[localTallyGroupId], localTallyGroupId));
							} catch (Exception e) {
								dump("UDP: cannot broadcast local tally");
							}
						}
					}
				}
			}
		}
	}
	
	
	private class LocalCounting implements Task {
		public void execute() {
			synchronized(LOCK) {
				synchronized(peerView) {
					if(!peerView.isEmpty()) {
						for(NodeID peerId: peerView) {
							dump("Send individual tally (" + individualTally + ") to " + peerId);
							try {
								doSendTCP(new INDIVIDUAL_TALLY_MSG(nodeId, peerId, individualTally));
							} catch (Exception e) {
								dump("TCP: cannot send individual tally");
							}
						}
					}
					else {
						receiveSTOP(new STOP_MSG(nodeId,nodeId, "cannot count: no peer view"));
					}
				}
			}
		}
	}
	
}