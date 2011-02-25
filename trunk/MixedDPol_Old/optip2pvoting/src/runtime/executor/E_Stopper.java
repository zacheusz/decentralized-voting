package runtime.executor;

import runtime.Receiver;
import runtime.Stopper;
import runtime.Task;
import runtime.TaskManager;

public class E_Stopper implements Stopper {

	TaskManager manager = null;

	// Used for stopping the program
	public E_Stopper(TaskManager manager) {
		this.manager = manager;
	}

	// Executor waits some time (minimun GOSSIP_PERIOD * TTL), and then kills
	// this node
	public void stopNode(Receiver node) {
		// manager.registerTask(new DieTask(node.toString()),
		// Node.GOSSIPING_PERIOD * 10);
		manager.registerTask(new DieTask(node.toString()));
	}

	public void stopNode(Receiver node, String message) {
		manager.registerTask(new DieTask(node.toString(), message));
	}

	// Kill me
	public class DieTask implements Task {
		String nodeId;
		String message;

		public DieTask(String nodeId) {
			this.nodeId = nodeId;
			this.message = "";
		}

		public DieTask(String nodeId, String message) {
			this.nodeId = nodeId;
			this.message = message;

		}

		public void execute() {
			try {
				// System.out.println(nodeId + " quits cleanly... (with
				// Stopper)");
				if (!(message == null || message.equals(""))) {
					System.out.print("\033[31m" + message + "\n\033[0m");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.exit(0);
		}
	}

}
