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
    private final static int BOOTSTRAP_CONTACT_TIMEOUT = 40000;
    private static int GET_PEER_VIEW_FROM_BOOTSTRAP_DELAY = 40000;				// Duration of the joining phase: 19 seconds to get peers
    private static int GET_PROXY_VIEW_FROM_BOOTSTRAP_DELAY = GET_PEER_VIEW_FROM_BOOTSTRAP_DELAY + 40000;
    //                                1  second  to get proxies
    private static int VOTE_DELAY = GET_PROXY_VIEW_FROM_BOOTSTRAP_DELAY + 40000;// Delay before voting: 50 seconds
    private static int CLOSE_VOTE_DELAY = VOTE_DELAY + 490 * 1000; 				// Duration of the local voting phase: 1 minute
    private static int CLOSE_COUNTING_DELAY = CLOSE_VOTE_DELAY + 20 * 1000;		// Duration of the local counting phase: 1 minute
    private static int CLOSE_GLOBAL_COUNTING_DELAY = CLOSE_COUNTING_DELAY + 20 * 1000;		// Duration of the local counting phase: 1 minute
    private static int CLOSE_DecryptionSharing_DELAY = CLOSE_GLOBAL_COUNTING_DELAY + 20 * 1000;
    private static int CLOSE_TallyDecryption_DELAY = CLOSE_DecryptionSharing_DELAY + 20 * 1000;
    private static int SELF_DESTRUCT_DELAY = CLOSE_TallyDecryption_DELAY + 20 * 1000;
    private static int COUNTING_PERIOD = 20 * 1000;								// Duration of epidemic dissemination: 20 seconds
    public static int VOTECOUNT;
    public static int VOTERCOUNT;
    public static int kvalue;
    public static int MINTALLIES;
    public static int nodesPerMachine;
    public static ClusterChoice nodeToCluster;
    public static int chosenCluster;
    public static int numClusters;
    public static Map<E_CryptoNodeID, Integer> IDAssignment = new HashMap<E_CryptoNodeID, Integer>();
     
    public static Map<E_CryptoNodeID, Integer> finalIDAssignment = new HashMap<E_CryptoNodeID, Integer>();
     
    Set<E_CryptoNodeID> smallestCluster;
    public int numRecvClusterAssign = 0;
    public int numRecvFinalClusterAssign = 0;
    public ClusterAssignment clusterAssign;
    public boolean IAmSmallest =false;
    public int numPartialTallies=0;
    public boolean computedLocalTally=false;
    public boolean computedPartialTally=false;
    protected BigInteger partialTally=BigInteger.ONE;
    protected List<BigInteger> partialTallies = new LinkedList<BigInteger>();
    public int numBallots=0;
    public boolean isResultDiffusionOver=false;
    public int numFinalResults=0;
    protected List<BigInteger> finalResults = new LinkedList<BigInteger>();
    public boolean computedFinalResult=false;
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
    protected List<E_CryptoNodeID> peerView = new LinkedList<E_CryptoNodeID>();
    protected List<E_CryptoNodeID> proxyView = new LinkedList<E_CryptoNodeID>();
    protected List<E_CryptoNodeID> clientView = new LinkedList<E_CryptoNodeID>();
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
        this.bootstrap = bootstrap;
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
        for (i = 0; i < E_CryptoNodeID.NB_GROUPS; i++) {
            this.localTallySets[i] = new HashMap<E_CryptoNodeID, BigInteger>();
            this.localTallies[i] = BigInteger.ONE;

        }

        try {
//            taskManager.registerTask(new AnnouncerTask());
//            taskManager.registerTask(new GetViewFromBootstrapTask(GetViewFromBootstrapTask.PEERS), GET_PEER_VIEW_FROM_BOOTSTRAP_DELAY);
//            taskManager.registerTask(new GetViewFromBootstrapTask(GetViewFromBootstrapTask.PROXIES), GET_PROXY_VIEW_FROM_BOOTSTRAP_DELAY);
//            taskManager.registerTask(new VoteTask(), VOTE_DELAY);
       //     taskManager.registerTask(new PreemptCloseLocalElectionTask(), CLOSE_VOTE_DELAY);
            taskManager.registerTask(new PreemptCloseLocalCountingTask(), CLOSE_COUNTING_DELAY);
            taskManager.registerTask(new PreemptCloseGlobalCountingTask(), CLOSE_GLOBAL_COUNTING_DELAY);
            taskManager.registerTask(new PreemptCloseTallyDecryptionSharing(), CLOSE_DecryptionSharing_DELAY);
            taskManager.registerTask(new PreemptTallyDecryption(), CLOSE_TallyDecryption_DELAY);
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
                case Message.CLUSTER_ASSIGN_MSG:
                    receiveClusterAssign((CLUSTER_ASSIGN_MSG) msg);
                    break;
                case Message.FINAL_CLUSTER_ASSIGN_MSG:
                    receiveFinalClusterAssign((FINAL_CLUSTER_ASSIGN_MSG) msg);
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


    private void receiveBallot(CRYPTO_BALLOT_MSG msg) throws NoSuchAlgorithmException {
        synchronized (LOCK) {
            synchronized (localTally) {
                if (!isLocalCountingOver) {
                    dump("Received a ballot (" + msg.getVote() + ") from " + msg.getSrc());

                    aggrLocalTally(msg.getVote());
             
     
                        taskManager.registerTask(new CloseLocalCountingTask());
                }
                 else {
                    dump("Discarded an ballot message (cause: sent too late)");
                }
            }
        }
    }

    private void receiveDecryptionShare(CRYPTO_DECRYPTION_SHARE_MSG msg) throws NoSuchAlgorithmException {
        synchronized (LOCK) {

            if (!isDecryptionSharingOver) {
                dump("Received a decryption share (" + msg.getShare() + ") from " + msg.getSrc());
                //          dump("Inputs to check share:"+msg.getShare()+" "+finalEncryptedResult);
                //              if (res.CheckShare(msg.getShare(), finalEncryptedResult)) {
//                dump("Received Share is legal."+ "from " + msg.getSrc());
                //   resultShares.put(msg.getSrc(), msg.getShare());
                //          if (resultSharesList[msg.getShareOrder()]!=null)
                //		dump("existing order");

                resultSharesList.add(msg.getShare());

                currentDecodingIndex++;
                dump("sharesize: " + currentDecodingIndex);
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

    private void receivePartialTally(CRYPTO_PARTIAL_TALLY_MSG msg) {

     //   int groupId = msg.getGroupId();

//        if (groupId == getPreviousGroupId()) {
//            return;
//        }

        synchronized (LOCK) {
      //      if (!IAmSmallest) {

                //             synchronized (localTallySets[groupId]) {
                synchronized (localTallies) {

                        dump("Received a partial tally (" + msg.getTally() + ") from " + msg.getSrc());
                        numPartialTallies++;
                        
                        partialTallies.add(msg.getTally());                    

                        if (numPartialTallies==clientView.size())
                        {
                           partialTally=mostPresent(partialTallies);
                           computedPartialTally =true;
                           
                           if (IAmSmallest)
                           {
                               
                               finalEncryptedResult=partialTally;
                               taskManager.registerTask(new TallyDecryptionSharing());
                           }
                           else if (computedLocalTally)
                           {   
                               partialTally=encryptor.add(localTally,partialTally);
                               taskManager.registerTask(new GlobalCountingTask());
                           }
                        }
                        
                //        taskManager.registerTask(new GlobalCountingTask(groupId));

                    }


                }
            }
    
    
            private BigInteger mostPresent(List<BigInteger> values) {
                
                int c, max = 0;
                BigInteger argmax = BigInteger.ZERO;
                
                for(BigInteger i: values) {
                        c = 0;
                        for(BigInteger j: values)
                                if(j.compareTo(i)==0)
                                        c++;
                        
                        if(c>max) {
                                argmax = i;
                                max = c;
                        }
                }
                return argmax;
        }
        
    //    }
    
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

    //each node announces its cluster chosen in phase 1
    private class positionAnnouncerTask implements Task {

        public void execute() {
            //choose the cluster
            numClusters = (int) ((Math.log(VOTERCOUNT) / 2) / (kvalue * Math.log(VOTERCOUNT / 2)));
            Random generator = new Random();
            chosenCluster = generator.nextInt(numClusters);

            //spread the data
            //chose random node 

            E_CryptoNodeID randomNodeID = getRandomNodeID();

            nodeToCluster = new ClusterChoice();

            nodeToCluster.add(chosenCluster, nodeId);


            //  startDiffInfo();


        }
    }

    //each node in smallest cluster sends its choices for each node's cluster
    private class randomIDAssignerTask implements Task {

        public void execute() {
            synchronized (smallestCluster) {

                if (nodeToCluster.getSmallestCluster() == chosenCluster) {
                    IAmSmallest=true;
                    Random generator = new Random();
                    Set<E_CryptoNodeID> currentCluster;
                    Map<E_CryptoNodeID, Integer> myIDAssignment = new HashMap<E_CryptoNodeID, Integer>();
                    int assignedOrder = 0;
                    Iterator it;
                    //assign random clusters to nodes
                    for (int i = 0; i < numClusters; i++) {
                        currentCluster = nodeToCluster.get(chosenCluster);

                        it = currentCluster.iterator();

                        while (it.hasNext()) {
                            assignedOrder = generator.nextInt((int) Math.pow(VOTERCOUNT, 3));
                            myIDAssignment.put((E_CryptoNodeID) it.next(), assignedOrder);
                        }
                    }

                    //send choices to smallest cluster's member
                    smallestCluster = nodeToCluster.get(chosenCluster);
                    aggrIDAssign(myIDAssignment);
                    for (E_CryptoNodeID peerId : smallestCluster) {
                        dump("Send cluster assignment to " + peerId);
                        try {
                            doSendTCP(new CLUSTER_ASSIGN_MSG(nodeId, peerId, myIDAssignment));
                        } catch (Exception e) {
                            dump("TCP: cannot send cluster assignment");
                        }
                    }
                }
            }
        }
    }
    
//each node in the smallest cluster diffuses the final assignment to the network
    private class finalAssignAnnouncerTask implements Task {

        public void execute() {
            synchronized (LOCK) {
                //synchronized (localTallies) {
            }
        }
    }

//nodes in smallest cluster receieve each others' random assignments
    private void receiveClusterAssign(CLUSTER_ASSIGN_MSG msg) throws NoSuchAlgorithmException {
        synchronized (LOCK) {
            //synchronized (localTallies) {

            dump("Received a cluster assignment from " + msg.getSrc());

            Map<E_CryptoNodeID, Integer> recIDAssign = msg.getIDAssignment();
            aggrIDAssign(recIDAssign);

            numRecvClusterAssign++;

            if (numRecvClusterAssign == smallestCluster.size() - 1) { 
                
                clusterAssign=new ClusterAssignment(IDAssignment);
                taskManager.registerTask(new finalAssignAnnouncerTask());

            }

        }
    }

//    public class ClusterAssignment {
//
//        List< Set<E_CryptoNodeID>> nodeToClusterList;
//        public int steps;
//
//        private void add(int chosenCluster, E_CryptoNodeID nodeID) {
//            nodeToClusterList.get(chosenCluster).add(nodeID);
//        }
//
//        private Set<E_CryptoNodeID> get(int cluster) {
//            return nodeToClusterList.get(cluster);
//        }
//
//        public ClusterAssignment() {
//            E_CryptoNodeID id;            
//            int cluster = 0;
//            int count = 1;
//            Set<E_CryptoNodeID> singleSet = null;
//            nodeToClusterList = new LinkedList< Set< E_CryptoNodeID>>();
//            for (Iterator i = sortByValue(IDAssignment).iterator(); i.hasNext();) {
//                id = (E_CryptoNodeID) i.next();                
//                cluster = (int) Math.floor(count / (kvalue * Math.log(VOTERCOUNT)));
//                count++;
//                if (nodeToClusterList.get(cluster) == null) {
//                    singleSet = new HashSet<E_CryptoNodeID>();
//                    singleSet.add(id);
//                } else {
//                    nodeToClusterList.get(cluster).add(id);
//                }
//            }
//        }
//    }

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
//each node receives the final assignment as a part of diffusion process
    private void receiveFinalClusterAssign(FINAL_CLUSTER_ASSIGN_MSG msg) throws NoSuchAlgorithmException {
        synchronized (LOCK) {
            //synchronized (localTallies) {

            dump("Received a final cluster assignment from " + msg.getSrc());

            ClusterAssignment recIDAssign = msg.getClusterAssignment();
      //      mergeIDAssign(recIDAssign);

            numRecvFinalClusterAssign++;

            if (IDAssignment.size() == VOTERCOUNT) {
                
                //taskManager.registerTask(new finalAssignAnnouncerTask());
                
            }

        }
    }

    private void mergeIDAssign(Map<E_CryptoNodeID, Integer> recIDAssign) {
        //     synchronized (LOCK) {
        synchronized (IDAssignment) {
            synchronized (smallestCluster) {
                if (IDAssignment.isEmpty()) {
                    IDAssignment = recIDAssign;
                } else {
                    Integer order;

                    for (Map.Entry<E_CryptoNodeID, Integer> entry : recIDAssign.entrySet()) {
                        order = IDAssignment.get(entry.getKey());
                        if (order == null) {
                            IDAssignment.put(entry.getKey(), entry.getValue());
                            smallestCluster.add(entry.getKey());
                        }
                    }
                }

            }
        }
    }

    private void aggrIDAssign(Map<E_CryptoNodeID, Integer> recIDAssign) {
        //     synchronized (LOCK) {
        synchronized (IDAssignment) {
            if (IDAssignment.isEmpty()) {
                IDAssignment = recIDAssign;
            } else {
                Integer order;

                for (Map.Entry<E_CryptoNodeID, Integer> entry : recIDAssign.entrySet()) {
                    order = IDAssignment.get(entry.getKey());
                    if (order == null) {
                        IDAssignment.put(entry.getKey(), entry.getValue());
                        smallestCluster.add(entry.getKey());
                    } else {
                        IDAssignment.put(entry.getKey(), (entry.getValue() + order) % (int) Math.pow(VOTERCOUNT, 3));
                    }
                }


            }
            //       }

        }
    }


    private E_CryptoNodeID getRandomNodeID() {

        Random generator = new Random();
        int randomHost = generator.nextInt(VOTERCOUNT / nodesPerMachine) + 1;
        String randomHostName = "node-" + randomHost;
        int randomPort = generator.nextInt(nodesPerMachine) + nodeId.port;

        E_CryptoNodeID randomNodeID = new E_CryptoNodeID(randomHostName, randomPort, -1);

        return randomNodeID;
    }



//nodes send their presence to the bootsrap (unused)    
    private class AnnouncerTask implements Task {

        public void execute() {
            try {
                dump("sending to bootstrap: " + bootstrap);
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


            if (!peerView.isEmpty()) {
                //Vote ballot = vote;
                startInstant = (new Date()).getTime();

                for (E_CryptoNodeID peerId : peerView) {
                    dump("Send a '" + Emsg + "' ballot to " + peerId);
                    try {
                        /*	if(isMalicious && ballot) {
                        dump("Corrupted vote to " + proxyId);
                        doSendTCP(new CRYPTO_BALLOT_MSG(nodeId, proxyId, !ballot));
                        }
                        else {
                         */
                        doSendTCP(new CRYPTO_BALLOT_MSG(nodeId, peerId, Emsg));
                        break;
                        //	}
                    } catch (Exception e) {
                        dump("TCP: cannot vote");
                    }
                    //ballot = !ballot;


                }
                isVoteTaskOver = true;
        //        taskManager.registerTask(new AttemptSelfDestruct());
           //     taskManager.registerTask(new CloseVoteTask());
                aggrLocalTally(Emsg);
            } else {
                dump("Cannot vote: no peer view");
            }
            //     }
            //  }
        }
    }
   
        public void aggrLocalTally(BigInteger ballot) {

                
               localTally = encryptor.add(localTally, ballot);
               numBallots++;
               if(numBallots==peerView.size()+1)
               {
                   computedLocalTally=true;
                   if (IAmSmallest)
                   {
                       partialTally=localTally;
                       taskManager.registerTask(new GlobalCountingTask());
                   }
                   else if (computedPartialTally)
                   {     partialTally=encryptor.add(localTally,partialTally);
                       taskManager.registerTask(new GlobalCountingTask());
                   }
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
                if (isGlobalCountingOver && isVoteTaskOver && isIndivSendingOver && isResultOutputed) {
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

//    private class CloseLocalElectionTask implements Task {
//
//        public void execute() {
//            synchronized (LOCK) {
//
//                //actually close the local vote session
//                dump("CloseLocalElectionTask");
//                isLocalVoteOver = true;
//                try {
//                    //	dump("tally=" + ((individualTally>0)?"+":"") + individualTally);
//                    // schedule local counting
//                    //   taskManager.registerTask(new receiveSelfIndividualTallyTask());
//                    receiveIndividualTally(new CRYPTO_INDIVIDUAL_TALLY_MSG(nodeId, nodeId, individualTally));
//
//                } catch (NoSuchAlgorithmException ex) {
//                    Logger.getLogger(CryptoNode.class.getName()).log(Level.SEVERE, null, ex);
//
//                }
//
//                taskManager.registerTask(new LocalCounting()); //, ((long) (Math.random() * COUNTING_PERIOD)));
//
//            }
//        }
//    }

//    private class PreemptCloseLocalElectionTask implements Task {
//
//        public void execute() {
//            synchronized (LOCK) {
//                if (!isLocalVoteOver) {//actually close the local counting session
//                    isLocalVoteOver = true;
//
//                    taskManager.registerTask(new CloseLocalElectionTask());
//                }
//
//            }
//        }
//    }

    private class PreemptCloseLocalCountingTask implements Task {

        public void execute() {
            synchronized (LOCK) {
                if (!isLocalCountingOver) {//actually close the local counting session
                    isLocalCountingOver = true;

                    taskManager.registerTask(new CloseLocalCountingTask());
                }

            }
        }
    }

    private class PreemptCloseGlobalCountingTask implements Task {

        public void execute() {
            synchronized (LOCK) {
                if (!isGlobalCountingOver) {//actually close the local counting session
                    isGlobalCountingOver = true;

                    taskManager.registerTask(new CloseGlobalCountingTask());
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
                if (!isDecryptionSharingOver) {
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
                if (!isTallyDecryptionOver) {
                    dump("PreemptTallyDecryption");

                    //actually close the Tally Decryption Sharing session
                    isTallyDecryptionOver = true;
                    taskManager.registerTask(new TallyDecryption());
                }
            }
        }
    }

    private class GlobalCountingTask implements Task {

     //   private int localTallyGroupId;

        public GlobalCountingTask() {
          //  this.localTallyGroupId = groupId;
        }

        public void execute() {
            // broadcast
            dump("GlobalCountingTask at begin");

            synchronized (LOCK) {
                //       synchronized (proxyView) {
           //     if (!isGlobalCountingOver) {

                    dump("GlobalCountingTask");
       //             nbSentLocalTallies++;
      //              if (localTallyGroupId != nodeId.groupId) { //we don't send this value to the next group since it is the originator

                        for (E_CryptoNodeID proxyId : proxyView) {
                            dump("Send local tally (" + partialTally + ") to " + proxyId);
                            try {
                                doSendUDP(new CRYPTO_PARTIAL_TALLY_MSG(nodeId, proxyId, partialTally));
                            } catch (Exception e) {
                                dump("UDP: cannot broadcast local tally");
                            }
                            break; //only send to one proxy.
                        }
        //            }
                    //check if the node has all the groups' tallies
                    //      boolean done = true;
//                        for (BigInteger mytally : localTallies) {
//
//                            if (mytally.equals(BigInteger.ZERO)) {
//                                done = false;
//                                break;
//                            }
//                        }
//                    dump("nbSentLocalTallies: " + nbSentLocalTallies);
//                    if (nbSentLocalTallies == nodeId.NB_GROUPS) {
//                        taskManager.registerTask(new CloseGlobalCountingTask());
//                        isGlobalCountingOver = true;
//                        taskManager.registerTask(new AttemptSelfDestruct());
//                    } else {
//                        dump("still not done");
//                    }
           //     }
                          isGlobalCountingOver = true;
                          taskManager.registerTask(new CloseGlobalCountingTask());
//                        taskManager.registerTask(new AttemptSelfDestruct());
            }
            //      }
            dump("GlobalCountingTask at end");
        }
    }

//    private class LocalCounting implements Task {
//
//        public void execute() {
//            synchronized (LOCK) {
//                //   synchronized (peerView) {
//                dump("LocalCounting");
//
//                if (!peerView.isEmpty()) {
//                    for (E_CryptoNodeID peerId : peerView) {
//                        dump("Send individual tally (" + individualTally + ") to " + peerId);
//                        try {
//                            doSendTCP(new CRYPTO_INDIVIDUAL_TALLY_MSG(nodeId, peerId, individualTally));
//                        } catch (Exception e) {
//                            dump("TCP: cannot send individual tally");
//                        }
//                    }
//                    isIndivSendingOver = true;
//                    taskManager.registerTask(new AttemptSelfDestruct());
//                } else {
//                    receiveSTOP(new STOP_MSG(nodeId, nodeId, "cannot count: no peer view"));
//                }
//            }
//            //    }
//        }
//    }

    private class TallyDecryptionSharing implements Task {

        public void execute() {
            synchronized (LOCK) {

                if (!isDecryptionSharingOver) {
                    dump("TallyDecryptionSharing");
//                    finalEncryptedResult = BigInteger.ONE;
//
//                    for (BigInteger mytally : localTallies) {
//
//                        //       dump("input1: " + finalEncryptedResult + "\ninput2: " + mytally);
//                        finalEncryptedResult = encryptor.add(finalEncryptedResult, mytally);
//                        //     dump("output: " + finalEncryptedResult);
//                    }

                    dump("final encrypted:" + finalEncryptedResult.toString());
                    nodeResultShare = secKey.decrypt(finalEncryptedResult);
                    //    resultShares.put(nodeId, nodeResultShare);
                    //	  if (resultSharesList[shareOrder]!=null)
                    //      	dump("existing order");

                    resultSharesList.add(nodeResultShare);
                    currentDecodingIndex++;
                    isFinalResultCalculated = true;
                    dump("sharesize: " + currentDecodingIndex);







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
                    if (currentDecodingIndex == MINTALLIES) {
                        taskManager.registerTask(new CloseTallyDecryptionSharing());
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

                    PartialDecryption[] decArray = new PartialDecryption[resultSharesList.size()];
                    for (int i = 0; i < resultSharesList.size(); i++) {
                        decArray[i] = resultSharesList.get(i);
                    }

                    finalResult = secKey.combineShares(decArray);
                    computedFinalResult=true;
                    dump("Determined final result:" + finalResult);
                    //              try {
                    //                Thread.currentThread().sleep(10000);
                    //          } catch (InterruptedException ex) {
                    //            Logger.getLogger(CryptoNode.class.getName()).log(Level.SEVERE, null, ex);
                    //      }
                    isTallyDecryptionOver = true;
                  
                    taskManager.registerTask(new ResultDiffusionTask());



                }
            }
        }
    }
    
    private class ResultDiffusionTask implements Task {

     //   private int localTallyGroupId;

        public ResultDiffusionTask() {
          //  this.localTallyGroupId = groupId;
        }

        public void execute() {
            // broadcast
            dump("ResultDiffusionTask at begin");

            synchronized (LOCK) {

                        for (E_CryptoNodeID proxyId : proxyView) {
                            dump("Send local tally (" + finalResult + ") to " + proxyId);
                            try {
                                doSendUDP(new CRYPTO_FINAL_RESULT_MSG(nodeId, proxyId, finalResult));
                            } catch (Exception e) {
                                dump("UDP: cannot broadcast local tally");
                            }
                            break; //only send to one proxy.
                        }

            }
            //      }
            dump("ResultDiffusionTask at end");
        }
    }

    private void receiveFinalResult(CRYPTO_FINAL_RESULT_MSG msg) {

     //   int groupId = msg.getGroupId();

//        if (groupId == getPreviousGroupId()) {
//            return;
//        }

        synchronized (LOCK) {
            if (!computedFinalResult) {

                //             synchronized (localTallySets[groupId]) {
                synchronized (localTallies) {

                        dump("Received a final result (" + msg.getResult() + ") from " + msg.getSrc());
                        numFinalResults++;
                        
                        finalResults.add(msg.getResult());                    

                        if (numPartialTallies==clientView.size())
                        {
                           finalResult=mostPresent(finalResults);
                           computedFinalResult =true;
                           
                           if (IAmSmallest)
                           {
                               
                               taskManager.registerTask(new ResultOutput());
                               taskManager.registerTask(new AttemptSelfDestruct());
                           }
                           else  {   
                               
                               taskManager.registerTask(new ResultDiffusionTask());
                           }
                        }
                        
                //        taskManager.registerTask(new GlobalCountingTask(groupId));

                    }


                }
            }
    }
    private class ResultOutput implements Task {

        public void execute() {
            synchronized (LOCK) {
                paillierp.testingPaillier.TestingRest.getResult(finalResult, VOTECOUNT, votes);
                try {
                    doSendTCP(new DEAD_MSG(nodeId, bootstrap));
                } catch (UnknownHostException ex) {
                    Logger.getLogger(CryptoNode.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(CryptoNode.class.getName()).log(Level.SEVERE, null, ex);
                }
                isResultOutputed = true;
               // taskManager.registerTask(new AttemptSelfDestruct());
            }


        }
    }
}
