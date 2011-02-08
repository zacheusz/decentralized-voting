package runtime;

import protocol.communication.Message;

public interface Receiver {

	void receive(Message msg);

	public String toString();

	public void dump(String message);

	public boolean isStopped();

}