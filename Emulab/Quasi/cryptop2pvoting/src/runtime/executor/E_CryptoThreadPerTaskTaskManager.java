package runtime.executor;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import protocol.node.CryptoNode;


import runtime.Receiver;
import runtime.Task;
import runtime.TaskManager;

public class E_CryptoThreadPerTaskTaskManager implements TaskManager {
	public static Timer timer = new Timer();
	private Receiver r = null;

	public E_CryptoThreadPerTaskTaskManager() {
	}

	public E_CryptoThreadPerTaskTaskManager(Receiver r) {
		this.r = r;
	}


	public void setCryptoNode(Receiver r) {
		this.r = r;
	}
	public void registerTask(Task t) {
		try {
			new Thread(new TimerHelper(t)).start();
		} catch (Exception e) {
			System.out
					.println("Exception at E_ThreadPerTaskTaskManager.registerTask() "
							+ e.getMessage());
			System.exit(0);
		}
	}

	public void registerTask(Task t, long timeout) {
		try {
			timer.schedule(new TaskExecutor(t), timeout);
		} catch (Exception e) {
			System.out
					.println("Exception at E_ThreadPerTaskTaskManager.registerTask() "
							+ e.getMessage());
			System.exit(0);
		}
	}

	public void registerTask(Task t, long timeout, long period) {
		try {
			// timer.schedule(new TaskExecutor(t), timeout);
			timer.scheduleAtFixedRate(new TaskExecutor(t), timeout, period); // AtFixedRate
		} catch (Exception e) {
			System.out
					.println("Exception at E_ThreadPerTaskTaskManager.registerTask() "
							+ e.getMessage());
			System.exit(0);
		}
	}

	class TaskExecutor extends TimerTask {
		Task t;

		public TaskExecutor(Task t) {
			this.t = t;
		}

		public void run() {
			long lateness = System.currentTimeMillis()
					- scheduledExecutionTime();

			if (lateness >= 10000) {
				String header = "";
				OperatingSystemMXBean mxb = ManagementFactory
						.getOperatingSystemMXBean();
				if (r != null) {
					header = r.toString() + " -> ";
					if (!r.isStopped()) {
						((CryptoNode) r).stopped = true;
						r.dump("[ERROR][HIGHLOAD] " + r.toString()
								+ " late by " + lateness
								+ " ms !!! (Huge CPU load? Load:"
								+ mxb.getSystemLoadAverage() + ")");
					}
				}
				System.out.println("\033[1;36m" + System.currentTimeMillis()
						+ " " + Calendar.getInstance().getTime() + " " + header
						+ "Task: " + t.getClass().toString() + " late by "
						+ lateness + " ms !!! (Huge CPU load? Load: "
						+ mxb.getSystemLoadAverage() + ")\033[0m");

				System.exit(-1);
				// } else {
				// System.out.println("Task: " + t + " late by " +
				// lateness
				// + " ms");
			}
			// }
			t.execute();
		}
	}

	class TimerHelper implements Runnable {
		Task t;

		public TimerHelper(Task t) {
			this.t = t;
		}

		public void run() {
			t.execute();
		}
	}

}
