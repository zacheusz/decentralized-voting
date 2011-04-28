package protocol.node;

//import Exception.NoLegalVotes;
//import Exception.NotEnoughTallies;
import protocol.communication.CLUSTER_ASSIGN_MSG;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import launchers.executor.CryptoGossipLauncher;
import paillierp.Paillier;
import paillierp.PaillierThreshold;
import paillierp.PartialDecryption;
import paillierp.key.PaillierKey;
import runtime.executor.E_CryptoNodeID;
import paillierp.testingPaillier.Testing;
import zkp.DecryptionZKP;

public class CryptoNode extends Node {

    // Timeout that are used in the protocol
    public static double DECISION_THRESHOLD = 0.1;								// Required ratio of answers for making a decision
    public static long DECISION_DELAY = 10000;									// Delay before making a decision for localTally
    public static double VOTE_RATIO = 0.5;
    public static double MALICIOUS_RATIO = 0.1;
  //  private final static int BOOTSTRAP_CONTACT_TIMEOUT = 40000;
    //  private static int GET_PEER_VIEW_FROM_BOOTSTRAP_DELAY = 40000;				// Duration of the joining phase: 19 seconds to get peers
    //  private static int GET_PROXY_VIEW_FROM_BOOTSTRAP_DELAY = GET_PEER_VIEW_FROM_BOOTSTRAP_DELAY + 40000;
    //                                1  second  to get proxies
    //  private static int VOTE_DELAY = GET_PROXY_VIEW_FROM_BOOTSTRAP_DELAY + 40000;// Delay before voting: 50 seconds
    //   private static int CLOSE_VOTE_DELAY = 490 * 1000; 				// Duration of the local voting phase: 1 minute
    private static int CLOSE_COUNTING_DELAY = 40 * 1000;		// Duration of the local counting phase: 1 minute
    private static int CLOSE_GLOBAL_COUNTING_DELAY = 40 * 1000;		// Duration of the local counting phase: 1 minute
    private static int CLOSE_DecryptionSharing_DELAY =  40 * 1000;
    private static int CLOSE_ResultDiffusion_DELAY=40* 1000;
//    private static int CLOSE_TallyDecryption_DELAY = CLOSE_DecryptionSharing_DELAY + 20 * 1000;
    private static int SELF_DESTRUCT_DELAY = 500 * 1000;
   // private static int COUNTING_PERIOD = 20 * 1000;		
    
