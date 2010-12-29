package launchers.executor;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;

import protocol.communication.STOP_MSG;
import runtime.executor.E_NetworkSend;
import runtime.executor.E_CryptoNodeID;

public class TheKiller {

	public static void main(String[] args) {
		
		HashMap<String, String> arguments = new HashMap<String, String>();
		for (int i = 0; i < args.length - 1; i=i+2) {
			arguments.put(args[i], args[i + 1]);
		}
				
		int port = Integer.parseInt(arguments.get("-port"));
		String  name = arguments.get("-name");
		
		if(name == null) {
			System.err.println("Usage : TheKiller -name host_name -port port_number [-message custom_message]");
			System.exit(1);
		}
		
		String message = arguments.get("-message");
		
		message = (message==null)?"no message":message;
		E_CryptoNodeID srcId = new E_CryptoNodeID("God",0,-1);
		E_CryptoNodeID destId = new E_CryptoNodeID(name,port,-1);
		
		try {
				(new E_NetworkSend()).sendUDP(new STOP_MSG(srcId,destId,message));
		} catch (SocketTimeoutException e) {
			System.err.println("UDP: " + name + ":" + " might be dead!");
		} catch (ConnectException e) {
			System.err.println("UDP: " + name + ":" + " is dead!");
		} catch (UnknownHostException e) {
			System.err.println("UDP: unknown hostname: " + name);
		} catch (IOException e) {
			System.err.println("UDP: IO error: " + e.getMessage());
		}
	}
	
}
