package launchers.executor;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashMap;

import protocol.node.Node;
import protocol.node.SimpleNode;
import runtime.NodeID;

public abstract class Setup {

	public static void configure(HashMap<String,String> arguments) throws Exception {
		if(arguments.get("-alpha")!=null)
			SimpleNode.VOTE_RATIO = Double.parseDouble(arguments.get("-alpha"));
		if(arguments.get("-beta")!=null)
			SimpleNode.MALICIOUS_RATIO = Double.parseDouble(arguments.get("-beta"));		
		if(arguments.get("-decision") != null)
			SimpleNode.DECISION_THRESHOLD = Double.parseDouble(arguments.get("-decision"));	
		if(arguments.get("-fileName") != null) {
			Node.out = new PrintStream(new BufferedOutputStream(new FileOutputStream(arguments.get("-fileName"))));
		}
		if(arguments.get("-nbGroups") != null) {
			NodeID.NB_GROUPS = Integer.parseInt(arguments.get("-nbGroups"));
            }
		if(arguments.get("-nbBallots") != null) {					// Modif nbBALLOTS -> nbBALLOTS_MAX
			Node.NB_BALLOTS_MAX = Integer.parseInt(arguments.get("-nbBallots"));

		}		
	}
	
}
