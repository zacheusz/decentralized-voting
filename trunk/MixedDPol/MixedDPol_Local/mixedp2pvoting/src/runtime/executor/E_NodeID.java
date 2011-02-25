package runtime.executor;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import runtime.NodeID;

public class E_NodeID extends NodeID implements Externalizable {
	private static final long serialVersionUID = 1L;
	public String name;
	public int port;
      	public int groupId=-1;
	public int NB_BALLOTS;		// NB_BALLOTS

	public E_NodeID() {
		this.NB_BALLOTS=GM_NB_BALLOTS();	// Initialisation of NB_BALLOTS
	}

	public E_NodeID(String name, int port, int groupId) {
		this.name = name;
		this.port = port;
                this.groupId=groupId;
                this.NB_BALLOTS=GM_NB_BALLOTS();	// Initialisation of NB_BALLOTS
	}
        
        public E_NodeID(String name, int port) {
		this.name = name;
		this.port = port;
		this.NB_BALLOTS=GM_NB_BALLOTS();	// Initialisation of NB_BALLOTS

	}
	public String getName() {
		return name;
	}

	public int getPort() {
		return port;
	}

	private int GM_NB_BALLOTS() {			//random NB_BALLOTS
	int lower = 1;
	int higher = 10;
	int random = (int)(Math.random() * (higher-lower)) + lower;
	if ((random % 3 == 0) || (random % 5 == 0))
		return 3; //Node.NB_BALLOTS_MAX;
	else	
		return 1;	
	}

	@Override
	public boolean equals(Object obj) {
		E_NodeID id = (E_NodeID) obj;
		return (name.equals(id.name) && port == id.port  && groupId==id.groupId);
	}

	@Override
	public int hashCode() {
		return name.hashCode() ^ port;
	}

	@Override
	public String toString() {
		return "" + name + ":" + port ;
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		name = in.readUTF();
		port = in.readInt();
                groupId = in.readInt();


	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(name);
		out.writeInt(port);
                out.writeInt(groupId);

	}
}
