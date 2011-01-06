package runtime;

public interface NodeIDReader {
	NodeID readBootstrapNodeID(String str);
	NodeID readNodeID(String str, int port);

}
