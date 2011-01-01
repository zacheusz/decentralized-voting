package runtime.executor;

import runtime.NodeID;
import runtime.NodeIDReader;

public class E_NodeIDReader implements NodeIDReader {
	public NodeID readBootstrapNodeID(String str) {
		String[] data = str.split(" ");
		if (data.length < 2) {
			System.err
					.println("error! NodeID \"" + str + "\" has a bad format");
			System.exit(1);
		}
		E_NodeID id = new E_NodeID(data[0], Integer.parseInt(data[1]));
		return id;
	}

	public NodeID readNodeID(String str, int port) {
		E_NodeID id = new E_NodeID(str, port);
		return id;
	}

	// public int getNodeIDSize() {
	// return 2;
	// }
}
