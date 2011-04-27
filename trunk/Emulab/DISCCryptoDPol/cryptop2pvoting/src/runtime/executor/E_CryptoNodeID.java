package runtime.executor;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import protocol.node.CryptoNode;


public class E_CryptoNodeID implements Externalizable {
	private static final long serialVersionUID = 1L;
        public String name;
	public int port;
	public int groupId;
        public static int NB_GROUPS;

	public E_CryptoNodeID() {
	}

	public E_CryptoNodeID(String name, int port ) {
                this.name = name;
		this.port = port;
              
	}


        public int getOrder(){
            
               int hash =hashCode();
                hash = (hash<0)?-hash:hash;
                return hash % CryptoNode.VOTERCOUNT;
        }
	public String getName() {
		return name;
	}

	public int getPort() {
		return port;
	}

	@Override
	public boolean equals(Object obj) {
		E_CryptoNodeID id = (E_CryptoNodeID) obj;
		return (name.equals(id.name) && port == id.port && groupId==id.groupId);
	}

	@Override
	public int hashCode() {
		return name.hashCode() ^ port;
	}

	@Override
	public String toString() {
		return "" + name + ":" + port; //+" ("+groupId+")" ;
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
