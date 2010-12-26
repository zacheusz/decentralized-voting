package launchers.simulator;

import OldVoting.PublicKey;
import OldVoting.Trusted;
import java.util.HashMap;
import java.util.Random;

import protocol.node.Bootstrap;
import protocol.node.CryptoNode;
import protocol.node.SimpleNode;
import runtime.MTRandom;
import runtime.NodeID;
import runtime.simulator.S_NodeID;
import runtime.simulator.S_Simulator;

public class CryptoNodeLauncher {

	public static void main(String[] args) throws Exception {
		HashMap<String, String> arguments = new HashMap<String, String>();
		for (int i = 0; i < args.length - 1; i=i+2) {
			arguments.put(args[i], args[i + 1]);

		}

		int nbNodes = Integer.parseInt(arguments.get("-nbNodes"));
		if(arguments.get("-proportion") != null)
			CryptoNode.VOTE_RATIO = Double.parseDouble(arguments.get("-proportion"));
		if(arguments.get("-decision") != null)
			CryptoNode.DECISION_THRESHOLD = Double.parseDouble(arguments.get("-decision"));
		//setup voting
                CryptoNode.VOTERCOUNT = Integer.parseInt(arguments.get("-votercount"));
                CryptoNode.VOTECOUNT = Integer.parseInt(arguments.get("-votecount"));
                CryptoNode.TALLYCOUNT = Integer.parseInt(arguments.get("-tallycount"));
                CryptoNode.MINTALLIES = Integer.parseInt(arguments.get("-mintallies"));
                CryptoNode.CERTAINTY = Integer.parseInt(arguments.get("-certainty"));

             
                 int power = 2;
                 int bits = 256;
                 int hashsize = 16;
                int i; 
                Trusted trusted;
                PublicKey pub; // the public key is shared by all the voters

                String[] vote_names = new String[CryptoNode.VOTECOUNT]; //candidates
                for (i = 0; i < CryptoNode.VOTECOUNT; i++)   
                            vote_names[i] = "Vote " + i;

                trusted = new Trusted (bits, power, hashsize, CryptoNode.TALLYCOUNT, CryptoNode.MINTALLIES, CryptoNode.CERTAINTY);//generates the secret key
                trusted.produceKeyShares();
                trusted.MakeSelectionElection ("Gore for president?", vote_names);//generates the public key specific for this setup
                pub = trusted.GetPublicKey ();//gets the public key shared between the voters




		S_Simulator simulator = new S_Simulator();

		Random r = new MTRandom();
		S_NodeID bootstrapId = new S_NodeID(0, r.nextInt());

		Bootstrap bt;
		bt = new Bootstrap(bootstrapId, simulator, simulator);

		simulator.addNode(bt);
                
		// Assign a behavior for the node
		for ( i = 1; i < nbNodes + 1; i++) {
			NodeID id = new S_NodeID(i, r.nextInt());

			// Create the node

			CryptoNode node = new CryptoNode(id,simulator,simulator,simulator,bootstrapId,trusted.GetSecretDistributedKeyPart (i-1),pub);
			// And add to simulation
			simulator.addNode(node);
		}

		// Fire it up
		simulator.start();
		simulator.thread.join();
	}
}
