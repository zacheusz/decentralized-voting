package protocol.communication;

//import OldVoting.DecodingShare;
import java.io.Serializable;
import java.math.BigInteger;
import paillierp.PartialDecryption;
import protocol.communication.Message;
import protocol.node.CryptoNode.BroadcastInfo;
import runtime.executor.E_CryptoNodeID;
import zkp.DecryptionZKP;

public class BROADCAST_MSG extends Message implements Serializable {
	private static final long serialVersionUID = 1L;
        private BroadcastInfo info=null;
        private E_CryptoNodeID actualSrc=null;
        
    
            
	public BROADCAST_MSG(E_CryptoNodeID src, E_CryptoNodeID dest, E_CryptoNodeID actualSrc, BroadcastInfo info) {
		
            super(Message.BROADCAST_DATA_MSG, src, dest);
		this.info=info;
                this.actualSrc=actualSrc;
	}
	
	public BroadcastInfo getInfo() {
		return info;
	}
       

	@Override
	public void doCopy(Message msg) {
		super.doCopy(msg);
		
		BROADCAST_MSG m = (BROADCAST_MSG) msg;
		info=m.info;
	}

}
