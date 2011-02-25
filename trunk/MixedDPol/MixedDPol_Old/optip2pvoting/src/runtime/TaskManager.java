package runtime;

public interface TaskManager {
	void registerTask(Task t);
	void registerTask(Task t, long timeout);
	void registerTask(Task t, long timeout, long period);
}
