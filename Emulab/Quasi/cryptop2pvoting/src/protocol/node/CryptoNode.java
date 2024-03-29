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
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;

import java.util.Set;
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
    private static int VIEW_DIFF_DELAY = 35 * 1000;// Delay before voting: 50 seconds
    private static int VOTE_DELAY = VIEW_DIFF_DELAY + 250 * 1000;
    //   private static int CLOSE_VOTE_DELAY = 490 * 1000; 				// Duration of the local voting phase: 1 minute
    private static int CLOSE_COUNTING_DELAY = 3200 * 1000;		// Duration of the local counting phase: 1 minute
    private static int CLOSE_PARTIAL_TALLYING_DELAY = CLOSE_COUNTING_DELAY + 3200 * 1000;		// Duration of the local counting phase: 1 minute
    private static int CLOSE_DecryptionSharing_DELAY = 3200 * 1000;
    private static int CLOSE_ResultDiffusion_DELAY = 3200 * 1000;
//    private static int CLOSE_TallyDecryption_DELAY = CLOSE_DecryptionSharing_DELAY + 20 * 1000;
    private static int SELF_DESTRUCT_DELAY = 3200 * 1000;
    // private static int COUNTING_PERIOD = 20 * 1000;		
    // Duration of epidemic dissemination: 20 seconds
    public static int kvalue;
    public static int MINTALLIES;
    public static int nodesPerMachine;
    public static ClusterChoice nodeToCluster = null;
    public static int chosenCluster;
    public static int currentNeighbour = 0;
    public static int firstRound = 0;
    public static int currentRound = 1;
    public static int currentCounter = 1;
    public static int numClusters;
    public static double LOSS = 0.5;
    E_CryptoNodeID bid;
    public static Map<E_CryptoNodeID, Integer> IDAssignment = new HashMap<E_CryptoNodeID, Integer>();
    public static Map<E_CryptoNodeID, Integer> finalIDAssignment = new HashMap<E_CryptoNodeID, Integer>();
    Set<E_CryptoNodeID> smallestCluster;
//    public int numRecvClusterAssign = 0;
//    public int numRecvFinalClusterAssign = 0;
//    public ClusterAssignment clusterAssign;
    public boolean IAmThreshold = false;
    public boolean IAmSource = false;
    public int numPartialTallies = 0;
    public boolean computedLocalTally = false;
    public boolean computedPartialTally = false;
    protected BigInteger partialTally = BigInteger.ONE;
    protected List<BigInteger> partialTallies = new LinkedList<BigInteger>();
    protected List<E_CryptoNodeID> sortedIDs;
    public int numBallots = 0;
    public boolean isResultDiffusionOver = false;
    public boolean isFirstDiffusion = true;
    public boolean isFirstReception = true;
    public int numFinalResults = 0;
    protected List<BigInteger> finalResults = new LinkedList<BigInteger>();
    public boolean computedFinalResult = false;
    //  public static int stepsConstant;
    public static int basicPort;
    public static int nodesPerCluster;
    // public static boolean isMalicious;
    public static int order;
    public static int numReceivedViews = 0;
    public static double threshold = 0.95;
    public static double thresholdBallot = 1;
    public static boolean receivedAllRumors = false;
    public static boolean isViewDiffusionOver = false;
    public static boolean isFirstView = true;
    public static long startViewTime = 0;
    public static long viewDuration = 0;
    public static int voteTimes = 0;
    public static int ballotTimes = 0;
    // Fields
    // protected final E_CryptoNodeID bootstrap;
    //Keys
//    PublicKey pub;
//    SecretKey sec;
    // Vote value
    protected boolean hasToken = true;
    protected boolean isLocalVoteOver = false;
    protected boolean isLocalCountingOver = false;
    protected boolean IsPartialTallyingOver = false;
    protected boolean isShareSendingOver = false;
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
    public int MSRumors = 0;
    public int MRRumors = 0;
    public int MDuplicates = 0;
    public int MSVote = 0;
    public int MRBallot = 0;
    public int MSPartial = 0;
    public int MRKeys = 1;
    public int MRPartial = 0;
    public int MSShare = 0;
    public int MRShare = 0;
    public int MSResult = 0;
    public int MRResult = 0;
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
    public long VoteDecTime = 0;
    public long ShareCompTime = 0;
    public boolean notInc=true;
    //protected boolean vote;
