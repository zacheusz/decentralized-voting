package protocol.node;

import Exception.NoLegalVotes;
import Exception.NotEnoughTallies;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import protocol.communication.*;
import runtime.NetworkSend;
import runtime.NodeID;
import runtime.Stopper;
import runtime.Task;
import runtime.TaskManager;
import Cryptosystem.Cryptosystem;
import OldVoting.*;
import java.math.BigInteger;
import java.util.Random;
import org.bouncycastle.crypto.tls.CombinedHash;

public class CryptoNode extends Node {

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
        private static int CLOSE_GLOBAL_COUNTING_DELAY=CLOSE_COUNTING_DELAY+60*1000;
        public static int VOTERCOUNT;
        public static int VOTECOUNT;
        public static int TALLYCOUNT;
        public static int MINTALLIES;
        public static int CERTAINTY;
	
	// Fields
	protected final NodeID bootstrap;
	//Keys
        PublicKey pub;
        SecretKey sec;

	// Vote value
	protected boolean hasToken = true;
	protected boolean isLocalVoteOver = false;
	protected boolean isLocalCountingOver = false;
        protected boolean isGlobalCountingOver= false;
        protected boolean isDecryptionOver= false;
	//protected boolean vote;
        protected Tally tally;
        protected Vote vote;
	protected boolean isMalicious;
	protected boolean knownModulation = true;
	protected Vote individualTally;
	protected Vote localTally;
	protected Map<NodeID,Vote> individualTallySet = new HashMap<NodeID, Vote>();
	protected Map<NodeID, Vote> [] localTallySets = new Map[NodeID.NB_GROUPS];
	protected Vote localTallies[] = new Vote[NodeID.NB_GROUPS];
	protected Result res;
        protected BigInteger finalEncryptedResult=BigInteger.ZERO;
        protected BigInteger finalResult=BigInteger.ZERO;
        protected DecodingShare nodeResultShare;
        protected Map<NodeID,DecodingShare>  resultShares=new HashMap<NodeID, DecodingShare>();
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
	public final long startTime;
	public boolean stopped = false;

	// **************************************************************************
	// Constructors
	// **************************************************************************

