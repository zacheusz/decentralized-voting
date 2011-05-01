package runtime.executor;

import runtime.executor.E_CryptoNodeID;
import runtime.NodeIDReader;

public class E_NodeIDReader implements NodeIDReader {
	public E_CryptoNodeID readBootstrapNodeID(String str) {
		String[] data = str.split(" ");
		if (data.length < 2) {
			System.err
					.println("error! E_CryptoNodeID \"" + str + "\" has a bad format");
			System.exit(1);
		}
		E_CryptoNodeID id = new E_CryptoNodeID(data[0], Integer.parseInt(data[1]),Boolean.parseBoolean(data[2]));
		return id;
	}

	public E_CryptoNodeID readNodeID(String str, int port,boolean isMalicious) {
		E_CryptoNodeID id = new E_CryptoNodeID(str, port,isMalicious);
		return id;
	}

	// public int getNodeIDSize() {
	// return 2;
	// }
}
