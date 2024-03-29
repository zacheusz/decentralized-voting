package protocol.node;

//import Exception.NoLegalVotes;
//import Exception.NotEnoughTallies;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
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

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;

import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import launchers.executor.CryptoGossipLauncher;
import paillierp.Paillier;
import paillierp.PaillierThreshold;
import paillierp.PartialDecryption;
import paillierp.key.PaillierKey;
import runtime.executor.E_CryptoNodeID;

import protocol.communication.ClusterChoice;

public class CryptoNode extends Node {

    // Timeout that are used in the protocol
    public static double DECISION_THRESHOLD = 0.1;								// Required ratio of answers for making a decision
    public static long DECISION_DELAY = 10000;									// Delay before making a decision for localTally
    public static double VOTE_RATIO = 0.5;
    public static double epsilon;
    public static double MALICIOUS_RATIO;
    //  private final static int BOOTSTRAP_CONTACT_TIMEOUT = 40000;
    //  private static int GET_PEER_VIEW_FROM_BOOTSTRAP_DELAY = 40000;				// Duration of the joining phase: 19 seconds to get peers
    //  private static int GET_PROXY_VIEW_FROM_BOOTSTRAP_DELAY = GET_PEER_VIEW_FROM_BOOTSTRAP_DELAY + 40000;
    //                                1  second  to get proxies
    public static int VOTECOUNT;
    public static int VOTERCOUNT;
    private static int VOTE_DELAY = 80 * 1000;// Delay before voting: 50 seconds
    private static int VIEW_DIFF_DELAY = 120 * 1000;
    //   private static int CLOSE_VOTE_DELAY = 490 * 1000; 				// Duration of the local voting phase: 1 minute
    private static int CLOSE_COUNTING_DELAY = 3200 * 1000;		// Duration of the local counting phase: 1 minute
    private static int CLOSE_PARTIAL_TALLYING_DELAY = CLOSE_COUNTING_DELAY + 3200 * 1000;		// Duration of the local counting phase: 1 minute
    private static int CLOSE_DecryptionSharing_DELAY = 3200 * 1000;
    private static int CLOSE_ResultDiffusion_DELAY = 3200 * 1000;
//    private static int CLOSE_TallyDecryption_DELAY = CLOSE_DecryptionSharing_DELAY + 20 * 1000;
//   private static int SELF_DESTRUCT_DELAY = 15000 * 1000;
    // private static int COUNTING_PERIOD = 20 * 1000;		
    // Duration of epidemic dissemination: 20 seconds
    public static int kvalue;
    public static int MINTALLIES;
    public static int nodesPerMachine;
    public static ClusterChoice nodeToCluster = null;
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
    Random generator = new Random();
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
    public static boolean isMalicious;
    public static int order;
    public static int numReceivedViews = 0;
    public static double threshold = 0.9;
    public static boolean receivedAllViews = false;
    public static boolean isViewDiffusionOver = false;
    public static boolean isFirstView = true;
    public static long startViewTime = 0;
    public static long viewDuration = 0;
    // Fields
    // protected final E_CryptoNodeID bootstrap;
    //Keys
//    PublicKey pub;
//    SecretKey sec;
    // Vote value
    protected static boolean hasToken = true;
    protected static boolean isLocalVoteOver = false;
    protected static boolean isLocalCountingOver = false;
    protected static boolean IsPartialTallyingOver = false;
    protected static boolean isShareSendingOver = false;
    protected static boolean startedShareSending = false;
    protected static boolean isDecryptionSharingOver = false;
    protected static boolean isFinalResultCalculated = false;
    protected static boolean isTallyDecryptionOver = false;
    protected static boolean isVoteTaskOver = false;
    protected static boolean isIndivSendingOver = false;
    protected static boolean isLocalSendingOver = false;
    protected static boolean isResultOutputed = false;
    protected static PaillierKey pubKey;
    protected static Paillier encryptor;
    protected static PaillierThreshold secKey;
    protected static BigInteger Emsg;
    public static int mycount = 0;
    public static BigInteger[] votes;
    public static String secKeyFile;
    public int MSView = 0;
    public int MRView = 0;
    public int MSVote = 0;
    public int MRBallot = 0;
    public int MSPartial = 0;
    public int MRPartial = 0;
    public int MSShare = 0;
    public int MRShare = 0;
    public int MSResult = 0;
    public int MRResult = 0;
    public int MRKeys = 0;
    public int SMRKeys = 0;
    public double SMSView = 0;
    public double SMRView = 0;
    public double SMSVote = 0;
    public double SMRBallot = 0;
    public double SMSPartial = 0;
    public double SMRPartial = 0;
    public double SMSShare = 0;
    public double SMRShare = 0;
    public double SMSResult = 0;
    public double SMRResult = 0;
    public long TallyAggTime = 0;
    public long VoteEncTime = 0;
    public long ShareCompTime = 0;
    public long VoteDecTime = 0;
    //protected boolean vote;
//    protected Tally tally;
//    protected Vote vote;
    protected boolean knownModulation = true;
    protected static BigInteger individualTally;
    protected static BigInteger localTally;
    //   protected Map<E_CryptoNodeID, BigInteger> individualTallySet = new HashMap<E_CryptoNodeID, BigInteger>();
    //   protected Map<E_CryptoNodeID, BigInteger>[] localTallySets = new Map[E_CryptoNodeID.NB_GROUPS];
    //   protected BigInteger localTallies[] = new BigInteger[E_CryptoNodeID.NB_GROUPS];
    //   protected Result res;
    protected static BigInteger finalEncryptedResult = BigInteger.ONE;
    protected static BigInteger finalResult = BigInteger.ONE;
    /*protected DecodingShare nodeResultShare;
    protected Map<E_CryptoNodeID, DecodingShare> resultShares = new HashMap<E_CryptoNodeID, DecodingShare>();
    protected DecodingShare[] resultSharesList;*/
    protected static PartialDecryption nodeResultShare;
    protected static List<PartialDecryption> resultSharesList = new LinkedList<PartialDecryption>();
    //protected List <DecryptionZKP> resultSharesList=new LinkedList<DecryptionZKP>();
    protected static int currentDecodingIndex;
    protected static int numIndTallies;
    //protected int shareOrder;
    protected static int nbSentLocalTallies = 0;
    // Overlay management
    protected static boolean receivedPeerView = false;
    protected static boolean receivedProxyView = false;
    //     protected boolean receivedClientView = false;
    protected static Set<E_CryptoNodeID> peerView = new HashSet<E_CryptoNodeID>();
    protected static Set<E_CryptoNodeID> proxyView = new HashSet<E_CryptoNodeID>();
    protected static Set<E_CryptoNodeID> clientView = new HashSet<E_CryptoNodeID>();
    //protected int clientSize;
    //   protected int clientsReceived=0;
    // Runtime functions
    protected final TaskManager taskManager;
    protected final Stopper stopper;
    // Stats
    public final long startTime;
    public boolean stopped = false;
    public double threshOrder;
    protected final Object BROADCASTLOCK = new Object();
    protected final Object SHARESENDINGLOCK = new Object();
    protected final Object VOTESENDINGLOCK = new Object();
    protected BROADCAST_MSG broadcastMsg;
    protected Map<E_CryptoNodeID, ArrayList<MutableInt>> echoCountMap = new HashMap<E_CryptoNodeID, ArrayList<MutableInt>>();
    protected Map<E_CryptoNodeID, ArrayList<MutableInt>> readyCountMap = new HashMap<E_CryptoNodeID, ArrayList<MutableInt>>();
    protected Map<E_CryptoNodeID, ArrayList<Boolean>> readyMap = new HashMap<E_CryptoNodeID, ArrayList<Boolean>>();
    protected Map<E_CryptoNodeID, ArrayList<Boolean>> deliveredMap = new HashMap<E_CryptoNodeID, ArrayList<Boolean>>();
    public static int sequenceNumber = 0;
    public static int receivedCount = 0;
    public static int receivedCount2 = 0;
    public static int SENDING_INTERVAL = 10;
    public static int MINI_SENDING_INTERVAL = 2;
    public static int INTERBROADCAST_INTERVAL = 15;
    //public static int nodeOrder = 0;
    public static boolean readyToSend = true;

