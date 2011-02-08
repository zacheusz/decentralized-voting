package launchers.executor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import protocol.node.SimpleNode;
import runtime.NetworkSend;
import runtime.NodeID;
import runtime.NodeIDReader;
import runtime.Stopper;
import runtime.TaskManager;
import runtime.executor.E_ConnectionListener;
import runtime.executor.E_NetworkSend;
import runtime.executor.E_NodeID;
import runtime.executor.E_NodeIDReader;
import runtime.executor.E_Stopper;
import runtime.executor.E_ThreadPerTaskTaskManager;

public class SimpleGossipLauncher {

	public static void main(String[] args) throws Exception {

		HashMap<String, String> arguments = new HashMap<String, String>();
		for (int i = 0; i < args.length; i++) {
			arguments.put(args[i], args[i + 1]);
			i++;
		}
		Setup.configure(arguments);
		
		// From Launcher
		String bset = arguments.get("-bset");
		String name = arguments.get("-name");
		int port = Integer.parseInt(arguments.get("-port"));

		E_NodeIDReader nodeIDReader = new E_NodeIDReader();
		E_NodeID id = new E_NodeID(name, port);

		TaskManager taskManager = new E_ThreadPerTaskTaskManager();
		NetworkSend networkSend = new E_NetworkSend();

		Set<NodeID> bootstrapSet = initBootstrapSet(bset, nodeIDReader);

		Stopper stopper = new E_Stopper(taskManager);


		SimpleNode node = new SimpleNode(id,taskManager,networkSend,stopper,bootstrapSet.iterator().next());

		((E_ThreadPerTaskTaskManager) taskManager).setSimpleNode(node);

		new Thread(new E_ConnectionListener(port, taskManager, node)).start();
	}

	static void printUsage() {
		System.out
				.println("SimpleGossipLauncher -name inet_name -p port"
						+ "-bset boostrapset_file" + "-fileName output_filename");
	}

	public static Set<NodeID> initBootstrapSet(String bootstrapSetFile,
			NodeIDReader nodeIDReader) {
		try {
			Set<NodeID> bootstrapSet = new HashSet<NodeID>();
			FileReader fr = new FileReader(bootstrapSetFile);
			BufferedReader inBuff = new BufferedReader(fr);
			String peer = "";
			while ((peer = inBuff.readLine()) != null) {
				NodeID id = nodeIDReader.readBootstrapNodeID(peer);
				bootstrapSet.add(id);
			}
			inBuff.close();
			fr.close();
			return bootstrapSet;
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
			return null;
		}
	}
}