            // Duration of epidemic dissemination: 20 seconds
    public static int VOTECOUNT;
    public static int VOTERCOUNT;
    public static int kvalue;
    public static int MINTALLIES;
    public static int nodesPerMachine;
    public static ClusterChoice nodeToCluster=null;
    public static int chosenCluster;
    public static int numClusters;
    public static Map<E_CryptoNodeID, Integer> IDAssignment = new HashMap<E_CryptoNodeID, Integer>();
    public static Map<E_CryptoNodeID, Integer> finalIDAssignment = new HashMap<E_CryptoNodeID, Integer>();
    Set<E_CryptoNodeID> smallestCluster;
//    public int numRecvClusterAssign = 0;
//    public int numRecvFinalClusterAssign = 0;
//    public ClusterAssignment clusterAssign;
    public boolean IAmThreshold = false;
    public int numPartialTallies = 0;
    public boolean computedLocalTally = false;
    public boolean computedPartialTally = false;
    protected BigInteger partialTally = BigInteger.ONE;
    protected List<BigInteger> partialTallies = new LinkedList<BigInteger>();
    public int numBallots = 0;
    public boolean isResultDiffusionOver = false;
    public int numFinalResults = 0;
    protected List<BigInteger> finalResults = new LinkedList<BigInteger>();
    public boolean computedFinalResult = false;
  //  public static int stepsConstant;
    public static int basicPort;
    public static int nodesPerCluster;
    // Fields
   // protected final E_CryptoNodeID bootstrap;
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
    public static String secKeyFile;
    //protected boolean vote;
//    protected Tally tally;
//    protected Vote vote;
    protected boolean isMalicious;
    protected boolean knownModulation = true;
    protected BigInteger individualTally;
    protected BigInteger localTally;
    //   protected Map<E_CryptoNodeID, BigInteger> individualTallySet = new HashMap<E_CryptoNodeID, BigInteger>();
    //   protected Map<E_CryptoNodeID, BigInteger>[] localTallySets = new Map[E_CryptoNodeID.NB_GROUPS];
    //   protected BigInteger localTallies[] = new BigInteger[E_CryptoNodeID.NB_GROUPS];
    //   protected Result res;
    protected BigInteger finalEncryptedResult = BigInteger.ONE;
    protected BigInteger finalResult = BigInteger.ONE;
    /*protected DecodingShare nodeResultShare;
    protected Map<E_CryptoNodeID, DecodingShare> resultShares = new HashMap<E_CryptoNodeID, DecodingShare>();
    protected DecodingShare[] resultSharesList;*/
    protected PartialDecryption nodeResultShare;
    protected List<PartialDecryption> resultSharesList = new LinkedList<PartialDecryption>();
    //protected List <DecryptionZKP> resultSharesList=new LinkedList<DecryptionZKP>();
    protected int currentDecodingIndex;
    protected int numIndTallies;
    //protected int shareOrder;
    protected int nbSentLocalTallies = 0;
    // Overlay management
    protected boolean receivedPeerView = false;
    protected boolean receivedProxyView = false;
    //     protected boolean receivedClientView = false;
    protected Set<E_CryptoNodeID> peerView = new HashSet<E_CryptoNodeID>();
    protected Set<E_CryptoNodeID> proxyView = new HashSet<E_CryptoNodeID>();
    protected Set<E_CryptoNodeID> clientView = new HashSet<E_CryptoNodeID>();
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
    public CryptoNode(E_CryptoNodeID nodeId, TaskManager taskManager, NetworkSend networkSend, Stopper stopper, PaillierThreshold sec) throws Exception {

        super(nodeId, networkSend);
        this.isMalicious = (Math.random() < MALICIOUS_RATIO);
        //this.vote = (Math.random() < VOTE_RATIO && !isMalicious);

        votes = new BigInteger[VOTECOUNT]; //a vector with same length as the candidates
        int bits;
        BigInteger base, temp;
        int i;
        secKey = sec;
        pubKey = sec.getPublicKey();
        encryptor = new Paillier();
        encryptor.setEncryption(pubKey);

        bits = pubKey.getNS().bitLength() / VOTECOUNT;
        base = (new BigInteger("2")).pow(bits);
        temp = base;
        votes[0] = BigInteger.ONE;

        for (i = 1; i < VOTECOUNT; i++) {
            votes[i] = temp;
            temp = temp.multiply(base);
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
     //   this.bootstrap = bootstrap;
        this.stopper = stopper;
        //
    /*    this.pub = pub;
        this.sec = sec;
        res = new Result(pub);
        tally = new Tally(sec, pub);//returns the distributed key share*/
        this.individualTally = BigInteger.ONE;
        this.localTally = BigInteger.ONE;
        finalEncryptedResult = BigInteger.ONE;
        finalResult = BigInteger.ONE;
        numIndTallies = 0;
        //resultSharesList = new DecodingShare[MINTALLIES];

        currentDecodingIndex = 0;
        //this.shareOrder=shareOrder;

        //         clientsReceived=0;
        //
//        for (i = 0; i < E_CryptoNodeID.NB_GROUPS; i++) {
//            this.localTallySets[i] = new HashMap<E_CryptoNodeID, BigInteger>();
//            this.localTallies[i] = BigInteger.ONE;
//
//        }

        numClusters = (int) (Math.ceil(VOTERCOUNT/ (kvalue * Math.log(VOTERCOUNT))));
        nodesPerCluster = VOTERCOUNT / numClusters;
        MINTALLIES = nodesPerCluster / 2 + 1;
        try {
//            taskManager.registerTask(new AnnouncerTask());
//            taskManager.registerTask(new GetViewFromBootstrapTask(GetViewFromBootstrapTask.PEERS), GET_PEER_VIEW_FROM_BOOTSTRAP_DELAY);
//            taskManager.registerTask(new GetViewFromBootstrapTask(GetViewFromBootstrapTask.PROXIES), GET_PROXY_VIEW_FROM_BOOTSTRAP_DELAY);
//            taskManager.registerTask(new VoteTask(), VOTE_DELAY);
            //     taskManager.registerTask(new PreemptCloseLocalElectionTask(), CLOSE_VOTE_DELAY);
            taskManager.registerTask(new getViews());

//            taskManager.registerTask(new PreemptCloseLocalCountingTask(), CLOSE_COUNTING_DELAY);
//            taskManager.registerTask(new PreemptCloseGlobalCountingTask(), CLOSE_GLOBAL_COUNTING_DELAY);
//            taskManager.registerTask(new PreemptCloseTallyDecryptionSharing(), CLOSE_DecryptionSharing_DELAY);
//            taskManager.registerTask(new PreemptTallyDecryption(), CLOSE_TallyDecryption_DELAY);
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
//                case Message.HITV:
//                    receiveHITV(((HITV_MSG) msg));
//                    break;
                case Message.CRYPTO_BALLOT:
                    receiveBallot((CRYPTO_BALLOT_MSG) msg);
                    break;
                //         case Message.CRYPTO_INDIVIDUAL_TALLY_MSG:
                //             receiveIndividualTally((CRYPTO_INDIVIDUAL_TALLY_MSG) msg);
                //       break;
                case Message.CRYPTO_PARTIAL_TALLY_MSG:
                    receivePartialTally((CRYPTO_PARTIAL_TALLY_MSG) msg);
                    break;
                case Message.CRYPTO_DECRYPTION_SHARE_MSG:
                    receiveDecryptionShare((CRYPTO_DECRYPTION_SHARE_MSG) msg);
                    break;
                case Message.CRYPTO_FINAL_RESULT_MSG:
                    receiveFinalResult((CRYPTO_FINAL_RESULT_MSG) msg);
//                case Message.CLUSTER_ASSIGN_MSG:
//                    receiveClusterAssign((CLUSTER_ASSIGN_MSG) msg);
//                    break;
//                case Message.FINAL_CLUSTER_ASSIGN_MSG:
//                    receiveFinalClusterAssign((FINAL_CLUSTER_ASSIGN_MSG) msg);
//                    break;  
//                 case Message.POSITION_ASSIGN_MSG:
//                    receivePositionDiffusion((POSITION_ASSIGN_MSG) msg);
//                    break;    
//                
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

    private void receiveBallot(CRYPTO_BALLOT_MSG msg) throws NoSuchAlgorithmException {

        if (!isLocalCountingOver) {
            synchronized (LOCK) {
                dump("Received a ballot (" + msg.getVote() + ") from " + msg.getSrc());
                aggrLocalTally(msg.getVote());
            }

        } else {
            dump("Discarded an ballot message (cause: sent too late)");
        }
    }

    private void receiveDecryptionShare(CRYPTO_DECRYPTION_SHARE_MSG msg) throws NoSuchAlgorithmException {
        synchronized (LOCK) {

            if (!isDecryptionSharingOver) {
                
                dump("Received a decryption share (" + msg.getShare() + ") from " + msg.getSrc());

                resultSharesList.add(msg.getShare());

                currentDecodingIndex++;
                dump("sharesize: " + currentDecodingIndex);
                if (isFinalResultCalculated && currentDecodingIndex == MINTALLIES) {
                    dump("CloseTallyDecryptionSharing");
                    //actually close the Tally Decryption Sharing session
                    isDecryptionSharingOver = true;
                    taskManager.registerTask(new TallyDecryption());

                }

            } else {
                dump("Discarded a decryption share message (cause: sent too late)" + " from " + msg.getSrc());
            }

        }
    }

    private void receivePartialTally(CRYPTO_PARTIAL_TALLY_MSG msg) {


        synchronized (LOCK) {

            dump("Received a partial tally (" + msg.getTally() + ") from " + msg.getSrc());
            numPartialTallies++;

            partialTallies.add(msg.getTally());

            if (numPartialTallies == clientView.size()) {
                partialTally = mostPresent(partialTallies);
                computedPartialTally = true;

                if (IAmThreshold) {

                    finalEncryptedResult = partialTally;
                    taskManager.registerTask(new TallyDecryptionSharing());
                } else if (computedLocalTally) {
                    partialTally = encryptor.add(localTally, partialTally);
                    taskManager.registerTask(new GlobalCountingTask());
                }
            }

        }
    }

    private BigInteger mostPresent(List<BigInteger> values) {

        int c, max = 0;
        BigInteger argmax = BigInteger.ONE;

        for (BigInteger i : values) {
            c = 0;
            for (BigInteger j : values) {
                if (j.compareTo(i) == 0) {
                    c++;
                }
            }

            if (c > max) {
                argmax = i;
                max = c;
            }
        }
        return argmax;
    }

    public static List sortByValue(final Map m) {
        List keys = new ArrayList();
        keys.addAll(m.keySet());
        Collections.sort(keys, new Comparator() {

            public int compare(Object o1, Object o2) {
                Object v1 = m.get(o1);
                Object v2 = m.get(o2);
                if (v1 == null) {
                    return (v2 == null) ? 0 : 1;
                } else if (v1 instanceof Comparable) {
                    return ((Comparable) v1).compareTo(v2);
                } else {
                    return 0;
                }
            }
        });
        return keys;
    }

    private class getViews implements Task {

        public void execute() {
            synchronized (LOCK) {
                startInstant = (new Date()).getTime();

                E_CryptoNodeID tempID;
                Map<E_CryptoNodeID, Integer> IDAssignment = new HashMap<E_CryptoNodeID, Integer>();
                List<E_CryptoNodeID> sortedIDs;

                for (int i = 1; i <= VOTERCOUNT / nodesPerMachine; i++) {
                    for (int j = 0; j < nodesPerMachine; j++) {
                        tempID = new E_CryptoNodeID("node" + i, basicPort + j);
                        IDAssignment.put(tempID, tempID.getOrder());
                    }
                }
                sortedIDs = sortByValue(IDAssignment);
                System.out.println(sortedIDs.size());
                nodeToCluster = new ClusterChoice(sortedIDs, nodeId);
                nodeId.groupId = nodeToCluster.myGroupID;

                if (nodeId.groupId == 0) {
                    IAmThreshold = true;
                    
                    secKey=(PaillierThreshold) CryptoGossipLauncher.getObject(secKeyFile+nodeToCluster.keyNum);
                }

                proxyView = nodeToCluster.get((nodeId.groupId + 1) % numClusters);
                peerView = nodeToCluster.get((nodeId.groupId));
                peerView.remove(nodeId);
                clientView = nodeToCluster.get((nodeId.groupId + numClusters - 1) % numClusters);
                taskManager.registerTask(new VoteTask());
            }
        }
    }

    private class VoteTask implements Task {

        public void execute() {
            synchronized (LOCK) {
                taskManager.registerTask(new PreemptCloseLocalCountingTask(), CLOSE_COUNTING_DELAY);
                if (!peerView.isEmpty()) {

                    for (E_CryptoNodeID peerId : peerView) {
                        dump("Send a '" + Emsg + "' ballot to " + peerId);
                        try {

                            doSendTCP(new CRYPTO_BALLOT_MSG(nodeId, peerId, Emsg));
                            break;
                        } catch (Exception e) {
                            dump("TCP: cannot vote");
                        }
                    }
                    isVoteTaskOver = true;
                    taskManager.registerTask(new AttemptSelfDestruct());
                    //     taskManager.registerTask(new CloseVoteTask());
                    aggrLocalTally(Emsg);
                } else {
                    dump("Cannot vote: no peer view");

                }
                taskManager.registerTask(new PreemptGlobalCountingTask(), CLOSE_GLOBAL_COUNTING_DELAY);
            }
        }
    }

    public void aggrLocalTally(BigInteger ballot) {


        localTally = encryptor.add(localTally, ballot);
        numBallots++;
        if (numBallots == peerView.size() + 1) {
            computedLocalTally = true;
            if (IAmThreshold) {
                partialTally = localTally;
                taskManager.registerTask(new GlobalCountingTask());
            } else if (computedPartialTally) {
                partialTally = encryptor.add(localTally, partialTally);
                taskManager.registerTask(new GlobalCountingTask());
            }
            isLocalCountingOver = true;
            //else do nothing
        }



    }

    private class AttemptSelfDestruct implements Task {

        public void execute() {
            //       System.out.println("isGlobalCountingOver:"+isGlobalCountingOver);
            //    System.out.println("isVoteTaskOver:"+isVoteTaskOver);
            //  System.out.println("isIndivSendingOver:"+isIndivSendingOver);
            //System.out.println("isResultOutputed:"+isResultOutputed);
            synchronized (LOCK) {
                if (isGlobalCountingOver && isVoteTaskOver && isLocalCountingOver) {
                    
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
                    taskManager.registerTask(new ResultOutput());
                    
                    
                }
            }


        }
    }

    private class PreemptCloseLocalCountingTask implements Task {

        public void execute() {
            synchronized (LOCK) {
                if (!isLocalCountingOver) {//actually close the local counting session

                    if (IAmThreshold) {
                        partialTally = localTally;
                        taskManager.registerTask(new GlobalCountingTask());
                    } else if (computedPartialTally) {
                        partialTally = encryptor.add(localTally, partialTally);
                        taskManager.registerTask(new GlobalCountingTask());
                        isLocalCountingOver = true;

                    }
                }

            }
        }
    }

    private class PreemptGlobalCountingTask implements Task {

        public void execute() {
            synchronized (LOCK) {
                if (!isGlobalCountingOver) {//actually close the local counting session

                    partialTally = mostPresent(partialTallies);
                    computedPartialTally = true;

                    if (IAmThreshold) {
                        finalEncryptedResult = partialTally;
                        taskManager.registerTask(new TallyDecryptionSharing());
                    } else if (computedLocalTally) {
                        partialTally = encryptor.add(localTally, partialTally);
                        taskManager.registerTask(new GlobalCountingTask());
                    }
                }

            }
        }
    }



    private class PreemptCloseTallyDecryptionSharing implements Task {

        public void execute() {
            synchronized (LOCK) {
                if (!isDecryptionSharingOver) {
                    dump("PreemptCloseTallyDecryptionSharing");

                    //actually close the Tally Decryption Sharing session
                    isDecryptionSharingOver = true;
                    taskManager.registerTask(new TallyDecryption());
                }
            }
        }
    }


    private class GlobalCountingTask implements Task {

        //   private int localTallyGroupId;
        public void execute() {            
            
            // broadcast
            dump("GlobalCountingTask at begin");
            if (!isGlobalCountingOver) {
                synchronized (LOCK) {

                    taskManager.registerTask(new PreemptResultDiffusionTask(), CLOSE_ResultDiffusion_DELAY);                    
                    dump("GlobalCountingTask");

                    for (E_CryptoNodeID proxyId : proxyView) {
                        dump("Send local tally (" + partialTally + ") to " + proxyId);
                        try {
                            doSendTCP(new CRYPTO_PARTIAL_TALLY_MSG(nodeId, proxyId, partialTally));
                        } catch (Exception e) {
                            dump("TCP: cannot broadcast local tally");
                        }
                        break;
                    }

                    isGlobalCountingOver = true;
                    // taskManager.registerTask(new CloseGlobalCountingTask());

                }
                dump("GlobalCountingTask at end");
            }
        }
    }

    private class TallyDecryptionSharing implements Task {

        public void execute() {
            synchronized (LOCK) {
                taskManager.registerTask(new PreemptCloseTallyDecryptionSharing(), CLOSE_DecryptionSharing_DELAY);
                if (!isDecryptionSharingOver) {
                    dump("TallyDecryptionSharing");

                    dump("final encrypted:" + finalEncryptedResult.toString());
                    nodeResultShare = secKey.decrypt(finalEncryptedResult);


                    resultSharesList.add(nodeResultShare);
                    currentDecodingIndex++;
                    isFinalResultCalculated = true;
                    dump("sharesize: " + currentDecodingIndex);


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
                    if (currentDecodingIndex == MINTALLIES) {
                        dump("CloseTallyDecryptionSharing");
                        //actually close the Tally Decryption Sharing session
                        isDecryptionSharingOver = true;
                        taskManager.registerTask(new TallyDecryption());
                    }

                }
            }
        }
    }
    
    private class TallyDecryption implements Task {

        public void execute() {
            synchronized (LOCK) {
                if (!isTallyDecryptionOver) {
                    dump("TallyDecryption");

                    PartialDecryption[] decArray = new PartialDecryption[resultSharesList.size()];
                    for (int i = 0; i < resultSharesList.size(); i++) {
                        decArray[i] = resultSharesList.get(i);
                    }

                    finalResult = secKey.combineShares(decArray);
                    computedFinalResult = true;
                    dump("Determined final result:" + finalResult);

                    isTallyDecryptionOver = true;

                    taskManager.registerTask(new ResultDiffusionTask());



                }
            }
        }
    }

    private class ResultDiffusionTask implements Task {

        public void execute() {
            // broadcast

            if (!isResultDiffusionOver) {
                dump("ResultDiffusionTask at begin");
                
                synchronized (LOCK) {                                        
                    for (E_CryptoNodeID proxyId : proxyView) {
                        dump("Send local tally (" + finalResult + ") to " + proxyId);
                        try {
                            doSendTCP(new CRYPTO_FINAL_RESULT_MSG(nodeId, proxyId, finalResult));
                        } catch (Exception e) {
                            dump("TCP: cannot broadcast local tally");
                        }
                        break;
                    }

                }
                //      }
                dump("ResultDiffusionTask at end");
            }
        }
    }

    private void receiveFinalResult(CRYPTO_FINAL_RESULT_MSG msg) {


        synchronized (LOCK) {
            if (!computedFinalResult) {

                //             synchronized (localTallySets[groupId]) {
                //    synchronized (finalResults) {

                dump("Received a final result (" + msg.getResult() + ") from " + msg.getSrc());
                numFinalResults++;

                finalResults.add(msg.getResult());

                if (numPartialTallies == clientView.size()) {
                    finalResult = mostPresent(finalResults);
                    computedFinalResult = true;

                    if (IAmThreshold) {
                        taskManager.registerTask(new AttemptSelfDestruct());
                    } else {

                        taskManager.registerTask(new ResultDiffusionTask());
                    }
                }

            }

            //         }
        }
    }
   private class PreemptResultDiffusionTask implements Task {

        public void execute() {
            synchronized (LOCK) {
                if (!isResultDiffusionOver) {//actually close the local counting session

                    finalResult = mostPresent(finalResults);
                    computedFinalResult = true;

                    if (IAmThreshold) {
                        taskManager.registerTask(new AttemptSelfDestruct());
                    } else {

                        taskManager.registerTask(new ResultDiffusionTask());
                    }
                }

            }
        }
    }

    private class ResultOutput implements Task {

        public void execute() {
    //        synchronized (LOCK) {
                paillierp.testingPaillier.TestingRest.getResult(finalResult, VOTECOUNT, votes);

                isResultOutputed = true;
                // taskManager.registerTask(new AttemptSelfDestruct());
                taskManager.registerTask(new SelfDestructTask());
      //      }


        }
    }
    //    }
    // **************************************************************************
    // Task handlers
    // **************************************************************************
    //
//
//    private class CloseTallyDecryptionSharing implements Task {
//
//        public void execute() {
//            synchronized (LOCK) {
//                dump("CloseTallyDecryptionSharing");
//
//                //actually close the Tally Decryption Sharing session
//                isDecryptionSharingOver = true;
//                //      taskManager.registerTask(new TallyDecryption());
//            }
//        }
//    }    
//    private class PreemptTallyDecryption implements Task {
//
//        public void execute() {
//            synchronized (LOCK) {
//                if (!isTallyDecryptionOver) {
//                    dump("PreemptTallyDecryption");
//
//                    //actually close the Tally Decryption Sharing session
//                    isTallyDecryptionOver = true;
//                    taskManager.registerTask(new TallyDecryption());
//                }
//            }
//        }
//    }
//    }
//    private class CloseGlobalCountingTask implements Task {
//
//        public void execute() {
//            synchronized (LOCK) {
//                dump("CloseGlobalCountingTask");
//
//                //actually close the local vote session
//                isGlobalCountingOver = true;
//                taskManager.registerTask(new TallyDecryptionSharing());
//            }
//        }
//    }
//
//     //each node announces its cluster chosen in phase 1
//    private class positionAnnouncerTask implements Task {
//
//        public void execute() {
//            //choose the cluster
//            numClusters = (int) ((Math.log(VOTERCOUNT) / 2) / (kvalue * Math.log(VOTERCOUNT / 2)));
//            Random generator = new Random();
//            chosenCluster = generator.nextInt(numClusters);
//
//            //spread the data
//            //chose random node 
//                 
//            nodeToCluster.add(chosenCluster, nodeId);            
//            nodeToCluster.resetSteps();
//            
//            for (int i=0;i<nodeToCluster.steps;i++)
//                taskManager.registerTask(new positionDiffusionTask(),1000*i);      
//                
//            //  startDiffInfo();
//
//
//        }
//    }
//    
//                //each node in sends its position to the others
//    private class positionDiffusionTask  implements Task {
//
//        public void execute() {
//            synchronized (LOCK) {
//                       
//                    E_CryptoNodeID randomNodeID = getRandomNodeID();
//                    try {
//                            doSendTCP(new POSITION_ASSIGN_MSG(nodeId, randomNodeID, nodeToCluster));
//                        } catch (Exception e) {
//                            dump("TCP: cannot send cluster assignment");
//                }
//            }
//        }
//    }
//    
//    
//        //nodes receive each others suggested postitions
//    private void receivePositionDiffusion(POSITION_ASSIGN_MSG msg) throws NoSuchAlgorithmException {
//        synchronized (LOCK) {
//            //synchronized (localTallies) {
//
//            dump("Received a position assignment from " + msg.getSrc());
//
//            ClusterChoice recNodeToCluster = msg.getNodeToCluster();
//            nodeToCluster.mergeClusterChoice(recNodeToCluster);
//            
//            if (nodeToCluster.steps>0)
//                taskManager.registerTask(new positionDiffusionTask());      
//                
//            else
//            {
//                if (nodeToCluster.getNbNodes()==VOTERCOUNT)
//                    taskManager.registerTask(new randomIDAssignerTask());      
//            }
//        }
//    }
//    
//
//    //each node in smallest cluster sends its choices for each node's cluster
//    private class randomIDAssignerTask implements Task {
//
//        public void execute() {
//            synchronized (smallestCluster) {
//
//                if (nodeToCluster.getSmallestCluster() == chosenCluster) {
//                    IAmSmallest=true;
//                    Random generator = new Random();
//                    Set<E_CryptoNodeID> currentCluster;
//                    Map<E_CryptoNodeID, Integer> myIDAssignment = new HashMap<E_CryptoNodeID, Integer>();
//                    int assignedOrder = 0;
//                    Iterator it;
//                    //assign random clusters to nodes
//                    for (int i = 0; i < numClusters; i++) {
//                        currentCluster = nodeToCluster.get(chosenCluster);
//
//                        it = currentCluster.iterator();
//
//                        while (it.hasNext()) {
//                            assignedOrder = generator.nextInt((int) Math.pow(VOTERCOUNT, 3));
//                            myIDAssignment.put((E_CryptoNodeID) it.next(), assignedOrder);
//                        }
//                    }
//
//                    //send choices to smallest cluster's member
//                    smallestCluster = nodeToCluster.get(chosenCluster);
//                    aggrIDAssign(myIDAssignment);
//                    for (E_CryptoNodeID peerId : smallestCluster) {
//                        dump("Send cluster assignment to " + peerId);
//                        try {
//                            doSendTCP(new CLUSTER_ASSIGN_MSG(nodeId, peerId, myIDAssignment));
//                        } catch (Exception e) {
//                            dump("TCP: cannot send cluster assignment");
//                        }
//                    }
//                }
//            }
//        }
//    }
//    
////each node in the smallest cluster diffuses the final assignment to the network
//    private class finalAssignAnnouncerTask implements Task {
//
//        public void execute() {
//            synchronized (LOCK) {
//                //synchronized (localTallies) {
//            }
//        }
//    }
//
//    
////nodes in smallest cluster receieve each others' random assignments
//    private void receiveClusterAssign(CLUSTER_ASSIGN_MSG msg) throws NoSuchAlgorithmException {
//        synchronized (LOCK) {
//            //synchronized (localTallies) {
//
//            dump("Received a cluster assignment from " + msg.getSrc());
//
//            Map<E_CryptoNodeID, Integer> recIDAssign = msg.getIDAssignment();
//            aggrIDAssign(recIDAssign);
//
//            numRecvClusterAssign++;
//
//            if (numRecvClusterAssign == smallestCluster.size() - 1) { 
//                
//                clusterAssign=new ClusterAssignment(IDAssignment);
//                taskManager.registerTask(new finalAssignAnnouncerTask());
//
//            }
//
//        }
//    }
//
//
//
//
//    //each node receives the final assignment as a part of diffusion process
//    private void receiveFinalClusterAssign(FINAL_CLUSTER_ASSIGN_MSG msg) throws NoSuchAlgorithmException {
//        synchronized (LOCK) {
//            //synchronized (localTallies) {
//
//            dump("Received a final cluster assignment from " + msg.getSrc());
//
//            ClusterAssignment recIDAssign = msg.getClusterAssignment();
//      //      mergeIDAssign(recIDAssign);
//
//            numRecvFinalClusterAssign++;
//
//            if (IDAssignment.size() == VOTERCOUNT) {
//                
//                //taskManager.registerTask(new finalAssignAnnouncerTask());
//                
//            }
//
//        }
//    }
//
//
//    private void aggrIDAssign(Map<E_CryptoNodeID, Integer> recIDAssign) {
//        //     synchronized (LOCK) {
//        synchronized (IDAssignment) {
//            if (IDAssignment.isEmpty()) {
//                IDAssignment = recIDAssign;
//            } else {
//                Integer order;
//
//                for (Map.Entry<E_CryptoNodeID, Integer> entry : recIDAssign.entrySet()) {
//                    order = IDAssignment.get(entry.getKey());
//                    if (order == null) {
//                        IDAssignment.put(entry.getKey(), entry.getValue());
//                        smallestCluster.add(entry.getKey());
//                    } else {
//                        IDAssignment.put(entry.getKey(), (entry.getValue() + order) % (int) Math.pow(VOTERCOUNT, 3));
//                    }
//                }
//
//
//            }
//            //       }
//
//        }
//    }
//
//
//    private E_CryptoNodeID getRandomNodeID() {
//
//        Random generator = new Random();
//        int randomHost = generator.nextInt(VOTERCOUNT / nodesPerMachine) + 1;
//        String randomHostName = "node-" + randomHost;
//        int randomPort = generator.nextInt(nodesPerMachine) + nodeId.port;
//
//        E_CryptoNodeID randomNodeID = new E_CryptoNodeID(randomHostName, randomPort, -1);
//
//        return randomNodeID;
//    }
//
//
//
////nodes send their presence to the bootsrap (unused)    
//    private class AnnouncerTask implements Task {
//
//        public void execute() {
//            try {
//                dump("sending to bootstrap: " + bootstrap);
//                doSendUDP(new IAM_MSG(nodeId, bootstrap, getGroupId(), isMalicious));
//            } catch (Exception e) {
//                dump("UDP: cannot announce myself");
//            }
//        }
//    }
//    
//    private class CloseLocalCountingTask implements Task {
//
//        public void execute() {
//            synchronized (LOCK) {
//                dump("CloseLocalCountingTask");
//
//                //actually close the local counting session
//                isLocalCountingOver = true;
//                /*                try {
//                // count
//                localTally = res.CombineVotes(localTally, individualTally);
//                } catch (NoLegalVotes ex) {
//                Logger.getLogger(CryptoNode.class.getName()).log(Level.SEVERE, null, ex);
//                } catch (NoSuchAlgorithmException ex) {
//                Logger.getLogger(CryptoNode.class.getName()).log(Level.SEVERE, null, ex);
//                } catch (NotEnoughTallies ex) {
//                Logger.getLogger(CryptoNode.class.getName()).log(Level.SEVERE, null, ex);
//                }*/
//
//            }
//        }
//    }
}
