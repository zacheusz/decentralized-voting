package protocol.node;

//import Exception.NoLegalVotes;
//import Exception.NotEnoughTallies;
import java.io.IOException;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import protocol.communication.*;
import runtime.NetworkSend;

import runtime.Stopper;
import runtime.Task;
import runtime.TaskManager;

//import OldVoting.*;
import java.math.BigInteger;
import java.util.Date;
import java.util.Random;
import paillierp.Paillier;
import paillierp.PaillierThreshold;
import paillierp.PartialDecryption;
import paillierp.key.PaillierKey;
import runtime.executor.E_CryptoNodeID;
import testingPaillier.Testing;
import zkp.DecryptionZKP;

public class CryptoNode extends Node {

    // Timeout that are used in the protocol
    public static double DECISION_THRESHOLD = 0.1;								// Required ratio of answers for making a decision
    public static long DECISION_DELAY = 10000;									// Delay before making a decision for localTally
    public static double VOTE_RATIO = 0.5;
    public static double MALICIOUS_RATIO = 0.1;
    private final static int BOOTSTRAP_CONTACT_TIMEOUT = 10000;
    private static int GET_PEER_VIEW_FROM_BOOTSTRAP_DELAY = 60000;				// Duration of the joining phase: 19 seconds to get peers
    private static int GET_PROXY_VIEW_FROM_BOOTSTRAP_DELAY = GET_PEER_VIEW_FROM_BOOTSTRAP_DELAY + 25000;
    //                                1  second  to get proxies
    private static int VOTE_DELAY = GET_PROXY_VIEW_FROM_BOOTSTRAP_DELAY + 25000;// Delay before voting: 50 seconds
    private static int CLOSE_VOTE_DELAY = VOTE_DELAY + 490 * 1000; 				// Duration of the local voting phase: 1 minute
    private static int CLOSE_COUNTING_DELAY = CLOSE_VOTE_DELAY + 20 * 1000;		// Duration of the local counting phase: 1 minute
    private static int CLOSE_GLOBAL_COUNTING_DELAY = CLOSE_COUNTING_DELAY + 20 * 1000;		// Duration of the local counting phase: 1 minute
    private static int CLOSE_DecryptionSharing_DELAY = CLOSE_GLOBAL_COUNTING_DELAY + 20 * 1000;
    private static int CLOSE_TallyDecryption_DELAY = CLOSE_DecryptionSharing_DELAY + 20 * 1000;
    private static int SELF_DESTRUCT_DELAY = CLOSE_TallyDecryption_DELAY + 20 * 1000;
    private static int COUNTING_PERIOD = 20 * 1000;								// Duration of epidemic dissemination: 20 seconds
    public static int VOTECOUNT;
    public static int MINTALLIES;
    // Fields
    protected final E_CryptoNodeID bootstrap;
    //Keys
//    PublicKey pub;
//    SecretKey sec;
    // Vote value
    protected boolean hasToken = true;
    protected boolean isLocalVoteOver = false;
    protected boolean isLocalCountingOver = false;
    protected boolean isGlobalCountingOver = false;
    protected boolean isDecryptionSharingOver = false;
    protected boolean isFinalResultCalculated = false;
    protected boolean isTallyDecryptionOver = false;
    protected boolean isVoteTaskOver = false;
    protected boolean isIndivSendingOver = false;
    protected boolean isLocalSendingOver = false;
    protected boolean isResultOutputed = false;

