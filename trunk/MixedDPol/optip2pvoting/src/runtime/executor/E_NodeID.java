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
	public int NB_BALLOTS;			//NB_BALLOTS d'un noeud

	public E_NodeID() {
	}

	public E_NodeID(String name, int port, int groupId) {
		this.name = name;
		this.port = port;
                this.groupId=groupId;
		this.NB_BALLOTS=GM_NB_BALLOTS();			// NB_BALLOTS = GM_NB_BALLOTS();
	}
        
        public E_NodeID(String name, int port) {
		this.name = name;
		this.port = port;
	}
	public String getName() {
		return name;
	}

	public int getPort() {
		return port;
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

	private int GM_NB_BALLOTS() {				//Fonction retournant aléatoirement 1 ou NB_BALLOTS_MAX
	int lower = 1;
	int higher = 10;
	int random = (int)(Math.random()* (higher-lower)) + lower;
	if ((random%3 == 0)||(random%5 == 0)) //0,1,2,4,8,7
		return Node.NB_BALLOTS_MAX;
	else 
		return 1;		//3,5,6,9
	}
}
