package runtime;

import java.io.IOException;
import java.net.UnknownHostException;

import protocol.communication.Message;

public interface NetworkSend {
	public void sendUDP(Message msg) throws UnknownHostException, IOException;

	public void sendTCP(Message msg) throws UnknownHostException, IOException;
}