    // **************************************************************************
    // Constructors
    // **************************************************************************
    public CryptoNode(E_CryptoNodeID nodeId, TaskManager taskManager, NetworkSend networkSend, Stopper stopper) throws Exception {

        super(nodeId, networkSend);
        MALICIOUS_RATIO = 0.5 - epsilon;
        //    this.isMalicious = (Math.random() < MALICIOUS_RATIO);

        //this.vote = (Math.random() < VOTE_RATIO && !isMalicious);

        E_CryptoNodeID tempID = null;
        mycount = 0;
        threshOrder = (0.5 - epsilon) * VOTERCOUNT;



        for (int i = 1; i <= VOTERCOUNT / nodesPerMachine; i++) {
            for (int j = 0; j < nodesPerMachine; j++) {

                tempID = new E_CryptoNodeID("node-" + i, basicPort + j, false);

                if (nodeId.equals(tempID)) {
                    dump("keynum: " + mycount);
                    secKey = (PaillierThreshold) CryptoGossipLauncher.getObject(secKeyFile + mycount);
                    nodeId.nodeOrder = mycount;
                    if (secKey == null) {
                        taskManager.registerTask(new SelfDestructTask());
                    }
                    //dump ("mycount: "+mycount+"threshOrder: "+threshOrder);
                    nodeId.isMalicious = (mycount + 1 < threshOrder);

                }
                peerView.add(tempID);
                mycount++;


            }
        }
        dump("peerview: ");
        for (E_CryptoNodeID loopID : peerView) {
            dump(loopID.toString());
        }




        votes = new BigInteger[VOTECOUNT]; //a vector with same length as the candidates
        int bits;
        BigInteger base, temp;
        int i;

        pubKey = secKey.getPublicKey();
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

        long startT = System.nanoTime();
        Emsg = encryptor.encrypt(votes[0]);
        VoteEncTime += System.nanoTime() - startT;

//        votes = new BigInteger[VOTECOUNT]; //a vector with same length as the candidates
//        int bits;
//        BigInteger base, temp;
//        int i;
//        secKey = sec;
//        pubKey = sec.getPublicKey();
//        encryptor = new Paillier();
//        encryptor.setEncryption(pubKey);
//        
//        bits = pubKey.getNS().bitLength() / VOTECOUNT;
//        base = (new BigInteger("2")).pow(bits);
//        temp = base;
//        votes[0] = BigInteger.ONE;
//        
//        for (i = 1; i < VOTECOUNT; i++) {
//            votes[i] = temp;
//            temp = temp.multiply(base);
//        }
//
//
//
//        //    if (isMalicious) {
//        long startT = System.nanoTime();
//        Emsg = encryptor.encrypt(votes[1]);
//        VoteEncTime += System.nanoTime() - startT;
        //    } else {
        //        Emsg = encryptor.encrypt(votes[1]);
        //    }

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

        numClusters = (int) (Math.ceil(VOTERCOUNT / (kvalue * Math.log(VOTERCOUNT))));
        nodesPerCluster = (int) (Math.ceil(VOTERCOUNT * 1.0 / numClusters));
        while (nodesPerCluster * numClusters >= VOTERCOUNT) {
            numClusters--;
        }
        numClusters++;
//        double test = Math.floor(VOTERCOUNT / numClusters);
//        if (test < (1.0 * VOTERCOUNT / numClusters)) {
//            numClusters++;
//        }
//        nodesPerCluster = (int) test;

        MINTALLIES = VOTERCOUNT / 2 + 1;
        Random generator = new Random();
        VOTE_DELAY = 15 + 1000 * VOTERCOUNT / 2000 * 1000 + generator.nextInt(VOTERCOUNT * 20);


//
        //  System.out.println("min:" + MINTALLIES);
        try {
//            taskManager.registerTask(new AnnouncerTask());
//            taskManager.registerTask(new GetViewFromBootstrapTask(GetViewFromBootstrapTask.PEERS), GET_PEER_VIEW_FROM_BOOTSTRAP_DELAY);
//            taskManager.registerTask(new GetViewFromBootstrapTask(GetViewFromBootstrapTask.PROXIES), GET_PROXY_VIEW_FROM_BOOTSTRAP_DELAY);
//            taskManager.registerTask(new VoteTask(), VOTE_DELAY);
            //     taskManager.registerTask(new PreemptCloseLocalElectionTask(), CLOSE_VOTE_DELAY);
            //  taskManager.registerTask(new VoteTask(), VOTE_DELAY + (nodeId.nodeOrder / 10) * 150 * 1000);
            if (nodeId.nodeOrder == 0) {
                taskManager.registerTask(new VoteTask(), VOTE_DELAY);
            }
//            taskManager.registerTask(new PreemptCloseLocalCountingTask(), CLOSE_COUNTING_DELAY);
//            taskManager.registerTask(new PreemptCloseGlobalCountingTask(), CLOSE_GLOBAL_COUNTING_DELAY);
//            taskManager.registerTask(new PreemptCloseTallyDecryptionSharing(), CLOSE_DecryptionSharing_DELAY);
//            taskManager.registerTask(new PreemptTallyDecryption(), CLOSE_TallyDecryption_DELAY);
            taskManager.registerTask(new SelfDestructTask(), SELF_DESTRUCT_DELAY);
        } catch (Error e) {
            dump(nodeId + ": " + e.getMessage());
            e.printStackTrace();
        }
        dump("Node " + nodeId.getName() + " is born: ");
        //  dump("Parameters: Vote Ratio=" + VOTE_RATIO);
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
//                case Message.CRYPTO_BALLOT:
//                    receiveBallot((BROADCAST_MSG) msg);
//                    break;
                case Message.BROADCAST_DATA_MSG:
                    receiveBroadcast((BROADCAST_MSG) msg);

                    break;
                //         case Message.CRYPTO_INDIVIDUAL_TALLY_MSG:
                //             receiveIndividualTally((CRYPTO_INDIVIDUAL_TALLY_MSG) msg);
                //       break;
//                case Message.CRYPTO_PARTIAL_TALLY_MSG:
//                    receivePartialTally((CRYPTO_PARTIAL_TALLY_MSG) msg);
//                    break;
//                case Message.CRYPTO_DECRYPTION_SHARE_MSG:
//                    receiveDecryptionShare((CRYPTO_DECRYPTION_SHARE_MSG) msg);
//                    break;
//                case Message.CRYPTO_FINAL_RESULT_MSG:
//                    receiveFinalResult((CRYPTO_FINAL_RESULT_MSG) msg);
//                    break;
//                case Message.CRYPTO_VIEW_MSG:
//                    receiveView((CRYPTO_VIEW_MSG) msg);
//                    break;

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

    class MutableInt {

        int value = 0;

        public void inc() {
            ++value;
        }

        public int get() {
            return value;
        }
    }

    private void receiveShareDataMsg(BROADCAST_MSG msg) throws NoSuchAlgorithmException {

        //if (!isLocalCountingOver) {
        //    synchronized (BROADCASTLOCK) {

        dump("Received a share data message from " + msg.getSrc() + " with actual src: " + msg.getInfo().actualSrc);
        Random generator = new Random();
        taskManager.registerTask(new BroadcastTask(new BroadcastInfo(msg.getInfo().share, null, Message.SHARE_ECHO_MSG, msg.getSrc(), msg.getInfo().seqNum)), generator.nextInt(SENDING_INTERVAL));

        //     }
        //} else {
        //  dump("Discarded an ballot message (cause: sent too late)");
        // }
    }

    private void receiveShareEchoMsg(BROADCAST_MSG msg) throws NoSuchAlgorithmException {

        //if (!isLocalCountingOver) {
        synchronized (readyMap) {
            dump("Received a share echo message from " + msg.getSrc() + " with actual src: " + msg.getInfo().actualSrc);
            Random generator = new Random();

            if (!msg.getSrc().isMalicious) {

                E_CryptoNodeID actualSrc = msg.getInfo().actualSrc;
                int seqNum = msg.getInfo().seqNum;
                ArrayList<MutableInt> countList = echoCountMap.get(actualSrc);
                ArrayList<Boolean> readyList = readyMap.get(actualSrc);
                boolean sentReady = false;

                if ((readyList == null) || readyList.isEmpty()) {
                    readyList = new ArrayList<Boolean>();
                    readyList.add(Boolean.FALSE);
                    readyList.add(Boolean.FALSE);
                } else {
                    sentReady = readyList.get(seqNum);
                }


                if ((countList == null) || countList.isEmpty()) {
                    countList = new ArrayList<MutableInt>();
                    countList.add(new MutableInt());
                    countList.add(new MutableInt());
                }

                countList.get(seqNum).inc();
                echoCountMap.put(actualSrc, countList);
                dump("echoCount (" + actualSrc + "): " + countList.get(seqNum).value);

                if (countList.get(seqNum).value > Math.floor(VOTERCOUNT * (1 + MALICIOUS_RATIO)*threshold / 2) && !sentReady) {
                    taskManager.registerTask(new BroadcastTask(new BroadcastInfo(msg.getInfo().share, null, Message.SHARE_READY_MSG, actualSrc, msg.getInfo().seqNum)), generator.nextInt(SENDING_INTERVAL));
                    readyList.set(seqNum, Boolean.TRUE);
                    readyMap.put(actualSrc, readyList);
                }
            } else {
                dump("node " + msg.getSrc() + " is malicious");
            }
        }

    }

    private void receiveShareReadyMsg(BROADCAST_MSG msg) throws NoSuchAlgorithmException {

        synchronized (readyMap) {
            dump("Received a share ready message from " + msg.getSrc() + " with actual src: " + msg.getInfo().actualSrc);
            Random generator = new Random();

            if (!msg.getSrc().isMalicious) {

                E_CryptoNodeID actualSrc = msg.getInfo().actualSrc;
                int seqNum = msg.getInfo().seqNum;
                ArrayList<MutableInt> countList = readyCountMap.get(actualSrc);
                ArrayList<Boolean> readyList = readyMap.get(actualSrc);
                boolean sentReady = false;
                boolean delivered = false;
                ArrayList<Boolean> deliveredList = deliveredMap.get(actualSrc);


                if ((deliveredList == null) || deliveredList.isEmpty()) {
                    deliveredList = new ArrayList<Boolean>();
                    deliveredList.add(Boolean.FALSE);
                    deliveredList.add(Boolean.FALSE);
                } else {
                    delivered = deliveredList.get(seqNum);
                }
                if (delivered) {
                    //add statistics
                    return;
                }

                if ((readyList == null) || readyList.isEmpty()) {
                    readyList = new ArrayList<Boolean>();
                    readyList.add(Boolean.FALSE);
                    readyList.add(Boolean.FALSE);
                } else {
                    sentReady = readyList.get(seqNum);
                }


                if ((countList == null) || countList.isEmpty()) {
                    countList = new ArrayList<MutableInt>();
                    countList.add(new MutableInt());
                    countList.add(new MutableInt());
                }

                countList.get(seqNum).inc();
                readyCountMap.put(actualSrc, countList);
                dump("readyCount (" + actualSrc + "): " + countList.get(seqNum).value);

                if (countList.get(seqNum).value > Math.floor(VOTERCOUNT * MALICIOUS_RATIO)*threshold && !sentReady) {
                    taskManager.registerTask(new BroadcastTask(new BroadcastInfo(msg.getInfo().share, null, Message.SHARE_READY_MSG, actualSrc, msg.getInfo().seqNum)), generator.nextInt(SENDING_INTERVAL));
                    readyList.set(seqNum, Boolean.TRUE);
                    readyMap.put(actualSrc, readyList);
                }

                if (countList.get(seqNum).value > Math.floor(2 * VOTERCOUNT * MALICIOUS_RATIO*threshold)) {
                    deliveredList.set(seqNum, true);
                    deliveredMap.put(actualSrc, deliveredList);
                    dump("delivered a share message " + msg.getInfo().share + " from (" + actualSrc);
                    if (!actualSrc.isMalicious)
                        receiveDecryptionShare(msg);
                    else
                        dump("but discared the share message " + msg.getInfo().share + " from malicious (" + actualSrc);
                    
                    if ((nodeId.nodeOrder == msg.getInfo().actualSrc.nodeOrder + 1)&&(!isShareSendingOver)) {
                        taskManager.registerTask(new TallyDecryptionSharing(), generator.nextInt(INTERBROADCAST_INTERVAL));
                        dump("launched new sharing session for node-" + (nodeId.nodeOrder + 1));
                    }

                    readyToSend = true;
                }

            }
        }


    }

    private void receiveVoteDataMsg(BROADCAST_MSG msg) throws NoSuchAlgorithmException {

        //if (!isLocalCountingOver) {
        synchronized (BROADCASTLOCK) {
            
            if (nodeId.nodeOrder!=0)
                startInstant = System.nanoTime();
            else 
                dump ("\r"+"transfer time: "+(System.nanoTime() - startInstant)+ "\r");
                   
            dump("Received a vote data message from " + msg.getSrc() + " with actual src: " + msg.getInfo().actualSrc);
            Random generator = new Random();
            taskManager.registerTask(new BroadcastTask(new BroadcastInfo(null, msg.getInfo().vote, Message.VOTE_ECHO_MSG, msg.getSrc(), msg.getInfo().seqNum)), generator.nextInt(SENDING_INTERVAL));

        }
        //} else {
        //  dump("Discarded an ballot message (cause: sent too late)");
        // }
    }

    private void receiveVoteEchoMsg(BROADCAST_MSG msg) throws NoSuchAlgorithmException {

        //if (!isLocalCountingOver) {
        synchronized (readyMap) {
            dump("Received a vote echo message from " + msg.getSrc() + " with actual src: " + msg.getInfo().actualSrc);
            Random generator = new Random();

            if (!msg.getSrc().isMalicious) {

                E_CryptoNodeID actualSrc = msg.getInfo().actualSrc;
                int seqNum = msg.getInfo().seqNum;
                ArrayList<MutableInt> countList = echoCountMap.get(actualSrc);
                ArrayList<Boolean> readyList = readyMap.get(actualSrc);
                boolean sentReady = false;

                if ((readyList == null) || readyList.isEmpty()) {
                    readyList = new ArrayList<Boolean>();
                    readyList.add(Boolean.FALSE);
                    readyList.add(Boolean.FALSE);
                } else {
                    sentReady = readyList.get(seqNum);
                }


                if ((countList == null) || countList.isEmpty()) {
                    countList = new ArrayList<MutableInt>();
                    countList.add(new MutableInt());
                    countList.add(new MutableInt());
                }

                countList.get(seqNum).inc();
                echoCountMap.put(actualSrc, countList);
                dump("echoCount (" + actualSrc + "): " + countList.get(seqNum).value);

                if (countList.get(seqNum).value > Math.floor(VOTERCOUNT * (1 + MALICIOUS_RATIO) / 2*threshold) && !sentReady) {
                    taskManager.registerTask(new BroadcastTask(new BroadcastInfo(null, msg.getInfo().vote, Message.VOTE_READY_MSG, actualSrc, msg.getInfo().seqNum)), generator.nextInt(SENDING_INTERVAL));
                    readyList.set(seqNum, Boolean.TRUE);
                    readyMap.put(actualSrc, readyList);
                }
            } else {
                dump("node " + msg.getSrc() + " is malicious");
            }
        }

    }

    private void receiveVoteReadyMsg(BROADCAST_MSG msg) throws NoSuchAlgorithmException {

        synchronized (readyMap) {
            dump("Received a vote ready message from " + msg.getSrc() + " with actual src: " + msg.getInfo().actualSrc);
            Random generator = new Random();

            if (!msg.getSrc().isMalicious) {

                E_CryptoNodeID actualSrc = msg.getInfo().actualSrc;
                int seqNum = msg.getInfo().seqNum;
                ArrayList<MutableInt> countList = readyCountMap.get(actualSrc);
                ArrayList<Boolean> readyList = readyMap.get(actualSrc);
                boolean sentReady = false;
                boolean delivered = false;
                ArrayList<Boolean> deliveredList = deliveredMap.get(actualSrc);


                if ((deliveredList == null) || deliveredList.isEmpty()) {
                    deliveredList = new ArrayList<Boolean>();
                    deliveredList.add(Boolean.FALSE);
                    deliveredList.add(Boolean.FALSE);
                } else {
                    delivered = deliveredList.get(seqNum);
                }
                if (delivered) {
                    //add statistics
                    return;
                }

                if ((readyList == null) || readyList.isEmpty()) {
                    readyList = new ArrayList<Boolean>();
                    readyList.add(Boolean.FALSE);
                    readyList.add(Boolean.FALSE);
                } else {
                    sentReady = readyList.get(seqNum);
                }


                if ((countList == null) || countList.isEmpty()) {
                    countList = new ArrayList<MutableInt>();
                    countList.add(new MutableInt());
                    countList.add(new MutableInt());
                }

                countList.get(seqNum).inc();
                readyCountMap.put(actualSrc, countList);
                dump("readyCount (" + actualSrc + "): " + countList.get(seqNum).value);

                if (countList.get(seqNum).value > Math.floor(VOTERCOUNT * MALICIOUS_RATIO*threshold) && !sentReady) {
                    taskManager.registerTask(new BroadcastTask(new BroadcastInfo(null, msg.getInfo().vote, Message.VOTE_READY_MSG, actualSrc, msg.getInfo().seqNum)), generator.nextInt(SENDING_INTERVAL));
                    readyList.set(seqNum, Boolean.TRUE);
                    readyMap.put(actualSrc, readyList);
                }

                if (countList.get(seqNum).value > Math.floor(2 * VOTERCOUNT * MALICIOUS_RATIO*threshold)) {
                    deliveredList.set(seqNum, true);
                    deliveredMap.put(actualSrc, deliveredList);
                    dump("delivered a ballot message " + msg.getInfo().vote + " from (" + actualSrc);
                    //testing broadcast
                   // if (nodeId.nodeOrder==0)
                     //   taskManager.registerTask(new ResultOutput());
                    
                    endInstant = System.nanoTime();
                    runningTime = endInstant - startInstant;
                    //     dump("Running Time: "+runningTime);
                    dump("\r"+"running time: "+runningTime+ "\r");
                    
                    taskManager.registerTask(new SelfDestructTask(),30*1000);
                    
                  //unremove these later  
//                    receiveBallot(msg);             
//                    
//                    if (nodeId.nodeOrder == msg.getInfo().actualSrc.nodeOrder + 1) {
//                        taskManager.registerTask(new VoteTask(), generator.nextInt(INTERBROADCAST_INTERVAL));
//                        dump("launched new voting session for node-" + nodeId.nodeOrder + 1);
//                    }
//
//                    readyToSend = true;
                }

            }
        }


    }

    private void receiveBroadcast(BROADCAST_MSG msg) throws NoSuchAlgorithmException {

        switch (msg.getInfo().type) {

            case Message.VOTE_DATA_MSG:

                receiveVoteDataMsg((BROADCAST_MSG) msg);
                break;

            case Message.VOTE_ECHO_MSG:
                receiveVoteEchoMsg((BROADCAST_MSG) msg);
                break;

            case Message.VOTE_READY_MSG:
                receiveVoteReadyMsg((BROADCAST_MSG) msg);
                break;

            case Message.SHARE_DATA_MSG:

                receiveShareDataMsg((BROADCAST_MSG) msg);
                break;

            case Message.SHARE_ECHO_MSG:
                receiveShareEchoMsg((BROADCAST_MSG) msg);
                break;

            case Message.SHARE_READY_MSG:
                receiveShareReadyMsg((BROADCAST_MSG) msg);
                break;
            default:
                dump("Discarded a message from " + msg.getSrc() + " of type " + msg.getHeader() + "(cause: unknown type)");

        }
    }

    private void receiveBallot(BROADCAST_MSG msg) throws NoSuchAlgorithmException {

        if (!isLocalCountingOver) {
            dump("Received a ballot (" + msg.getInfo().vote + ") from " + msg.getInfo().actualSrc);
            aggrLocalTally(msg.getInfo().vote);
            MRBallot++;
            SMRBallot += getObjectSize(msg);

        } else {
            dump("Discarded a ballot message from " + msg.getInfo().actualSrc + " (cause: sent too late)");
        }
    }

    private void receiveDecryptionShare(BROADCAST_MSG msg) throws NoSuchAlgorithmException {
        synchronized (LOCK) {

            if (!isDecryptionSharingOver) {

                dump("Received a decryption share (" + msg.getInfo().share + ") from " + msg.getInfo().actualSrc);

                resultSharesList.add(msg.getInfo().share);

                currentDecodingIndex++;
                dump("sharesize1: " + currentDecodingIndex);

                MRShare++;
                SMRShare += getObjectSize(msg);
                if (isFinalResultCalculated && currentDecodingIndex >= MINTALLIES) {
                    dump("CloseTallyDecryptionSharing");
                    //actually close the Tally Decryption Sharing session
                    isDecryptionSharingOver = true;
                    isShareSendingOver=true; //ensured by broadcast (no need)
                    taskManager.registerTask(new TallyDecryption());

                }

            } else {
                dump("Discarded a decryption share message (cause: sent too late)" + " from " + msg.getSrc());
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

    public static int getObjectSize(
            Serializable obj) {
        byte[] ba = null;

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);
            oos.flush();
            oos.close();
            ba = baos.toByteArray();
            baos.flush();
            baos.close();
        } catch (IOException ioe) {
            return 0;
        }
        return ba.length;
    }

    private class BroadcastTask implements Task {

        BroadcastInfo info = null;
        BROADCAST_MSG mes = null;
        int voteSent = 0;
        int shareSent = 0;

        private class BroadcastSenderTask extends TimerTask implements Runnable {

            BROADCAST_MSG senderMes;

            public BroadcastSenderTask(BROADCAST_MSG inMes) {
                receivedCount2++;
                //System.out.println("receivedCount2: " + receivedCount2);
                senderMes = inMes;
            }

            public void run() {
                try {
                    //send packet here
                    doSendUDP(senderMes);
                    if (senderMes.getInfo().type == Message.VOTE_DATA_MSG) {
                        synchronized (VOTESENDINGLOCK) {
                            voteSent++;
                            dump("voteSent: " + voteSent);
                            if (voteSent >= peerView.size()) {
                                isVoteTaskOver = true;
                                taskManager.registerTask(new AttemptSelfDestruct());
                            }
                        }
                    } else if (senderMes.getInfo().type == Message.SHARE_DATA_MSG) {
                        synchronized (SHARESENDINGLOCK) {
                            shareSent++;
                            dump("shareSent: " + shareSent);
                            if (shareSent >= peerView.size()) {
                                isShareSendingOver = true;
                                taskManager.registerTask(new AttemptSelfDestruct());
                            }
                        }
                    }


                    //                Thread.yield();
                } catch (UnknownHostException ex) {
                    Logger.getLogger(CryptoNode.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(CryptoNode.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        public BroadcastTask(BroadcastInfo inInfo) {
            receivedCount++;
           // System.out.println("receivedCount: " + receivedCount);
            this.info = inInfo;

        }

        public void execute() {

            synchronized (BROADCASTLOCK) {
                int x = 0;
                ScheduledThreadPoolExecutor schedThPoolExec = new ScheduledThreadPoolExecutor(1000);

                if (!(peerView.size() <= 1)) {

                    for (E_CryptoNodeID peerId : peerView) {
                        dump("count: " + x);
                        //     if (peerId.equals(nodeId)) {
                        //         continue;
                        //     }
                        dump("Send a '" + Emsg + "' ballot to " + peerId + " of type " + info.type + " with actual src: " + info.actualSrc);
                        try {
                            mes = new BROADCAST_MSG(nodeId, peerId, info);

                            schedThPoolExec.schedule(new BroadcastSenderTask(mes), generator.nextInt(MINI_SENDING_INTERVAL), TimeUnit.SECONDS);
                            // doSendUDP(mes);
                            //Timer timer = new Timer();
                            //   timer.schedule(new BroadcastSenderTask(mes), generator.nextInt(SENDING_INTERVAL*1000));
                            Thread.yield();


                            //  doSendUDP(mes);
                            //  Thread.sleep(10);
                        } catch (Exception e) {
                            dump("TCP: cannot vote");
                        }
                        x++;
                    }
//                    MSVote += peerView.size() - 1;
//                    SMSVote += getObjectSize(mes) * (peerView.size() - 1);

                } else {
                    dump("Cannot vote: no peer view");

                }
            }
        }
    }

    private class VoteTask implements Task {

//        private class VoteSenderTask implements Runnable {
//
//            CRYPTO_BALLOT_MSG mes = null;
//
//            public VoteSenderTask(CRYPTO_BALLOT_MSG mes) {
//                this.mes = mes;
//            }
//
//            public void run() {
//                try {
//                    //send packet here
//                    doSendUDP(mes);
//                    Thread.yield();
//                } catch (UnknownHostException ex) {
//                    Logger.getLogger(CryptoNode.class.getName()).log(Level.SEVERE, null, ex);
//                } catch (IOException ex) {
//                    Logger.getLogger(CryptoNode.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
//        }
        public void execute() {
            //   synchronized (LOCK) {

            startInstant = System.nanoTime();

            taskManager.registerTask(new PreemptCloseLocalCountingTask(), CLOSE_COUNTING_DELAY);
            Random generator = new Random();
            taskManager.registerTask(new BroadcastTask(new BroadcastInfo(null, Emsg, Message.VOTE_DATA_MSG, nodeId, 0)), generator.nextInt(SENDING_INTERVAL));
            sequenceNumber++;
            readyToSend = false;

            dump("sequence number: " + sequenceNumber);
//            ScheduledThreadPoolExecutor schedThPoolExec = new ScheduledThreadPoolExecutor(1000);
//
//            if (!(peerView.size() <= 1)) {
//                Random generator = new Random();
//                for (E_CryptoNodeID peerId : peerView) {
//                    if (peerId.equals(nodeId)) {
//                        continue;
//                    }
//                    dump("Send a '" + Emsg + "' ballot to " + peerId);
//                    try {
//
//                        mes = new CRYPTO_BALLOT_MSG(nodeId, peerId, Emsg);
//                        //             schedThPoolExec.schedule(new VoteSenderTask(mes), generator.nextInt(20), TimeUnit.SECONDS);
//
//                        taskManager.registerTask(new BroadcastTask());
//                        Thread.yield();
//
//                        //  doSendUDP(mes);
//                        //  Thread.sleep(10);
//                    } catch (Exception e) {
//                        dump("TCP: cannot vote");
//                    }
//                }
//                MSVote += peerView.size() - 1;
//                SMSVote += getObjectSize(mes) * (peerView.size() - 1);
//
//            } else {
//                dump("Cannot vote: no peer view");
//
//            }

            //   taskManager.registerTask(new PreemptPartialTallyingTask(), CLOSE_PARTIAL_TALLYING_DELAY);
            //   aggrLocalTally(Emsg);
            taskManager.registerTask(new AttemptSelfDestruct());
            //     taskManager.registerTask(new CloseVoteTask());



            //      }
        }
    }

    public void aggrLocalTally(BigInteger ballot) {

        synchronized (LOCK) {
            long startT = System.nanoTime();
            localTally = encryptor.add(localTally, ballot);
            TallyAggTime += System.nanoTime() - startT;
            numBallots++;


            dump("ballots " + numBallots + " " + peerView.size());

            if (numBallots == (int) Math.floor(VOTERCOUNT * threshold)) {
                computedLocalTally = true;
//            if (IAmThreshold) {
//                partialTally = localTally;
//                taskManager.registerTask(new GlobalCountingTask());
//            } else if (computedPartialTally) {
//                partialTally = encryptor.add(localTally, partialTally);
//                taskManager.registerTask(new GlobalCountingTask());
//            }


                isLocalCountingOver = true;

                finalEncryptedResult = localTally; 
                dump("final encrypted:" + finalEncryptedResult.toString());

                isFinalResultCalculated=true;
                if (nodeId.nodeOrder == 0) {
                    taskManager.registerTask(new TallyDecryptionSharing());
                } 

                //else do nothing
            }

        }

    }

    private class AttemptSelfDestruct implements Task {

        public void execute() {
            dump("isVoteTaskOver:" + isVoteTaskOver);
            dump("isLocalCountingOver:" + isLocalCountingOver);
            dump("isTallyDecryptionOver:" + isTallyDecryptionOver);
            dump("isShareSendingOver:" + isShareSendingOver);
            synchronized (LOCK) {
                if (isVoteTaskOver && isLocalCountingOver && isTallyDecryptionOver && isShareSendingOver) {

                    /*		       try {
                    doSendUDP(new DEAD_MSG(nodeId, bootstrap));
                    dump("sent a dead message");
                    }catch (Exception e) {
                    dump("TCP: cannot send dead message to bootstrap");
                    } 
                     */

                    endInstant = System.nanoTime();
                    runningTime = endInstant - startInstant + viewDuration;
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
                    //   specialDump("PreemptCloseLocalCountingTask");

                    computedLocalTally = true;
                    isLocalCountingOver = true;
                    finalEncryptedResult = localTally;
                    taskManager.registerTask(new TallyDecryptionSharing());

                }

            }
        }
    }

    private class TallyDecryptionSharing implements Task {

        public void execute() {
            // synchronized (LOCK) {
          //  if (!isFinalResultCalculated) {
                //    taskManager.registerTask(new PreemptCloseTallyDecryptionSharing(), CLOSE_DecryptionSharing_DELAY);

                //      specialDump("TallyDecryptionSharing");
                dump("TallyDecryptionSharing");

             
                long startT = System.nanoTime();
                nodeResultShare = secKey.decrypt(finalEncryptedResult);
                ShareCompTime += System.nanoTime() - startT;
                // synchronized (LOCK) {
//                synchronized (LOCK) {
//                    resultSharesList.add(nodeResultShare);
//                }


                //}
                isFinalResultCalculated = true;
                Random generator = new Random();

             //   taskManager.registerTask(new TallySending(), SENDING_INTERVAL);
 
                taskManager.registerTask(new BroadcastTask(new BroadcastInfo(nodeResultShare, null, Message.SHARE_DATA_MSG, nodeId, 1)), generator.nextInt(SENDING_INTERVAL));
                sequenceNumber++;
                readyToSend = false;

                dump("sequence number: " + sequenceNumber);
//                             currentDecodingIndex++;
//                    dump("sharesize2: " + currentDecodingIndex);

//                    CRYPTO_DECRYPTION_SHARE_MSG mes = null;
//
//                    if (!(peerView.size() <= 1)) {
//                        for (E_CryptoNodeID peerId : peerView) {
//                            if (peerId.equals(nodeId)) {
//                                continue;
//                            }
//                            dump("Send decryption share (" + nodeResultShare + ") to " + peerId);
//                            try {
//                                mes = new CRYPTO_DECRYPTION_SHARE_MSG(nodeId, peerId, nodeResultShare);
//                                doSendUDP(mes);
//                                // Thread.sleep(10);
//                            } catch (Exception e) {
//                                dump("TCP: cannot send decryption share");
//                            }
//                        }
//                        //  synchronized (LOCK) {
//
//                        MSShare += peerView.size() - 1;
//                        SMSShare += getObjectSize(mes) * (peerView.size() - 1);
//                        //   }
//                    } else {
//                        receiveSTOP(new STOP_MSG(nodeId, nodeId, "cannot share result share: no peer view"));
//                    }
//                    //}
//                    //   synchronized (LOCK) {
//
//                    if (currentDecodingIndex >= MINTALLIES) {
//                        isDecryptionSharingOver = true;
//                        dump("CloseTallyDecryptionSharing");
//                        //actually close the Tally Decryption Sharing session
//                        taskManager.registerTask(new TallyDecryption());
//                    }
//                    //  }
//                    taskManager.registerTask(new AttemptSelfDestruct());


          //  }
        }
        //  }
    }
//
//    private class TallySending implements Task {
//
//      //  private int sentShares = 0;
//
////        private class ShareSenderTask implements Runnable {
////
////            CRYPTO_DECRYPTION_SHARE_MSG mes = null;
////
////            public ShareSenderTask(CRYPTO_DECRYPTION_SHARE_MSG mes) {
////                this.mes = mes;
////            }
////
////            public void run() {
////                try {
////                    //send packet here
////
////
////                    doSendUDP(mes);
////                    dump("Send decryption share (" + nodeResultShare + ") to " + mes.getDest());
////                    synchronized (SHARESENDINGLOCK) {
////                        sentShares++;
////                        dump("sentShares: " + sentShares);
////                        if (sentShares >= peerView.size() - 1) {
////                            isShareSendingOver = true;
////                            taskManager.registerTask(new AttemptSelfDestruct());
////                        }
////                    }
////                } catch (UnknownHostException ex) {
////                    Logger.getLogger(CryptoNode.class.getName()).log(Level.SEVERE, null, ex);
////                } catch (IOException ex) {
////                    Logger.getLogger(CryptoNode.class.getName()).log(Level.SEVERE, null, ex);
////                }
////            }
////        }
//
//        public void execute() {
//            if (!startedShareSending) {
//                startedShareSending = true;
//                Random generator = new Random();
//                taskManager.registerTask(new BroadcastTask(new BroadcastInfo(nodeResultShare, null, Message.SHARE_DATA_MSG, nodeId, 1)), generator.nextInt(SENDING_INTERVAL));
//                sequenceNumber++;
//                readyToSend = false;
//
//                dump("sequence number: " + sequenceNumber);
////                CRYPTO_DECRYPTION_SHARE_MSG mes = null;
////
////                if (!(peerView.size() <= 1)) {
////                    //     ScheduledThreadPoolExecutor schedThPoolExec = new ScheduledThreadPoolExecutor(1);
////
////
////                    for (E_CryptoNodeID peerId : peerView) {
//////                        if (peerId.equals(nodeId)) {
//////                            continue;
//////                        }
////                        try {
////                        
////
////                            //    mes = new CRYPTO_DECRYPTION_SHARE_MSG(nodeId, peerId, nodeResultShare);
////                            //         schedThPoolExec.schedule(new ShareSenderTask(mes), generator.nextInt(SENDING_INTERVAL), TimeUnit.SECONDS);
////                            //         Thread.yield();
////                            //   doSendUDP(mes);
////                            // Thread.sleep(10);
////                        } catch (Exception e) {
////                            dump("TCP: cannot send decryption share");
////                        }
////                    }
////                    //  synchronized (LOCK) {
////
////                    //   }
////                } else {
////                    receiveSTOP(new STOP_MSG(nodeId, nodeId, "cannot share result share: no peer view"));
////                }
//                //}
////                synchronized (LOCK) {
////                    currentDecodingIndex++;
////
////
////                    dump("sharesize2: " + currentDecodingIndex);
//////                    MSShare += peerView.size() - 1;
//////                    SMSShare += getObjectSize(mes) * (peerView.size() - 1);
////
////                    if (currentDecodingIndex >= MINTALLIES) {
////                        isDecryptionSharingOver = true;
////                        dump("CloseTallyDecryptionSharing");
////                        //actually close the Tally Decryption Sharing session
////                        taskManager.registerTask(new TallyDecryption());
////                    }
////                    //  }
////                    taskManager.registerTask(new AttemptSelfDestruct());
////
////                }
//            }
//        }
//    }

    private class TallyDecryption implements Task {

        public void execute() {
            synchronized (LOCK) {
                if (!isTallyDecryptionOver) {
                    isTallyDecryptionOver = true;
                    dump("TallyDecryption");
                    isDecryptionSharingOver = true;
                    PartialDecryption[] decArray = new PartialDecryption[resultSharesList.size()];
                    //        System.out.println("shares: ");
                    for (int i = 0; i < resultSharesList.size(); i++) {
                        decArray[i] = resultSharesList.get(i);
                        //     System.out.println(" " + decArray[i].getDecryptedValue());
                    }
                    //System.out.println("decaraysize: " + resultSharesList.size());

                    long startT = System.nanoTime();
                    finalResult = secKey.combineShares(decArray);
                    VoteDecTime += System.nanoTime() - startT;

                    computedFinalResult = true;
                    dump("Determined final result:" + finalResult);



                    //         taskManager.registerTask(new ResultDiffusionTask());
                    taskManager.registerTask(new AttemptSelfDestruct());


                }
            }
        }
    }

    private class ResultOutput implements Task {

        public void execute() {
            synchronized (LOCK) {
                //  if (nodeId.name.equals("node-1") && nodeId.port == basicPort) {
                paillierp.testingPaillier.TestingRest.getResult(finalResult, VOTECOUNT, votes);
                //    }




                specialDump("\r" + MSView + " " + Math.pow(MSVote, 2) + " " + MSPartial + " " + MSShare + " " + MSResult
                        + " " + MRKeys + " " + MRView + " " + Math.pow(MRBallot, 2) + " " + MRPartial
                        + " " + MRShare + " " + MRResult + " " + SMSView + " " + Math.pow(SMSVote, 2) + " " + SMSPartial + " " + SMSShare
                        + " " + SMSResult + " " + SMRKeys + " " + SMRView + " "
                        + Math.pow(SMRBallot, 2) + " " + SMRPartial + " " + SMRShare + " " + SMRResult + " " + TallyAggTime
                        + " " + VoteEncTime + " " + ShareCompTime + " " + VoteDecTime + " " + runningTime + "\r");
                isResultOutputed = true;
                // taskManager.registerTask(new AttemptSelfDestruct());
                taskManager.registerTask(new SelfDestructTask());
            }


        }
    }
//    private class ResultDiffusionTask implements Task {
//
//        public void execute() {
//            // broadcast
//            synchronized (LOCK) {
//                if ((!isResultDiffusionOver) && !(numClusters == nodeId.groupId + 1)) {
//                    //      specialDump("ResultDiffusionTask");
//                    dump("ResultDiffusionTask at begin");
//
//                    CRYPTO_FINAL_RESULT_MSG mes = null;
//                    for (E_CryptoNodeID proxyId : proxyView) {
//
////                        if (isMalicious) {
////                            finalResult = votes[0].multiply(BigInteger.valueOf(VOTERCOUNT));
////                        }
//                        dump("Send final result (" + finalResult + ") to " + proxyId);
//                        try {
//                            mes = new CRYPTO_FINAL_RESULT_MSG(nodeId, proxyId, finalResult);
//                            doSendUDP(mes);
//                        } catch (Exception e) {
//                            dump("TCP: cannot broadcast final result");
//                        }
//
//                    }
//                    isResultDiffusionOver = true;
//                    MSResult += proxyView.size();
//                    SMSResult += getObjectSize(mes) * proxyView.size();
//
//                } else if (numClusters == nodeId.groupId + 1) {
//                    isResultDiffusionOver = true;
//                }
//
//                dump("ResultDiffusionTask at end");
//                taskManager.registerTask(new AttemptSelfDestruct());
//
//            }
//
//        }
//    }
//    private void receiveFinalResult(CRYPTO_FINAL_RESULT_MSG msg) {
//
//
//        synchronized (LOCK) {
//            if (!computedFinalResult) {
//
//                //             synchronized (localTallySets[groupId]) {
//                //    synchronized (finalResults) {
//
//                dump("Received a final result (" + msg.getResult() + ") from " + msg.getSrc());
//                numFinalResults++;
//
//                finalResults.add(msg.getResult());
//                MRResult++;
//                SMRResult += getObjectSize(msg);
//                dump("finals:" + numFinalResults + " " + clientView.size());
//                if (numFinalResults == (int) (Math.floor(clientView.size() * threshold))) {
//                    finalResult = mostPresent(finalResults);
//                    computedFinalResult = true;
//
//                    if (IAmThreshold) {
//                        taskManager.registerTask(new AttemptSelfDestruct());
//                    } else {
//
//                        taskManager.registerTask(new ResultDiffusionTask());
//                    }
//                }
//
//            }
//
//            //         }
//        }
//    }
//    private void receiveView(CRYPTO_VIEW_MSG msg) {
//
//        if (!receivedAllViews) {
//            synchronized (LOCK) {
//                if (isFirstView) {
//                    startViewTime = System.nanoTime();
//                    isFirstView = false;
//                }
//
//                dump("Received a view message from " + msg.getSrc());
//                numReceivedViews++;
//                if (numReceivedViews == nodesPerCluster) {
//                    receivedAllViews = true;
//                    viewDuration = System.nanoTime() - startViewTime;
//                }
//
//                MRView++;
//                SMRView += getObjectSize(msg);
//
//            }
//        }
//    }
    //    private class PreemptResultDiffusionTask implements Task {
//
//        public void execute() {
//            synchronized (LOCK) {
//
//                if (!computedFinalResult) {//actually close the local counting session
//                    //   specialDump("PreemptResultDiffusionTask");
//                    finalResult = mostPresent(finalResults);
//                    computedFinalResult = true;
//
//                    if (IAmThreshold) {
//                        taskManager.registerTask(new AttemptSelfDestruct());
//                    } else {
//
//                        taskManager.registerTask(new ResultDiffusionTask());
//                    }
//                }
//
//            }
//        }
//    }
    //    private class GlobalCountingTask implements Task {
//
//        //   private int localTallyGroupId;
//        public void execute() {
//
//            // broadcast
//            dump("GlobalCountingTask at begin");
//            if (!IsPartialTallyingOver) {
//                //  specialDump("GlobalCountingTask");
//                synchronized (LOCK) {
//                    CRYPTO_PARTIAL_TALLY_MSG mes = null;
//                    taskManager.registerTask(new PreemptResultDiffusionTask(), CLOSE_ResultDiffusion_DELAY);
//                    dump("GlobalCountingTask");
//
////                    if (isMalicious) {
////                        partialTally = encryptor.encrypt(votes[0].multiply(BigInteger.valueOf(nodeId.groupId + 1)));
////                    }
//
//                    for (E_CryptoNodeID proxyId : proxyView) {
//                        dump("Send partial tally (" + partialTally + ") to " + proxyId);
//                        try {
//                            mes = new CRYPTO_PARTIAL_TALLY_MSG(nodeId, proxyId, partialTally);
//                            doSendUDP(mes);
//                        } catch (Exception e) {
//                            dump("TCP: cannot broadcast local tally");
//                        }
//
//                    }
//
//                    IsPartialTallyingOver = true;
//                    MSPartial += proxyView.size();
//                    SMSPartial += getObjectSize(mes);
//                    // taskManager.registerTask(new CloseGlobalCountingTask());
//
//                }
//                dump("GlobalCountingTask at end");
//            }
//        }
//    }
//    private void receivePartialTally(CRYPTO_PARTIAL_TALLY_MSG msg) {
//
//        if (!computedPartialTally) {
//            synchronized (LOCK) {
//
//                dump("Received a partial tally (" + msg.getTally() + ") from " + msg.getSrc());
//                numPartialTallies++;
//
//                partialTallies.add(msg.getTally());
//                dump("partial:" + numPartialTallies + " " + clientView.size());
//
//                MRPartial++;
//                SMRPartial += getObjectSize(msg);
//
//                if (numPartialTallies == (int) (Math.floor(clientView.size() * threshold))) {
//                    partialTally = mostPresent(partialTallies);
//                    computedPartialTally = true;
//
//                    if (IAmThreshold) {
//
//                        finalEncryptedResult = partialTally;
//                        taskManager.registerTask(new TallyDecryptionSharing());
//                    } else if (computedLocalTally) {
//                        long startT = System.nanoTime();
//                        partialTally = encryptor.add(localTally, partialTally);
//                        TallyAggTime += System.nanoTime() - startT;
//
//                        taskManager.registerTask(new GlobalCountingTask());
//                    }
//                }
//            }
//        }
//    }
    //    private class ViewDiffusion implements Task {
//
//        public void execute() {
//            synchronized (LOCK) {
//                if (!isViewDiffusionOver) {
//                    //       taskManager.registerTask(new PreemptCloseLocalCountingTask(), CLOSE_COUNTING_DELAY);
//                    //   if (!(peerView.size()<=1)) {
//                    //  specialDump("ViewDiffusion");
//                    Set<E_CryptoNodeID> tempSet;
//                    CRYPTO_VIEW_MSG mes;
//                    for (int i = 0; i < numClusters; i++) {
//                        tempSet = nodeToCluster.get(i);
//                        for (E_CryptoNodeID peerId : tempSet) {
//                            if (peerId.equals(nodeId)) {
//                                continue;
//                            }
//                            dump("Send a viewto " + peerId);
//
//                            try {
//                                mes = new CRYPTO_VIEW_MSG(nodeId, peerId, nodeToCluster.get((peerId.groupId)), nodeToCluster.get((peerId.groupId + 1) % numClusters), nodeToCluster.get((peerId.groupId + numClusters - 1) % numClusters));
//                                doSendUDP(mes);
//                                MSView++;
//                                SMSView += getObjectSize(mes);
//                            } catch (Exception e) {
//                                dump("TCP: cannot vote");
//                            }
//                        }
//                    }
//
//
//                    isViewDiffusionOver = true;
//                    //  taskManager.registerTask(new PreemptPartialTallyingTask(), CLOSE_PARTIAL_TALLYING_DELAY);
//                    //     aggrLocalTally(Emsg);
//                    taskManager.registerTask(new AttemptSelfDestruct());
//                    //     taskManager.registerTask(new CloseVoteTask());
//
//
//                }
//            }
//        }
//    }
    //    private class PreemptPartialTallyingTask implements Task {
//
//        public void execute() {
//            synchronized (LOCK) {
//                if (!IsPartialTallyingOver) {//actually close the local counting session
//
//                    partialTally = mostPresent(partialTallies);
//                    computedPartialTally = true;
//
//                    if (IAmThreshold) {
//                        finalEncryptedResult = partialTally;
//                        taskManager.registerTask(new TallyDecryptionSharing());
//                    } else if (computedLocalTally) {
//                        partialTally = encryptor.add(localTally, partialTally);
//                        taskManager.registerTask(new GlobalCountingTask());
//                    }
//                }
//
//            }
//        }
//    }
//
//    private class PreemptCloseTallyDecryptionSharing implements Task {
//
//        public void execute() {
//            synchronized (LOCK) {
//                if (!isDecryptionSharingOver) {
//                    //   specialDump("PreemptCloseTallyDecryptionSharing");
//
//                    //actually close the Tally Decryption Sharing session
//                    isDecryptionSharingOver = true;
//                    taskManager.registerTask(new TallyDecryption());
//                }
//            }
//        }
//    }
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
//                            doSendUDP(new POSITION_ASSIGN_MSG(nodeId, randomNodeID, nodeToCluster));
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
//                            doSendUDP(new CLUSTER_ASSIGN_MSG(nodeId, peerId, myIDAssignment));
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
//    }public int MRKeys=1;
//      private class getViews implements Task {
//
//        public void execute() {
//            synchronized (LOCK) {
//
//
//                E_CryptoNodeID tempID;
//                //     Map<E_CryptoNodeID, Integer> IDAssignment = new HashMap<E_CryptoNodeID, Integer>();
//                //  List<E_CryptoNodeID> sortedIDs;
//
//                int mycount = 1;
//                threshOrder = (0.5 - epsilon) * VOTERCOUNT;
//                boolean isMal;
//                for (int i = 1; i <= VOTERCOUNT / nodesPerMachine; i++) {
//                    for (int j = 0; j < nodesPerMachine; j++) {
//                        isMal = (mycount < threshOrder);
//                        tempID = new E_CryptoNodeID("node-" + i, basicPort + j, isMal);
//
//                        if (tempID.equals(nodeId)) {
//                            nodeId.isMalicious = isMal;
//                            //     System.out.println("I am " + isMal);
//                        }
//                        peerView.add(tempID);
//                        // IDAssignment.put(tempID, tempID.getOrder());
//                        mycount++;
//                    }
//                }
//
////                //      System.out.println(nodeId.toString() + ":");
//////                for (int i=0;i<sortedIDs.size();i++)
//////                    System.out.println(sortedIDs.get(i).toString()+" ,");
//////                
////                // System.out.println(sortedIDs.size());
////                nodeToCluster = new ClusterChoice(sortedIDs, nodeId);
////                nodeId.groupId = nodeToCluster.myGroupID;
//////                if (nodeId.groupId == -1) {
//////                    System.out.println(nodeId.toString());
//////                }
////                if (nodeId.groupId == 0) {
////                    IAmThreshold = true;
////
////                    secKey = (PaillierThreshold) CryptoGossipLauncher.getObject(secKeyFile + nodeToCluster.keyNum);
////                    //  System.out.println("keynum:" + nodeToCluster.keyNum);
////                }
////
////                //        System.out.println("next: "+(nodeId.groupId + 1) % numClusters);
////                proxyView = nodeToCluster.get((nodeId.groupId + 1) % numClusters);
//////                   for (int i=0;i<proxyView.size();i++)
//////                    System.out.println(proxyView.toArray()[i].toString()+" ,");
//////             
////                peerView = nodeToCluster.get((nodeId.groupId));
////                //    peerView.remove(nodeId);
////                clientView = nodeToCluster.get((nodeId.groupId + numClusters - 1) % numClusters);
////
////                if (IAmThreshold) {
////                    taskManager.registerTask(new ViewDiffusion(), VIEW_DIFF_DELAY);
////
////
////                }
//                taskManager.registerTask(new VoteTask(), VOTE_DELAY);
//            }
//        }
//    }
//
}
