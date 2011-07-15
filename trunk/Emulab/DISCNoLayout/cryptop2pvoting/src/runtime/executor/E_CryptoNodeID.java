package runtime.executor;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import protocol.node.CryptoNode;


public class E_CryptoNodeID implements Externalizable {
	private static final long serialVersionUID = 1L;
        public String name;
	public int port;
	public int groupId;
        public boolean isMalicious;
        public int nodeOrder=-1;
      //  public static int NB_GROUPS;

	public E_CryptoNodeID() {
	}

	public E_CryptoNodeID(String name, int port, boolean  isMalicious ) {
                this.name = name;
		this.port = port;
                this.isMalicious=isMalicious;
              
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
		return (name.equals(id.name) && port == id.port );
	}

	@Override
	public int hashCode() {
            byte[] thedigest = null;
        try {
            byte[] bytesOfMessage = (name+port).getBytes("UTF-8");
            MessageDigest md = MessageDigest.getInstance("MD5");
            thedigest= md.digest(bytesOfMessage);
                
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(E_CryptoNodeID.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(E_CryptoNodeID.class.getName()).log(Level.SEVERE, null, ex);
        }
           return (new String(thedigest)).hashCode();

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
                isMalicious=in.readBoolean();
                nodeOrder=in.readInt();

	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(name);
		out.writeInt(port);
                out.writeInt(groupId);
                out.writeBoolean(isMalicious);
                out.writeInt(nodeOrder);

	}
}