    protected PaillierKey pubKey;
    protected Paillier encryptor;
    protected PaillierThreshold secKey;
    protected BigInteger Emsg;
    BigInteger[] votes;
    //protected boolean vote;
//    protected Tally tally;
//    protected Vote vote;
    protected boolean isMalicious;
    protected boolean knownModulation = true;
    protected BigInteger individualTally;
    protected BigInteger localTally;
    protected Map<E_CryptoNodeID, BigInteger> individualTallySet = new HashMap<E_CryptoNodeID, BigInteger>();
    protected Map<E_CryptoNodeID, BigInteger>[] localTallySets = new Map[E_CryptoNodeID.NB_GROUPS];
    protected BigInteger localTallies[] = new BigInteger[E_CryptoNodeID.NB_GROUPS];
 //   protected Result res;
    protected BigInteger finalEncryptedResult = BigInteger.ZERO;
    protected BigInteger finalResult = BigInteger.ZERO;
    /*protected DecodingShare nodeResultShare;
    protected Map<E_CryptoNodeID, DecodingShare> resultShares = new HashMap<E_CryptoNodeID, DecodingShare>();
    protected DecodingShare[] resultSharesList;*/
    protected PartialDecryption nodeResultShare;
    protected List <PartialDecryption> resultSharesList=new LinkedList<PartialDecryption>();
    //protected List <DecryptionZKP> resultSharesList=new LinkedList<DecryptionZKP>();
    protected int currentDecodingIndex;
    protected int numIndTallies;
    //protected int shareOrder;
    protected int nbSentLocalTallies=0;
    // Overlay management
    protected boolean receivedPeerView = false;
    protected boolean receivedProxyView = false;
    //     protected boolean receivedClientView = false;
    protected List<E_CryptoNodeID> peerView = new LinkedList<E_CryptoNodeID>();
    protected List<E_CryptoNodeID> proxyView = new LinkedList<E_CryptoNodeID>();
    protected List<E_CryptoNodeID> voterView = new LinkedList<E_CryptoNodeID>();
    //protected int clientSize;
    //   protected int clientsReceived=0;
    // Runtime functions
    protected final TaskManager taskManager;
    protected final Stopper stopper;
    // Stats
    public final long startTime;
    public boolean stopped = false;
    
    
    // **************************************************************************
    // Constructors
    // **************************************************************************
    public CryptoNode(E_CryptoNodeID nodeId, TaskManager taskManager, NetworkSend networkSend, Stopper stopper, E_CryptoNodeID bootstrap, PaillierThreshold sec) throws Exception {

        super(nodeId, networkSend);
        this.isMalicious = (Math.random() < MALICIOUS_RATIO);
        //this.vote = (Math.random() < VOTE_RATIO && !isMalicious);

        votes = new BigInteger[VOTECOUNT]; //a vector with same length as the candidates
	int bits;
	BigInteger base, temp;
	int i;
        secKey=sec;
        pubKey=sec.getPublicKey();
        encryptor = new Paillier()  ;
        encryptor.setEncryption(pubKey);
        
        bits = pubKey.getNS().bitLength()/ VOTECOUNT;
	base = (new BigInteger ("2")).pow (bits);
	temp = base;
	votes[0] = BigInteger.ONE;

	for (i = 1; i < VOTECOUNT; i++) {
	    votes[i] = temp;
	    temp = temp.multiply (base);
	}

     
        BigInteger msg = BigInteger.valueOf(1);
        Emsg = encryptor.encrypt(votes[1]);


      /*  Voter voter; //entity voting
        voter = new Voter(pub);
        // Random randomGenerator = new Random();

        //this.vote = voter.Vote(randomGenerator.nextInt(VOTECOUNT + 1));//vote for arbitrary candidate
        this.vote = voter.Vote(0);
*/
        this.taskManager = taskManager;
        this.bootstrap = bootstrap;
        this.stopper = stopper;
        //
    /*    this.pub = pub;
        this.sec = sec;
        res = new Result(pub);
        tally = new Tally(sec, pub);//returns the distributed key share*/
        this.individualTally = BigInteger.ZERO;
        this.localTally = BigInteger.ZERO;
        finalEncryptedResult = BigInteger.ZERO;
        finalResult = BigInteger.ZERO;
        numIndTallies = 0;
        //resultSharesList = new DecodingShare[MINTALLIES];
        
        currentDecodingIndex = 0;
        //this.shareOrder=shareOrder;

        //         clientsReceived=0;
        //
        for ( i = 0; i < E_CryptoNodeID.NB_GROUPS; i++) {
            this.localTallySets[i] = new HashMap<E_CryptoNodeID, BigInteger>();
            this.localTallies[i] = BigInteger.ZERO;

        }

        try {
            taskManager.registerTask(new AnnouncerTask());
            taskManager.registerTask(new GetViewFromBootstrapTask(GetViewFromBootstrapTask.PEERS), GET_PEER_VIEW_FROM_BOOTSTRAP_DELAY);
            taskManager.registerTask(new GetViewFromBootstrapTask(GetViewFromBootstrapTask.PROXIES), GET_PROXY_VIEW_FROM_BOOTSTRAP_DELAY);
            taskManager.registerTask(new VoteTask(), VOTE_DELAY);
            taskManager.registerTask(new PreemptCloseLocalElectionTask(),CLOSE_VOTE_DELAY);
            taskManager.registerTask(new PreemptCloseLocalCountingTask(),CLOSE_COUNTING_DELAY);
            taskManager.registerTask(new PreemptCloseGlobalCountingTask(),CLOSE_GLOBAL_COUNTING_DELAY);
            taskManager.registerTask(new PreemptCloseTallyDecryptionSharing(),CLOSE_DecryptionSharing_DELAY);
            taskManager.registerTask(new PreemptTallyDecryption(),CLOSE_TallyDecryption_DELAY);
            taskManager.registerTask(new SelfDestructTask(), SELF_DESTRUCT_DELAY);
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
                /*                      case Message.HITC:
                receiveHITC((HITC_MSG) msg);
                break;
                 */
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

    for(int i=0;i<E_CryptoNodeID.NB_GROUPS;i++) {
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
            synchronized (peerView) {
                if (msg.getGroupId() == getGroupId()) {
                    receivedPeerView = true;
                    peerView = msg.getView();
                } else if (msg.getGroupId() == getNextGroupId()) {
                    receivedProxyView = true;
                    proxyView = msg.getView();
                } else {
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
    /*	private void receiveHITC(HITC_MSG msg) {
    synchronized (LOCK) {
    //	synchronized(clientSize) {
    if(msg.getGroupId() == getGroupId() ) {
    receivedClientView = true;
    clientSize = msg.getViewSize();
    }

    else {
    receiveSTOP(new STOP_MSG(nodeId, nodeId, "ReceivedHITC: Bad groupId: " + msg.getGroupId()));
    return;
    }
    dump("Received a client size " + msg.getViewSize() + " of group " + msg.getGroupId());
    //				if(msg.knownModulation() && isMalicious) {
    //					knownModulation = true;
    //					dump("I know the modulation");
    //				}
    //	}
    }
    }*/

    private void receiveBallot(CRYPTO_BALLOT_MSG msg) throws  NoSuchAlgorithmException {
        synchronized (LOCK) {
            dump("Received a '" + msg.getVote() + "' ballot from " + msg.getSrc());
            if (!isLocalVoteOver) {
                /*	if(isMalicious && knownModulation && msg.getVote()) {//check this
                dump("Corrupted ballot from " + msg.getSrc());
                individualTally--;
                }
                else {	*/
         //       if (Tally.CheckVote(msg.getVote(), pub)) {
                    //individualTally = res.CombineVotes(individualTally, msg.getVote().vote);

                    individualTally = msg.getVote();
                    //                   clientsReceived++;

                    taskManager.registerTask(new CloseLocalElectionTask());


                    //                 if (receivedClientView && clientsReceived==clientSize)
                    //               {

                    //             }
                    //}
        //        } else {
          //          dump("Received an illegal ballot from " + msg.getSrc());
            //    }

                synchronized (voterView) {
                    if (!voterView.contains(msg.getSrc())) {
                        voterView.add(msg.getSrc());
                    }
                }
            } else {
                dump("Discarded a '" + msg.getVote() + "' ballot from " + msg.getSrc() + " (cause: sent too late)");
            }
        }
    }

       private class receiveSelfIndividualTallyTask implements Task {
		public void execute() {
            try {
                receiveIndividualTally(new CRYPTO_INDIVIDUAL_TALLY_MSG(nodeId, nodeId, individualTally));
            } catch (NoSuchAlgorithmException ex) {
                Logger.getLogger(CryptoNode.class.getName()).log(Level.SEVERE, null, ex);
            }
          
            }
    }
    private void receiveIndividualTally(CRYPTO_INDIVIDUAL_TALLY_MSG msg) throws NoSuchAlgorithmException {
        synchronized (LOCK) {
            synchronized(localTallies)
            {
                if (!isLocalCountingOver) {
                    dump("Received an individual tally (" + msg.getTally() + ") from " + msg.getSrc());
                    //localTally += msg.getTally();
                    //              if(Tally.CheckVote (msg.getTally(),pub)){
                    //         dump("inputs: "+localTally+" "+msg.getTally() );
         //           dump("input1: " + localTally + "\ninput2: " + msg.getTally());

                    localTally = encryptor.add(localTally, msg.getTally());
           //         dump("output: " + localTally);
                    //       dump("current localtally: "+localTally)    ;
                    numIndTallies++;
                    if (numIndTallies == peerView.size() + 1) {
                        dump("local tally:" + localTally);

                        // update vote vector
                        localTallies[getPreviousGroupId()] = localTally;
                        dump("set local tally of group" + getPreviousGroupId());
                        // broadcast result
                        taskManager.registerTask(new GlobalCountingTask(getPreviousGroupId()));
                        taskManager.registerTask(new CloseLocalCountingTask());
                    }

                    //        }

                } else {
                    dump("Discarded an individual tally message (cause: sent too late)");
                }
            }
        }
    }

    private void receiveDecryptionShare(CRYPTO_DECRYPTION_SHARE_MSG msg) throws NoSuchAlgorithmException{
        synchronized (LOCK) {
            
                if (!isDecryptionSharingOver) {
                    dump("Received a decryption share (" + msg.getShare() + ") from " + msg.getSrc());
                    //          dump("Inputs to check share:"+msg.getShare()+" "+finalEncryptedResult);
                    //              if (res.CheckShare(msg.getShare(), finalEncryptedResult)) {
//                dump("Received Share is legal."+ "from " + msg.getSrc());
                 //   resultShares.put(msg.getSrc(), msg.getShare());
          //          if (resultSharesList[msg.getShareOrder()]!=null)
	//		dump("existing order");
	       	
		resultSharesList.add(msg.getShare()) ;

                    currentDecodingIndex++;
                    dump("sharesize: "+currentDecodingIndex);
                //    dump("sharesize: "+resultShares.size());
                    if (isFinalResultCalculated && currentDecodingIndex == MINTALLIES) {
                        taskManager.registerTask(new CloseTallyDecryptionSharing());
                        taskManager.registerTask(new TallyDecryption());

                    }


                    //            }
                    //      else{

                    //             dump("Discarded a decryption share message (cause: not legal)"+" from " + msg.getSrc());
//
                    //          }

                } else {
                    dump("Discarded a decryption share message (cause: sent too late)" + " from " + msg.getSrc());
                }
          
        }
    }

    private void receiveLocalTally(CRYPTO_LOCAL_TALLY_MSG msg) {

        int groupId = msg.getGroupId();

        if (groupId == getPreviousGroupId()) {
            return;
        }

        synchronized (LOCK) {
            if (!isGlobalCountingOver) {

                //             synchronized (localTallySets[groupId]) {
                synchronized (localTallies) {

                    dump("Received a local tally (" + msg.getTally() + ") from " + msg.getSrc());
                    if (localTallies[groupId].equals(BigInteger.ZERO)) {
                        localTallies[groupId] = msg.getTally();
                        dump("set local tally of group" + groupId);
                        
			taskManager.registerTask(new GlobalCountingTask(groupId));

                    }
                    //    dump("Determined local tally (" + localTallies[groupId] + ") for group " + groupId);
                     /*   if (!localTallySets[groupId].containsKey(msg.getSrc())) {

                    dump("Received a local tally (" + msg.getTally() + ") from " + msg.getSrc());
                    localTallySets[groupId].put(msg.getSrc(), msg.getTally());

                    if (localTallies[groupId] == BigInteger.ZERO) {
                    taskManager.registerTask(new GlobalCountingTask(groupId));
                    }

                    localTallies[groupId] = (BigInteger) localTallySets[groupId].values().toArray()[0];//we can't take the most Present since we don't know the decryptions
                    dump("Determined local tally (" + localTallies[groupId] + ") for group " + groupId);
                    }
                     */


                    //                  }

                }
            }
        }
    }
    // **************************************************************************
    // Task handlers
    // **************************************************************************

    private class GetViewFromBootstrapTask implements Task {

        public static final int PEERS = 0, PROXIES = 1;
        private int type;

        public GetViewFromBootstrapTask(int type) {
            this.type = type;
        }

        public void execute() {
            synchronized (LOCK) {
                boolean receivedView = true;
                int groupId;
                switch (type) {
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
		dump ("sending to bootstrap: "+bootstrap);
                doSendUDP(new IAM_MSG(nodeId, bootstrap, getGroupId(), isMalicious));
            } catch (Exception e) {
                dump("UDP: cannot announce myself");
            }
        }
    }

    private class VoteTask implements Task {

        public void execute() {
      //    synchronized (LOCK) {
       //     synchronized (proxyView) {


                if (!proxyView.isEmpty()) {
                    //Vote ballot = vote;
                     startInstant = (new Date ()).getTime ();
                     
                    for (E_CryptoNodeID proxyId : proxyView) {
                        dump("Send a '" + Emsg + "' ballot to " + proxyId);
                        try {
                            /*	if(isMalicious && ballot) {
                            dump("Corrupted vote to " + proxyId);
                            doSendTCP(new CRYPTO_BALLOT_MSG(nodeId, proxyId, !ballot));
                            }
                            else {
                             */
                            doSendTCP(new CRYPTO_BALLOT_MSG(nodeId, proxyId, Emsg));
                            break;
                            //	}
                        } catch (Exception e) {
                            dump("TCP: cannot vote");
                        }
                        //ballot = !ballot;

                          
                    }
                    isVoteTaskOver=true;
                    taskManager.registerTask(new AttemptSelfDestruct());
                } else {
                    dump("Cannot vote: no proxy view");
                }
       //     }
     //  }
        }
    }

    	private class AttemptSelfDestruct implements Task {
		public void execute() {
                      //       System.out.println("isGlobalCountingOver:"+isGlobalCountingOver);
                        //    System.out.println("isVoteTaskOver:"+isVoteTaskOver);
                          //  System.out.println("isIndivSendingOver:"+isIndivSendingOver);
                            //System.out.println("isResultOutputed:"+isResultOutputed);
                    synchronized(LOCK) {
                      if (isGlobalCountingOver&&isVoteTaskOver&&isIndivSendingOver&&isResultOutputed){
/*		       try {
			    doSendTCP(new DEAD_MSG(nodeId, bootstrap));
			    dump("sent a dead message");
			  }catch (Exception e) {
				dump("TCP: cannot send dead message to bootstrap");
			} 
*/

                       // endInstant = (new Date ()).getTime ();
                     //   runningTime=endInstant-startInstant;
                   //     dump("Running Time: "+runningTime);
                        taskManager.registerTask(new SelfDestructTask());
                        }
                    }


                }
    }

    private class CloseLocalElectionTask implements Task {

        public void execute() {
            synchronized (LOCK) {
          
                    //actually close the local vote session
                    dump("CloseLocalElectionTask");
                    isLocalVoteOver = true;
                try {
                    //	dump("tally=" + ((individualTally>0)?"+":"") + individualTally);
                    // schedule local counting
                    //   taskManager.registerTask(new receiveSelfIndividualTallyTask());
                    receiveIndividualTally(new CRYPTO_INDIVIDUAL_TALLY_MSG(nodeId, nodeId, individualTally));
            
                } catch (NoSuchAlgorithmException ex) {
                    Logger.getLogger(CryptoNode.class.getName()).log(Level.SEVERE, null, ex);
            
                }

                    taskManager.registerTask(new LocalCounting()); //, ((long) (Math.random() * COUNTING_PERIOD)));
             
            }
        }
    }
	private class PreemptCloseLocalElectionTask implements Task {
		public void execute() {
			synchronized(LOCK) {
                                if (!isLocalVoteOver)
                                {//actually close the local counting session
				isLocalVoteOver = true;

				taskManager.registerTask(new CloseLocalElectionTask());
                          }

			}
		}
	}

          private class PreemptCloseLocalCountingTask implements Task {
        public void execute() {
                synchronized(LOCK) {
                        if (!isLocalCountingOver)
                        {//actually close the local counting session
                        isLocalCountingOver = true;

                        taskManager.registerTask(new CloseLocalCountingTask());
                  }

                }
        }
}
         private class PreemptCloseGlobalCountingTask implements Task {
        public void execute() {
                synchronized(LOCK) {
                        if (!isGlobalCountingOver)
                        {//actually close the local counting session
                        isGlobalCountingOver = true;

                         taskManager.registerTask(new CloseGlobalCountingTask ());
                  }

                }
        }
}
    private class CloseLocalCountingTask implements Task {

        public void execute() {
            synchronized (LOCK) {
                dump("CloseLocalCountingTask");

                //actually close the local counting session
                isLocalCountingOver = true;
                /*                try {
                // count
                localTally = res.CombineVotes(localTally, individualTally);
                } catch (NoLegalVotes ex) {
                Logger.getLogger(CryptoNode.class.getName()).log(Level.SEVERE, null, ex);
                } catch (NoSuchAlgorithmException ex) {
                Logger.getLogger(CryptoNode.class.getName()).log(Level.SEVERE, null, ex);
                } catch (NotEnoughTallies ex) {
                Logger.getLogger(CryptoNode.class.getName()).log(Level.SEVERE, null, ex);
                }*/

            }
        }
    }

    private class CloseGlobalCountingTask implements Task {

        public void execute() {
            synchronized (LOCK) {
                dump("CloseGlobalCountingTask");

                //actually close the local vote session
                isGlobalCountingOver = true;
                taskManager.registerTask(new TallyDecryptionSharing());
            }
        }
    }

    private class CloseTallyDecryptionSharing implements Task {

        public void execute() {
            synchronized (LOCK) {
                dump("CloseTallyDecryptionSharing");

                //actually close the Tally Decryption Sharing session
                isDecryptionSharingOver = true;
                //      taskManager.registerTask(new TallyDecryption());
            }
        }
    }
    private class PreemptCloseTallyDecryptionSharing implements Task {

        public void execute() {
            synchronized (LOCK) {
                if (!isDecryptionSharingOver){
                dump("PreemptCloseTallyDecryptionSharing");

                //actually close the Tally Decryption Sharing session
                isDecryptionSharingOver = true;
                taskManager.registerTask(new CloseTallyDecryptionSharing());
            }
        }
    }
    }
        private class PreemptTallyDecryption implements Task {

        public void execute() {
            synchronized (LOCK) {
                if (!isTallyDecryptionOver){
                dump("PreemptTallyDecryption");

                //actually close the Tally Decryption Sharing session
                isTallyDecryptionOver = true;
                taskManager.registerTask(new TallyDecryption());
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
           dump("GlobalCountingTask at begin");

            synchronized (LOCK) {
         //       synchronized (proxyView) {
                    if(!isGlobalCountingOver){
                    
                        dump("GlobalCountingTask");
                        nbSentLocalTallies++;
			   if (localTallyGroupId != nodeId.groupId) { //we don't send this value to the next group since it is the originator

                        for (E_CryptoNodeID proxyId : proxyView) {
                            dump("Send local tally (" + localTallies[localTallyGroupId] + ") to " + proxyId);
                            try {
                                doSendUDP(new CRYPTO_LOCAL_TALLY_MSG(nodeId, proxyId, localTallies[localTallyGroupId], localTallyGroupId));
                            } catch (Exception e) {
                                dump("UDP: cannot broadcast local tally");
                            }
                            break; //only send to one proxy.
                        }
			}	
                        //check if the node has all the groups' tallies
                  //      boolean done = true;
//                        for (BigInteger mytally : localTallies) {
//
//                            if (mytally.equals(BigInteger.ZERO)) {
//                                done = false;
//                                break;
//                            }
//                        }
			dump("nbSentLocalTallies: "+nbSentLocalTallies);
                        if (nbSentLocalTallies==nodeId.NB_GROUPS) {			
                            taskManager.registerTask(new CloseGlobalCountingTask());
                            isGlobalCountingOver=true;
                            taskManager.registerTask(new AttemptSelfDestruct());
                        }
                        else
                            dump("still not done");
                    }
                }
      //      }
		dump("GlobalCountingTask at end");
        }
    }

    private class LocalCounting implements Task {

        public void execute() {
            synchronized (LOCK) {
             //   synchronized (peerView) {
                    dump("LocalCounting");

                    if (!peerView.isEmpty()) {
                        for (E_CryptoNodeID peerId : peerView) {
                            dump("Send individual tally (" + individualTally + ") to " + peerId);
                            try {
                                doSendTCP(new CRYPTO_INDIVIDUAL_TALLY_MSG(nodeId, peerId, individualTally));
                            } catch (Exception e) {
                                dump("TCP: cannot send individual tally");
                            }
                        }
                          isIndivSendingOver=true;
                          taskManager.registerTask(new AttemptSelfDestruct());
                    } else {
                        receiveSTOP(new STOP_MSG(nodeId, nodeId, "cannot count: no peer view"));
                    }
                }
        //    }
        }
    }

    private class TallyDecryptionSharing implements Task {

        public void execute() {
            synchronized (LOCK) {
                
                    if (!isDecryptionSharingOver) {
                        dump("TallyDecryptionSharing");
                        finalEncryptedResult=BigInteger.ZERO;
                        
                            for (BigInteger mytally : localTallies) {

                         //       dump("input1: " + finalEncryptedResult + "\ninput2: " + mytally);
                                finalEncryptedResult =encryptor.add( finalEncryptedResult,mytally);
                           //     dump("output: " + finalEncryptedResult);
                            }

                            dump("final encrypted:" + finalEncryptedResult.toString());
                            nodeResultShare = secKey.decrypt(finalEncryptedResult);
                          //    resultShares.put(nodeId, nodeResultShare);
		//	  if (resultSharesList[shareOrder]!=null)
                  //      	dump("existing order");

                            resultSharesList.add(nodeResultShare);
                            currentDecodingIndex++;
                            isFinalResultCalculated = true;
                            dump("sharesize: "+currentDecodingIndex);





                   

//                        synchronized (peerView) {
                            if (!peerView.isEmpty()) {
                                for (E_CryptoNodeID peerId : peerView) {
                                    dump("Send decryption share (" + nodeResultShare + ") to " + peerId);
                                    try {
                                        doSendTCP(new CRYPTO_DECRYPTION_SHARE_MSG(nodeId, peerId, nodeResultShare));
                                    } catch (Exception e) {
                                        dump("TCP: cannot send decryption share");
                                    }
                                }

                            } else {
                                receiveSTOP(new STOP_MSG(nodeId, nodeId, "cannot share result share: no peer view"));
                            }

                        //}
                        if (currentDecodingIndex==MINTALLIES) {
                            taskManager.registerTask(new CloseTallyDecryptionSharing());
                            isDecryptionSharingOver=true;
                            taskManager.registerTask(new TallyDecryption());

                        }
                    
                }
            }
        }
    }

    private class TallyDecryption implements Task {

        public void execute() {
            synchronized (LOCK) {
            if (!isTallyDecryptionOver){
                dump("TallyDecryption");

                
                   //         DecodingShare[] shares = (DecodingShare[]) resultShares.values().toArray(new DecodingShare[resultShares.size()]);
                    //    dump("size: "+currentDecodingIndex);
                 /*   for ( PartialDecryption sh : resultSharesList) {
                        if (sh.verify(finalEncryptedResult)) {
                            dump("share ok");
                        } else {
                            dump("bad share");
                        }
                    }*/
                    //dump("final input: "+finalEncryptedResult.toString());


                        //finalResult = secKey.combineShares((DecryptionZKP[]) resultSharesList.toArray());
                     
                            PartialDecryption[] decArray=new PartialDecryption[resultSharesList.size()] ;
                            for (int i=0;i<resultSharesList.size();i++)
                                decArray[i]=resultSharesList.get(i);
                            
                          finalResult = secKey.combineShares(decArray);
                          
                    dump("Determined final result:" + finalResult);
      //              try {
        //                Thread.currentThread().sleep(10000);
          //          } catch (InterruptedException ex) {
            //            Logger.getLogger(CryptoNode.class.getName()).log(Level.SEVERE, null, ex);
              //      }
                        isTallyDecryptionOver=true;
                        taskManager.registerTask(new ResultOutput());

             

                }
            }
        }
    }

   private class ResultOutput implements Task {

        public void execute() {
            synchronized (LOCK) {
            Testing.getResult(finalResult, VOTECOUNT, votes);
                try {
                    doSendTCP(new DEAD_MSG(nodeId, bootstrap));
                } catch (UnknownHostException ex) {
                    Logger.getLogger(CryptoNode.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(CryptoNode.class.getName()).log(Level.SEVERE, null, ex);
                }
            isResultOutputed=true;
            taskManager.registerTask(new AttemptSelfDestruct());
            }


}
}
}