//    protected Tally tally;
//    protected Vote vote;
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
    public long startTime = 0;
    public boolean stopped = false;
    public double threshOrder;
    public static int period;

    // **************************************************************************
    // Constructors
    // **************************************************************************
    public CryptoNode(E_CryptoNodeID nodeId, TaskManager taskManager, NetworkSend networkSend, Stopper stopper, E_CryptoNodeID bid) throws Exception {

        super(nodeId, networkSend);
        this.bid = bid;
        //MALICIOUS_RATIO = 0.5 - epsilon;
        //    this.isMalicious = (Math.random() < MALICIOUS_RATIO);

        //this.vote = (Math.random() < VOTE_RATIO && !isMalicious);


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
//        //    } else {
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
//        this.localTally = BigInteger.ONE;
//        finalEncryptedResult = BigInteger.ONE;
//        finalResult = BigInteger.ONE;
//        numIndTallies = 0;
//        //resultSharesList = new DecodingShare[MINTALLIES];
//
//        currentDecodingIndex = 0;
        //this.shareOrder=shareOrder;

        //         clientsReceived=0;
        //
//        for (i = 0; i < E_CryptoNodeID.NB_GROUPS; i++) {
//            this.localTallySets[i] = new HashMap<E_CryptoNodeID, BigInteger>();
//            this.localTallies[i] = BigInteger.ONE;
//
//        }

//        numClusters = (int) (Math.ceil(VOTERCOUNT / (kvalue * Math.log(VOTERCOUNT))));
//        nodesPerCluster = (int) (Math.ceil(VOTERCOUNT * 1.0 / numClusters));
//        while (nodesPerCluster * numClusters >= VOTERCOUNT) {
//            numClusters--;
//        }
//        numClusters++;
//        double test = Math.floor(VOTERCOUNT / numClusters);
//        if (test < (1.0 * VOTERCOUNT / numClusters)) {
//            numClusters++;
//        }
//        nodesPerCluster = (int) test;

//        MINTALLIES = nodesPerCluster / 2 + 1;
//        //  System.out.println("min:" + MINTALLIES)
        VIEW_DIFF_DELAY = 15 + 1000 * VOTERCOUNT / 3000 * 1000;
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
        dump("Node " + nodeId.getName() + " is born: ");
        dump("Central Node " + bid.getName() + ": " + bid.getPort());
        //  dump("Parameters: Vote Ratio=" + VOTE_RATIO);
        // dump("Parameters: DT=" + DECISION_THRESHOLD + " DD=" + DECISION_DELAY);

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
//                    receiveBallot((CRYPTO_BALLOT_MSG) msg);
//                    break;
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

                case Message.RUMOR_MSG:
                    receiveRumor((RUMOR_MSG) msg);
                    break;
                case Message.COUNTER_MSG:
                    receiveCounter((COUNTER_MSG) msg);
                    break;


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

    public static List sortByValue(final Map m) {
        List keys = new LinkedList();
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


                E_CryptoNodeID tempID;

                Map<E_CryptoNodeID, Integer> IDAssignment = new HashMap<E_CryptoNodeID, Integer>();

                int mycount = 1;
                for (int i = 1; i <= VOTERCOUNT / nodesPerMachine; i++) {
                    for (int j = 0; j < nodesPerMachine; j++) {
                        tempID = new E_CryptoNodeID("node-" + i, basicPort + j, false);
                        if (tempID.equals(nodeId)) {
                            continue;
                        }
                        IDAssignment.put(tempID, tempID.getOrder());
                        mycount++;
                    }
                }
                sortedIDs = sortByValue(IDAssignment);

                IAmSource = ((nodeId.port == basicPort) && nodeId.name.equals("node-1"));


                if (sortedIDs.size() == VOTERCOUNT - 1) {
                    dump("I was removed");
                }

                if (IAmSource) {
                    startTime = System.currentTimeMillis();
                    dump("I am source");
                    Random generator = new Random();
                    currentRound = 0;
                    currentNeighbour = generator.nextInt(VOTERCOUNT - 1);
                    isFirstDiffusion = false;
                    isFirstReception=false;
                    taskManager.registerTask(new RumorDiffusion(), VIEW_DIFF_DELAY);
                }
            }
        }
    }
