package launchers.executor;

import java.util.HashMap;

import protocol.node.Bootstrap;
import runtime.NetworkSend;
import runtime.TaskManager;
import runtime.executor.E_ConnectionListener;
import runtime.executor.E_NetworkSend;
import runtime.executor.E_NodeID;
import runtime.executor.E_ThreadPerTaskTaskManager;

public class BootstrapLauncher {

	public static void main(String[] args) throws Exception {

		HashMap<String, String> arguments = new HashMap<String, String>();
		for (int i = 0; i < args.length; i++) {
			arguments.put(args[i], args[i + 1]);
			i++;
		}
		Setup.configure(arguments);
		
		// From Launcher
		String name = arguments.get("-name");
		int port = Integer.parseInt(arguments.get("-port"));

		E_NodeID id = new E_NodeID(name, port);

		TaskManager t = new E_ThreadPerTaskTaskManager();
		NetworkSend networkSend = new E_NetworkSend();
		Bootstrap bt = new Bootstrap(id, t, networkSend);

		((E_ThreadPerTaskTaskManager) t).setSimpleNode(bt);
		new Thread(new E_ConnectionListener(port, t, bt)).start();
	}

	private static void printUsage() {
		System.out.println("BootstrapLauncher -name inet_name -p port [-fileName log_file]");
	}

}
