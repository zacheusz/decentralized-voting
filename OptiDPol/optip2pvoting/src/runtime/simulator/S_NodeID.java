package runtime.simulator;

import runtime.NodeID;

public class S_NodeID extends NodeID {
	public final int id;
	private final int randomId;

	public S_NodeID(int id, int randomId) {
		this.id = id;
		this.randomId = randomId;
	}

	public int getRandomId() {
		return randomId;
	}

	@Override
	public String toString() {
		return "Node " + id;
	}

	@Override
	public boolean equals(Object obj) {
		S_NodeID s_id = (S_NodeID) obj;
		return (id == s_id.id);
	}

	@Override
	public int hashCode() {
		return id;
	}

	public String getName() {
		return this.toString();
	}

	public int getPort() {
		return 0;
	}

}