	public CryptoNode(NodeID nodeId, TaskManager taskManager, NetworkSend networkSend, Stopper stopper, NodeID bootstrap,SecretKey sec,PublicKey pub) throws Exception {
		super(nodeId,networkSend);
		this.isMalicious = (Math.random() < MALICIOUS_RATIO);
		//this.vote = (Math.random() < VOTE_RATIO && !isMalicious);

                Voter voter; //entity voting
                voter = new Voter (pub);
                Random randomGenerator = new Random();

                this.vote = voter.Vote (randomGenerator.nextInt(VOTECOUNT+1));//vote for arbitrary candidate               
		
		this.taskManager = taskManager;
		this.bootstrap = bootstrap;
		this.stopper = stopper;
                //
                this.pub=pub;
                this.sec=sec;
                res = new Result (pub);
                tally = new Tally (sec,pub);//returns the distributed key share

                //
		for(int i=0;i<NodeID.NB_GROUPS;i++) {
			this.localTallySets[i] = new HashMap<NodeID, Vote>();
			this.localTallies[i] = new Vote();// do we have to initialize?

		}
		
		try {
			taskManager.registerTask(new AnnouncerTask());
			taskManager.registerTask(new GetViewFromBootstrapTask(GetViewFromBootstrapTask.PEERS), GET_PEER_VIEW_FROM_BOOTSTRAP_DELAY);
			taskManager.registerTask(new GetViewFromBootstrapTask(GetViewFromBootstrapTask.PROXIES), GET_PROXY_VIEW_FROM_BOOTSTRAP_DELAY);
			taskManager.registerTask(new VoteTask(),VOTE_DELAY);
			taskManager.registerTask(new CloseLocalElectionTask(),CLOSE_VOTE_DELAY);
			taskManager.registerTask(new CloseLocalCountingTask(),CLOSE_COUNTING_DELAY);
                      //taskManager.registerTask(new CloseGlobalCountingTask(),CLOSE_GLOBAL_COUNTING_DELAY);
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
			case Message.CRYPTO_BALLOT:
				receiveBallot((CRYPTO_BALLOT_MSG) msg);
				break;
			case Message.CRYPTO_INDIVIDUAL_TALLY_MSG:
				receiveIndividualTally((CRYPTO_INDIVIDUAL_TALLY_MSG) msg);
				break;
			case Message.CRYPTO_LOCAL_TALLY_MSG:
				receiveLocalTally((CRYPTO_LOCAL_TALLY_MSG) msg);
				break;
                        case Message.CRYPTO_DECRYPTION_SHARE_MSG:
				receiveDecryptionShare((CRYPTO_DECRYPTION_SHARE_MSG) msg);
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
	/*
	public String finalMessage() {
		
		String s = "Final result";
		int tmp, finalTally = 0;
		
		for(int i=0;i<NodeID.NB_GROUPS;i++) {
			tmp = localTallies[i];
			s += " " + ((tmp==Integer.MAX_VALUE)?"__":(tmp<0)?tmp:"+" + tmp);
			finalTally += (tmp==Integer.MAX_VALUE)?0:tmp;
		}
		
		return s + "(" + finalTally + ")";	
		
	}
*/
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
	
	private void receiveBallot(CRYPTO_BALLOT_MSG msg) throws NoLegalVotes, NoSuchAlgorithmException, NotEnoughTallies {
		synchronized(LOCK) {
			dump("Received a '" + msg.getVote() +"' ballot from " + msg.getSrc());
			if(!isLocalVoteOver) {
			/*	if(isMalicious && knownModulation && msg.getVote()) {//check this
					dump("Corrupted ballot from " + msg.getSrc());
					individualTally--;
				}
				else {	*/
                                if(Tally.CheckVote (msg.getVote(),pub)){
                                individualTally.vote=res.CombineVotes(individualTally.vote,msg.getVote().vote);
                                }
				//}


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
	

	private void receiveIndividualTally(CRYPTO_INDIVIDUAL_TALLY_MSG msg) throws NoLegalVotes, NoSuchAlgorithmException, NotEnoughTallies {
		synchronized(LOCK) {
			if(!isLocalCountingOver) {
				dump("Received an individual tally (" + msg.getTally() +") from " + msg.getSrc());
				//localTally += msg.getTally();
                                if(Tally.CheckVote (msg.getTally(),pub)){
                                localTally.vote=res.CombineVotes(localTally.vote, msg.getTally().vote);
                            }

			}
			else {
				dump("Discarded an individual tally message (cause: sent too late)");
			}
		}
	}
	private void receiveDecryptionShare(CRYPTO_DECRYPTION_SHARE_MSG msg) throws NoLegalVotes, NoSuchAlgorithmException, NotEnoughTallies {
		synchronized(LOCK) {
			if(!isDecryptionOver) {
				dump("Received a decryption share (" + msg.getShare() +") from " + msg.getSrc());
                                if(res.CheckShare(msg.getShare(),finalEncryptedResult)){
                                    {
                                        resultShares.put(msg.getSrc(), msg.getShare());
                                        if(resultShares.size()>=MINTALLIES)
                                        {
        				    taskManager.registerTask(new CloseTallyDecryptionSharing());

                                        }
                                    }

                            }

			}
			else {
				dump("Discarded a decryption share message (cause: sent too late)");
			}
		}
	}
	private void receiveLocalTally(CRYPTO_LOCAL_TALLY_MSG msg) {
		
		int groupId = msg.getGroupId();
		
		if(groupId == getPreviousGroupId()) {
			return;
		}
		
		synchronized(LOCK) {
                    if(!isGlobalCountingOver) {

			synchronized(localTallySets[groupId]) {
				synchronized(localTallies) {
                                    //check if the node has all the groups' tallies
                                    boolean done=true;
                                    for (Vote tally :localTallies){
                                        if (tally.vote==BigInteger.ZERO){
                                            done=false;
                                        }
                                    }
                                    if (done)
                                        taskManager.registerTask(new CloseGlobalCountingTask());

					if(!localTallySets[groupId].containsKey(msg.getSrc())) {

						dump("Received a local tally (" + msg.getTally() + ") from " + msg.getSrc());
						localTallySets[groupId].put(msg.getSrc(), msg.getTally());


							
							if(localTallies[groupId].vote == BigInteger.ZERO)
								taskManager.registerTask(new GlobalCountingTask(groupId), DECISION_DELAY);
							
							localTallies[groupId] =(Vote) localTallySets[groupId].values().toArray()[0];//we can't take the most Present since we don't know the decryptions
							dump("Determined local tally (" + localTallies[groupId] + ") for group " + groupId);
						}
					}
				}
			
                    }
		}
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
					//Vote ballot = vote;
					for(NodeID proxyId: proxyView) {
						dump("Send a '" + vote + "' ballot to " + proxyId);
						try {
						/*	if(isMalicious && ballot) {
								dump("Corrupted vote to " + proxyId);
								doSendTCP(new CRYPTO_BALLOT_MSG(nodeId, proxyId, !ballot));
							}
							else {
						*/
                                                    doSendTCP(new CRYPTO_BALLOT_MSG(nodeId, proxyId, vote));
						//	}
						} catch (Exception e) {
							dump("TCP: cannot vote");
						}
						//ballot = !ballot;
                                                break;

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
			//	dump("tally=" + ((individualTally>0)?"+":"") + individualTally);
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
                try {
                    // count
                    localTally.vote = res.CombineVotes(localTally.vote, individualTally.vote);
                } catch (NoLegalVotes ex) {
                    Logger.getLogger(CryptoNode.class.getName()).log(Level.SEVERE, null, ex);
                } catch (NoSuchAlgorithmException ex) {
                    Logger.getLogger(CryptoNode.class.getName()).log(Level.SEVERE, null, ex);
                } catch (NotEnoughTallies ex) {
                    Logger.getLogger(CryptoNode.class.getName()).log(Level.SEVERE, null, ex);
                }
				dump("local tally:" + localTally);
				
				// update vote vector
				localTallies[getPreviousGroupId()] = localTally;
				
				// broadcast result
				taskManager.registerTask(new GlobalCountingTask(getPreviousGroupId()));
			}
		}
	}

        private class CloseGlobalCountingTask implements Task {
		public void execute() {
			synchronized(LOCK) {
				//actually close the local vote session
				isGlobalCountingOver = true;
				taskManager.registerTask(new TallyDecryptionSharing());
			}
		}
	}
        private class CloseTallyDecryptionSharing implements Task {
		public void execute() {
			synchronized(LOCK) {
				//actually close the Tally Decryption Sharing session
				isDecryptionOver = true;
				taskManager.registerTask(new TallyDecryption());
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
								doSendUDP(new CRYPTO_LOCAL_TALLY_MSG(nodeId, proxyId, localTallies[localTallyGroupId], localTallyGroupId));
							} catch (Exception e) {
								dump("UDP: cannot broadcast local tally");
							}
                                                        break; //only send to one proxy.
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
								doSendTCP(new CRYPTO_INDIVIDUAL_TALLY_MSG(nodeId, peerId, individualTally));
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


	private class TallyDecryptionSharing implements Task {
		public void execute() {
			synchronized(LOCK) {

                                try {
                                    for(Vote groupVote: localTallies) {
                                    finalEncryptedResult = res.CombineVotes(finalEncryptedResult, groupVote.vote);
                                    }
                                    nodeResultShare=tally.Decode(finalEncryptedResult);
                                    resultShares.put(nodeId,nodeResultShare);

                                    } catch (NoLegalVotes ex) {
                                    Logger.getLogger(CryptoNode.class.getName()).log(Level.SEVERE, null, ex);
                                    } catch (NoSuchAlgorithmException ex) {
                                        Logger.getLogger(CryptoNode.class.getName()).log(Level.SEVERE, null, ex);
                                    } catch (NotEnoughTallies ex) {
                                        Logger.getLogger(CryptoNode.class.getName()).log(Level.SEVERE, null, ex);
                                    }


                            synchronized(peerView) {
					if(!peerView.isEmpty()) {
						for(NodeID peerId: peerView) {
							dump("Send decryption share (" + nodeResultShare + ") to " + peerId);
							try {
								doSendTCP(new CRYPTO_DECRYPTION_SHARE_MSG(nodeId, peerId, nodeResultShare));
							} catch (Exception e) {
								dump("TCP: cannot send decryption share");
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
        private class TallyDecryption implements Task {
		public void execute() {
			synchronized(LOCK) {
                try {
                    finalResult = res.DistDecryptVotes((DecodingShare[]) resultShares.values().toArray(), finalEncryptedResult);
                } catch (NoLegalVotes ex) {
                    Logger.getLogger(CryptoNode.class.getName()).log(Level.SEVERE, null, ex);
                } catch (NoSuchAlgorithmException ex) {
                    Logger.getLogger(CryptoNode.class.getName()).log(Level.SEVERE, null, ex);
                } catch (NotEnoughTallies ex) {
                    Logger.getLogger(CryptoNode.class.getName()).log(Level.SEVERE, null, ex);
                }


			}
		}
	}
}