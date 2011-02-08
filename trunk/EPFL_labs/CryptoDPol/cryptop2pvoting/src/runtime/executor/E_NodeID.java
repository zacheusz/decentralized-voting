//package runtime.executor;
//
//import java.io.Externalizable;
//import java.io.IOException;
//import java.io.ObjectInput;
//import java.io.ObjectOutput;
//
//import runtime.NodeID;
//
//public class E_NodeID extends NodeID implements Externalizable {
//	private static final long serialVersionUID = 1L;
//	public String name;
//	public int port;
//
//	public E_NodeID() {
//	}
//
//	public E_NodeID(String name, int port) {
//		this.name = name;
//		this.port = port;
//	}
//
//	public String getName() {
//		return name;
//	}
//
//	public int getPort() {
//		return port;
//	}
//
//	@Override
//	public boolean equals(Object obj) {
//		E_NodeID id = (E_NodeID) obj;
//		return (name.equals(id.name) && port == id.port);
//	}
//
//	@Override
//	public int hashCode() {
//		return name.hashCode() ^ port;
//	}
//
//	@Override
//	public String toString() {
//		return "" + name + ":" + port ;
//	}
//
//	@Override
//	public void readExternal(ObjectInput in) throws IOException,
//			ClassNotFoundException {
//		name = in.readUTF();
//		port = in.readInt();
//
//	}
//
//	@Override
//	public void writeExternal(ObjectOutput out) throws IOException {
//		out.writeUTF(name);
//		out.writeInt(port);
//
//	}
//}
