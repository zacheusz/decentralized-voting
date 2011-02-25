package launchers.simulator;

import java.util.HashMap;
import java.util.Random;

import protocol.node.Bootstrap;
import protocol.node.SimpleNode;
import runtime.MTRandom;
import runtime.NodeID;
import runtime.simulator.S_NodeID;
import runtime.simulator.S_Simulator;

public class SimpleNodeLauncher {

	public static void main(String[] args) throws Exception {
		HashMap<String, String> arguments = new HashMap<String, String>();
		for (int i = 0; i < args.length - 1; i=i+2) {
			arguments.put(args[i], args[i + 1]);

		}

		int nbNodes = Integer.parseInt(arguments.get("-nbNodes"));
		if(arguments.get("-proportion") != null)
			SimpleNode.VOTE_RATIO = Double.parseDouble(arguments.get("-proportion"));
		if(arguments.get("-decision") != null)
			SimpleNode.DECISION_THRESHOLD = Double.parseDouble(arguments.get("-decision"));		
		
		S_Simulator simulator = new S_Simulator();

		Random r = new MTRandom();
		S_NodeID bootstrapId = new S_NodeID(0, r.nextInt());

		Bootstrap bt;
		bt = new Bootstrap(bootstrapId, simulator, simulator);

		simulator.addNode(bt);

		// Assign a behavior for the node
		for (int i = 1; i < nbNodes + 1; i++) {
			NodeID id = new S_NodeID(i, r.nextInt());

			// Create the node
			SimpleNode node = new SimpleNode(id,simulator,simulator,simulator,bootstrapId);
			// And add to simulation
			simulator.addNode(node);
		}

		// Fire it up
		simulator.start();
		simulator.thread.join();
	}
}
