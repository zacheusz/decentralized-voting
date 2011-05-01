package launchers.executor;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashMap;

import protocol.node.Node;
import protocol.node.CryptoNode;
import runtime.executor.E_CryptoNodeID;

public abstract class CryptoSetup {

    public static void configure(HashMap<String, String> arguments) throws Exception {
        if (arguments.get("-alpha") != null) {
            CryptoNode.VOTE_RATIO = Double.parseDouble(arguments.get("-alpha"));
        }
        if (arguments.get("-beta") != null) {
            CryptoNode.MALICIOUS_RATIO = Double.parseDouble(arguments.get("-beta"));
        }
        if (arguments.get("-decision") != null) {
            CryptoNode.DECISION_THRESHOLD = Double.parseDouble(arguments.get("-decision"));
        }
        if (arguments.get("-fileName") != null) {
            Node.out = new PrintStream(new BufferedOutputStream(new FileOutputStream(arguments.get("-fileName"))));
        }
//		if(arguments.get("-nbGroups") != null) {
//			E_CryptoNodeID.NB_GROUPS = Integer.parseInt(arguments.get("-nbGroups"));
//		}

        if (arguments.get("-votecount") != null) {
            CryptoNode.VOTECOUNT = Integer.parseInt(arguments.get("-votecount"));

        }

//                if(arguments.get("-mintallies") != null) {
//                                    CryptoNode.MINTALLIES = Integer.parseInt(arguments.get("-mintallies"));
//
//                }                
        if (arguments.get("-nbVoters") != null) {
            CryptoNode.VOTERCOUNT = Integer.parseInt(arguments.get("-nbVoters"));
        }
        if (arguments.get("-kvalue") != null) {
            CryptoNode.kvalue = Integer.parseInt(arguments.get("-kvalue"));
        }

        if (arguments.get("-nodesPerMachine") != null) {
            CryptoNode.nodesPerMachine = Integer.parseInt(arguments.get("-nodesPerMachine"));
        }
//                if(arguments.get("-stepsConstant") != null) {
//                                    CryptoNode.stepsConstant = Integer.parseInt(arguments.get("-stepsConstant"));
//                }                    
        if (arguments.get("-basicPort") != null) {
            CryptoNode.basicPort = Integer.parseInt(arguments.get("-basicPort"));
        }
        if (arguments.get("-epsilon") != null) {
            CryptoNode.epsilon = Double.parseDouble(arguments.get("-epsilon"));
        }
        if (arguments.get("-order") != null) {
            CryptoNode.order = Integer.parseInt(arguments.get("-order"));
        }

    }
}
