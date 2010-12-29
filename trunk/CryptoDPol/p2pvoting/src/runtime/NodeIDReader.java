package runtime;

import runtime.executor.E_CryptoNodeID;

public interface NodeIDReader {
	E_CryptoNodeID readBootstrapNodeID(String str);
	E_CryptoNodeID readNodeID(String str, int port);

}
