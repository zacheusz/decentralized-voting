package runtime;

public interface Stopper {
	void stopNode(Receiver node);

	void stopNode(Receiver node, String message);
}