//
//    public static int getObjectSize(
//            Serializable obj) {
//        byte[] ba = null;
//
//        try {
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            ObjectOutputStream oos = new ObjectOutputStream(baos);
//            oos.writeObject(obj);
//            oos.close();
//            ba = baos.toByteArray();
//            baos.close();
//        } catch (IOException ioe) {
//            return 0;
//        }
//        return ba.length;
//    }

    private class RumorDiffusion implements Task {

        public void execute() {
            synchronized (LOCK) {
                currentRound++;
//
//                 PING_MSG mes0= new PING_MSG(nodeId, bid);;
//                 try {
//                    networkSend.sendTCP(mes0);
//                } catch (SocketTimeoutException e) {
//                    dump("TCP: " + nodeId + ":" + mes0.getDest() + " might be dead!");
//                } catch (ConnectException e) {
//                    dump("TCP: " + nodeId + ":" + mes0.getDest() + " is dead!");
//                    taskManager.registerTask(new ResultOutput());
//       //             currentCounter--;
//                    return;
//
//                } catch (UnknownHostException ex) {
//                    Logger.getLogger(CryptoNode.class.getName()).log(Level.SEVERE, null, ex);
//                } catch (IOException ex) {
//                    Logger.getLogger(CryptoNode.class.getName()).log(Level.SEVERE, null, ex);
//                }
//                 
                 
                E_CryptoNodeID peerId = null;
                RUMOR_MSG mes = null;


                peerId = sortedIDs.get(currentNeighbour);
                //update current neighbor for next time
                currentNeighbour = (currentNeighbour + 1) % (VOTERCOUNT - 1);
               //     Random generator = new Random();
                 //   currentNeighbour = generator.nextInt(VOTERCOUNT - 1);
                
                dump("Send a rumor to " + peerId);

                mes = new RUMOR_MSG(nodeId, peerId, currentRound);
                try {
                    networkSend.sendTCP(mes);
                } catch (SocketTimeoutException e) {
                    specialDump("TCP: " + nodeId + ":" + mes.getDest() + " might be dead!");
                    
                } catch (ConnectException e) {
                    dump("TCP: " + nodeId + ":" + mes.getDest() + " is dead!");
                    taskManager.registerTask(new ResultOutput());
                    return;

                } catch (UnknownHostException ex) {
                    Logger.getLogger(CryptoNode.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(CryptoNode.class.getName()).log(Level.SEVERE, null, ex);
                }
                MSRumors++;
                //       SMSView += getObjectSize(mes);
//                taskManager.registerTask(new RumorDiffusion(), (long) exp(1) * period);
                            taskManager.registerTask(new RumorDiffusion(), (long) period);

            }


        }
    }

    private class ReadCounter implements Task {

        public void execute() {
       
            synchronized (LOCK) {
                
                

                
                READ_CTR_MSG mes = null;
                dump("Read Counter value");

                mes = new READ_CTR_MSG(nodeId, bid);

                try {
                    networkSend.sendTCP(mes);
                } catch (SocketTimeoutException e) {
                    specialDump("TCP: " + nodeId + ":" + mes.getDest() + " might be dead!");
                    taskManager.registerTask(new ReadCounter(), (long) exp(1) * period);
                    return; 
                } catch (ConnectException e) {
                    dump("TCP: " + nodeId + ":" + mes.getDest() + " is dead!");
                    taskManager.registerTask(new ResultOutput());
                    return;

                } catch (UnknownHostException ex) {
                    Logger.getLogger(CryptoNode.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(CryptoNode.class.getName()).log(Level.SEVERE, null, ex);
                }
                


            }
            


        }
    }

    private class IncCounter implements Task {

        public void execute() {
            synchronized (LOCK) {
                INC_CTR_MSG mes = null;
                dump("Incremenet Counter value");


                mes = new INC_CTR_MSG(nodeId, bid);
                try {
                    networkSend.sendTCP(mes);
                } catch (SocketTimeoutException e) {
                    specialDump("TCP: " + nodeId + ":" + mes.getDest() + " might be dead!");
                    taskManager.registerTask(new IncCounter(), (long) exp(1) * period);
                    //notInc=true;
                    return;
                } catch (ConnectException e) {
                    dump("TCP: " + nodeId + ":" + mes.getDest() + " is dead!");
                    taskManager.registerTask(new ResultOutput());
                    return;
                } catch (UnknownHostException ex) {
                    Logger.getLogger(CryptoNode.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(CryptoNode.class.getName()).log(Level.SEVERE, null, ex);
                }
               // notInc=false;

            }
        }
    }

    private void receiveCounter(COUNTER_MSG msg) {

        if (!receivedAllRumors) {
            synchronized (LOCK) {
                dump("Received counter value");


                if (isFirstDiffusion) {
                    
                currentCounter = msg.counter;

                Random generator;
                    firstRound = currentRound;
                    currentCounter++;
                    taskManager.registerTask(new IncCounter());

                    generator = new Random();
                    currentNeighbour = generator.nextInt(VOTERCOUNT - 1);
                    isFirstDiffusion = false;
                    dump("First Time Received");


               }  
//                {
//                    currentNeighbour = (currentNeighbour + 1) % (VOTERCOUNT - 1);
//                }
//                if (isFirstView) {
//                    currentCounter++;
//                    taskManager.registerTask(new IncCounter());
//
//                }

                if (currentCounter == VOTERCOUNT) {
                    {
                        dump("All received rumors.");
                        receivedAllRumors = true;
                        taskManager.registerTask(new ResultOutput());
                        return;
                    }
                } else {
                 //   taskManager.registerTask(new RumorDiffusion(), (long) exp(1) * period);
                        taskManager.registerTask(new RumorDiffusion(), (long)  period);

                }

            }
        }
    }

    /**
     * Return a real number from an exponential distribution with rate lambda.
     */
    public double exp(double lambda) {
        double time = -Math.log(1 - Math.random()) / lambda;
        dump("time: " + time);
        return time;
    }

    private void receiveRumor(RUMOR_MSG msg) {

        if (!receivedAllRumors) {
            synchronized (LOCK) {
                // msg.round>=currentRound &&
                if (Math.random() < LOSS) {
                        dump("Discarded rumor: loss");
                        return;
                    }
                else if (isFirstReception) {
                    

                    dump("Received rumor from" + msg.getSrc());
                    //update current counter
                    currentRound = msg.round;
                    MRRumors++;
                    taskManager.registerTask(new ReadCounter());
                    
                    isFirstReception = false;
                } else {
                    dump("Discarded rumor: duplicate");
                    MRRumors++;
                    MDuplicates++;
                    
                }

            }
        }
    }

    private class ResultOutput implements Task {

        public void execute() {
            synchronized (LOCK) {
                long duration = 0;
                if (IAmSource) {
                    duration = System.currentTimeMillis() - startTime;
                }


                specialDump("\r" + " " + MSRumors + " " + MRRumors + " " + firstRound + " " + currentRound  + "\r");
                isResultOutputed = true;
                // taskManager.registerTask(new AttemptSelfDestruct());
                taskManager.registerTask(new SelfDestructTask());
            }


        }
    }
    //
//    private class ViewDiffusion implements Task {
//
//        public void execute() {
//            synchronized (LOCK) {
//                if (!isViewDiffusionOver) {
//                    //       taskManager.registerTask(new PreemptCloseLocalCountingTask(), CLOSE_COUNTING_DELAY);
//                    //   if (!(peerView.size()<=1)) {
//                    //specialDump("ViewDiffusion");
//                    Set<E_CryptoNodeID> tempSet;
//                    CRYPTO_VIEW_MSG mes = null;
//                    for (int i = 1; i < numClusters; i++) {
//                        tempSet = nodeToCluster.get(i);
//                        for (E_CryptoNodeID peerId : tempSet) {
//
//                            dump("Send a viewto " + peerId);
//
//                            try {
//                                mes = new CRYPTO_VIEW_MSG(nodeId, peerId, nodeToCluster.get((peerId.groupId)), nodeToCluster.get((peerId.groupId + 1) % numClusters), nodeToCluster.get((peerId.groupId + numClusters - 1) % numClusters));
//                                doSendTCP(mes);
//                            } catch (Exception e) {
//                                dump("TCP: cannot vote");
//                            }
//                            MSView++;
//                            SMSView += getObjectSize(mes);
//                        }
//                    }
//
//
//                    isViewDiffusionOver = true;
//                    //  taskManager.registerTask(new PreemptPartialTallyingTask(), CLOSE_PARTIAL_TALLYING_DELAY);
//                    //     aggrLocalTally(Emsg);
//                    //  taskManager.registerTask(new AttemptSelfDestruct());
//                    //     taskManager.registerTask(new CloseVoteTask());
//
//
//                }
//            }
//        }
//    }
//
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
//
//    private class VoteTask implements Task {
//
//        public void execute() {
//            if (!isVoteTaskOver) {
//                //specialDump("VoteTask");
//                voteTimes++;
//                dump ("VoteTimes: "+voteTimes);
//                
//                startInstant = System.nanoTime();
//
//                CRYPTO_BALLOT_MSG mes = null;
//
//                taskManager.registerTask(new PreemptCloseLocalCountingTask(), CLOSE_COUNTING_DELAY);
//                //   synchronized (LOCK) {
//
//                if (!(peerView.size() <= 1)) {
//                    dump("psize: " + peerView.size());
//                    for (E_CryptoNodeID peerId : peerView) {
//                        if (peerId.equals(nodeId)) {
//                            continue;
//                        }
//                        dump("Send a '" + Emsg + "' ballot to " + peerId);
//                        try {
//
//                            mes = new CRYPTO_BALLOT_MSG(nodeId, peerId, Emsg);
//
//                            doSendTCP(mes);
//                        } catch (Exception e) {
//                            dump("TCP: cannot vote");
//                        }
//                    }
//                    MSVote += peerView.size() - 1;
//                    SMSVote += getObjectSize(mes) * (peerView.size() - 1);
//
//                } else {
//                    dump("Cannot vote: no peer view");
//
//                }
//                isVoteTaskOver = true;
//                taskManager.registerTask(new PreemptPartialTallyingTask(), CLOSE_PARTIAL_TALLYING_DELAY);
//
////                    BigInteger ballot = Emsg;
////
////                    localTally = encryptor.add(localTally, ballot);
////                    numBallots++;
////
////                    System.out.println("ballots " + numBallots + " " + peerView.size());
////
////                    if (numBallots >= (int) (Math.floor(peerView.size() * thresholdBallot))) {
////                        System.out.println((int) (Math.floor(peerView.size() * thresholdBallot)));
////                        computedLocalTally = true;
////                        if (IAmThreshold) {
////                            partialTally = localTally;
////                            taskManager.registerTask(new GlobalCountingTask());
////                        } else if (computedPartialTally) {
////                            partialTally = encryptor.add(localTally, partialTally);
////                            taskManager.registerTask(new GlobalCountingTask());
////                        }
////                        isLocalCountingOver = true;
////                        taskManager.registerTask(new AttemptSelfDestruct());
////
////                        //else do nothing
////                    }
//
//
//                aggrLocalTally(Emsg);
//                //taskManager.registerTask(new AttemptSelfDestruct());
//                //     taskManager.registerTask(new CloseVoteTask());
//
//            }
//            //  }
//
//        }
//    }
//
//    public void aggrLocalTally(BigInteger ballot) {
//        synchronized (BallotLOCK) {
//
//
//            localTally = encryptor.add(localTally, ballot);
//            numBallots++;
//
//
//            System.out.println("ballots " + numBallots + " " + peerView.size());
//
//            if (numBallots >= (int) (Math.floor(peerView.size() * thresholdBallot))) {
//                System.out.println((int) (Math.floor(peerView.size() * thresholdBallot)));
//                computedLocalTally = true;
//                if (IAmThreshold) {
//                    partialTally = localTally;
//                    taskManager.registerTask(new GlobalCountingTask());
//                } else if (computedPartialTally) {
//                    partialTally = encryptor.add(localTally, partialTally);
//                    taskManager.registerTask(new GlobalCountingTask());
//                }
//                isLocalCountingOver = true;
//                taskManager.registerTask(new AttemptSelfDestruct());
//
//                //else do nothing
//            }
//        }
//
//
//    }
//
//    private class AttemptSelfDestruct implements Task {
//
//        public void execute() {
//            System.out.println("IsPartialTallyingOver:" + IsPartialTallyingOver);
//            System.out.println("isVoteTaskOver:" + isVoteTaskOver);
//            System.out.println("isLocalCountingOver:" + isLocalCountingOver);
//            System.out.println("computedFinalResult:" + computedFinalResult);
//            System.out.println("isResultDiffusionOver:" + isResultDiffusionOver);
//            System.out.println("isShareSendingOver:" + isShareSendingOver);
//
//            synchronized (LOCK) {
//                if (IsPartialTallyingOver && isVoteTaskOver && isLocalCountingOver && computedFinalResult && isResultDiffusionOver && isShareSendingOver) {
//
//                    /*		       try {
//                    doSendTCP(new DEAD_MSG(nodeId, bootstrap));
//                    dump("sent a dead message");
//                    }catch (Exception e) {
//                    dump("TCP: cannot send dead message to bootstrap");
//                    } 
//                     */
//
//                    endInstant = System.nanoTime();
//                    runningTime = endInstant - startInstant + viewDuration;
//                    //     dump("Running Time: "+runningTime);
//                    taskManager.registerTask(new ResultOutput());
//
//
//                }
//            }
//
//
//        }
//    }
//    private class PreemptCloseLocalCountingTask implements Task {
//
//        public void execute() {
//            synchronized (LOCK) {
//                //specialDump("PreemptCloseLocalCountingTask");
//                if (!isLocalCountingOver) {//actually close the local counting session
//
//                    if (IAmThreshold) {
//                        partialTally = localTally;
//                        taskManager.registerTask(new GlobalCountingTask());
//                    } else if (computedPartialTally) {
//                        partialTally = encryptor.add(localTally, partialTally);
//                        taskManager.registerTask(new GlobalCountingTask());
//                        isLocalCountingOver = true;
//
//                    }
//                }
//
//            }
//        }
//    }
//
//    private class PreemptPartialTallyingTask implements Task {
//
//        public void execute() {
//            synchronized (LOCK) {
//                if (!IsPartialTallyingOver) {//actually close the local counting session
//                    //specialDump ("PreemptPartialTallyingTask");
//                    partialTally = mostPresent(partialTallies);
//                    computedPartialTally = true;
//
//                    if (IAmThreshold) {
//                        finalEncryptedResult = partialTally;
//                        taskManager.registerTask(new TallyDecryptionSharing());
//                    } else if (computedLocalTally) {
//                        partialTally = encryptor.add(localTally, partialTally);
//                        taskManager.registerTask(new GlobalCountingTask());
//                        isShareSendingOver = true;
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
//                    //specialDump("PreemptCloseTallyDecryptionSharing");
//
//                    //actually close the Tally Decryption Sharing session
//                    isDecryptionSharingOver = true;
//                    isShareSendingOver = true;
//
//                    taskManager.registerTask(new TallyDecryption());
//                }
//            }
//        }
//    }
//
//    private class GlobalCountingTask implements Task {
//
//        //   private int localTallyGroupId;
//        public void execute() {
//            synchronized(LOCK){
//            // broadcast
//            dump("GlobalCountingTask at begin");
//            if (!IsPartialTallyingOver) {
//                //specialDump("GlobalCountingTask");
//                CRYPTO_PARTIAL_TALLY_MSG mes = null;
//                taskManager.registerTask(new PreemptResultDiffusionTask(), CLOSE_ResultDiffusion_DELAY);
//                dump("GlobalCountingTask");
//
////                    if (isMalicious) {
////                        partialTally = encryptor.encrypt(votes[0].multiply(BigInteger.valueOf(nodeId.groupId + 1)));
////                    }
//
//                for (E_CryptoNodeID proxyId : proxyView) {
//                    dump("Send partial tally (" + partialTally + ") to " + proxyId);
//                    try {
//                        mes = new CRYPTO_PARTIAL_TALLY_MSG(nodeId, proxyId, partialTally);
//                        doSendTCP(mes);
//                    } catch (Exception e) {
//                        dump("TCP: cannot broadcast local tally");
//                    }
//
//                }
//
//                IsPartialTallyingOver = true;
//                MSPartial += proxyView.size();
//                SMSPartial += getObjectSize(mes);
//                // taskManager.registerTask(new CloseGlobalCountingTask());
//                taskManager.registerTask(new AttemptSelfDestruct());
//            }
//            dump("GlobalCountingTask at end");
//        }
//        }
//    }
//
//    private class TallyDecryptionSharing implements Task {
//
//        public void execute() {
//            synchronized (LOCK) {
//                taskManager.registerTask(new PreemptCloseTallyDecryptionSharing(), CLOSE_DecryptionSharing_DELAY);
//                if (!isShareSendingOver) {
//                    isShareSendingOver = true;
//
//                    //specialDump("TallyDecryptionSharing");
//                    dump("TallyDecryptionSharing");
//
//                    dump("final encrypted:" + finalEncryptedResult.toString());
//                    long startT = System.nanoTime();
//                    nodeResultShare = secKey.decrypt(finalEncryptedResult);
//                    ShareCompTime += System.nanoTime() - startT;
//
//
//
//                    resultSharesList.add(nodeResultShare);
//
//                    currentDecodingIndex++;
//
//                    isFinalResultCalculated = true;
//                    dump("sharesize: " + currentDecodingIndex);
//
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
//                                doSendTCP(mes);
//                            } catch (Exception e) {
//                                dump("TCP: cannot send decryption share");
//                            }
//                        }
//
//                        MSShare += peerView.size() - 1;
//                        SMSShare += getObjectSize(mes) * (peerView.size() - 1);
//
//                    } else {
//                        receiveSTOP(new STOP_MSG(nodeId, nodeId, "cannot share result share: no peer view"));
//                    }
////                    isShareSendingOver = true;
//                    //}
//
//
//                    if (currentDecodingIndex >= MINTALLIES) {
//                        dump("CloseTallyDecryptionSharing");
//                        //actually close the Tally Decryption Sharing session
//                        taskManager.registerTask(new TallyDecryption());
//                    }
//
//                    taskManager.registerTask(new AttemptSelfDestruct());
//
//                }
//            }
//        }
//    }
//
//    private class TallyDecryption implements Task {
//
//        public void execute() {
//            synchronized (LOCK) {
//                if (!isTallyDecryptionOver) {
//                    //specialDump("TallyDecryption");
//                    isDecryptionSharingOver = true;
//                    PartialDecryption[] decArray = new PartialDecryption[resultSharesList.size()];
//                    //        System.out.println("shares: ");
//                    for (int i = 0; i < resultSharesList.size(); i++) {
//                        decArray[i] = resultSharesList.get(i);
//                        System.out.println(" " + decArray[i].getDecryptedValue());
//                    }
//                    System.out.println("decaraysize: " + resultSharesList.size());
//
//                    long startT = System.nanoTime();
//
//                    finalResult = secKey.combineShares(decArray);
//                    VoteDecTime += System.nanoTime() - startT;
//
//                    computedFinalResult = true;
//                    dump("Determined final result:" + finalResult);
//
//                    isTallyDecryptionOver = true;
//
//                    taskManager.registerTask(new ResultDiffusionTask());
//                    taskManager.registerTask(new AttemptSelfDestruct());
//
//
//                }
//            }
//        }
//    }
//
//    private class ResultDiffusionTask implements Task {
//
//        public void execute() {
//            // broadcast
//            synchronized (LOCK) {
//                if (!isResultDiffusionOver) {
//                    if (!(numClusters == nodeId.groupId + 1)) {
//                        //specialDump("ResultDiffusionTask");
//                        dump("ResultDiffusionTask at begin");
//
//                        CRYPTO_FINAL_RESULT_MSG mes = null;
//                        for (E_CryptoNodeID proxyId : proxyView) {
//
////                        if (isMalicious) {
////                            finalResult = votes[0].multiply(BigInteger.valueOf(VOTERCOUNT));
////                        }
//                            dump("Send final result (" + finalResult + ") to " + proxyId);
//                            try {
//                                mes = new CRYPTO_FINAL_RESULT_MSG(nodeId, proxyId, finalResult);
//                                doSendTCP(mes);
//                            } catch (Exception e) {
//                                dump("TCP: cannot broadcast final result");
//                            }
//
//                        }
//                        MSResult += proxyView.size();
//                        SMSResult += getObjectSize(mes) * proxyView.size();
//
//                    }
//                    isResultDiffusionOver = true;
//
//                    dump("ResultDiffusionTask at end");
//                    taskManager.registerTask(new AttemptSelfDestruct());
//
//                }
//            }
//
//        }
//    }
//
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
//                if (numFinalResults >= (int) (Math.floor(clientView.size() * threshold))) {
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
//
//    private class PreemptResultDiffusionTask implements Task {
//
//        public void execute() {
//            synchronized (LOCK) {
//
//                if (!computedFinalResult) {//actually close the local counting session
//                    //specialDump("PreemptResultDiffusionTask");
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
//                doSendTCP(new IAM_MSG(nodeId, bootstrap, getGroupId(), isMalicious));
//            } catch (Exception e) {
//                dump("TCP: cannot announce myself");
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
