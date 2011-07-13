package protocol.communication;

//import OldVoting.DecodingShare;
import java.io.Serializable;
import java.math.BigInteger;
import paillierp.PartialDecryption;
import protocol.communication.Message;
import runtime.executor.E_CryptoNodeID;
import zkp.DecryptionZKP;

public class BROADCAST_MSG extends Message implements Serializable {
	private static final long serialVersionUID = 1L;
        private BroadcastInfo info=null;
       
        
    
            
	public BROADCAST_MSG(E_CryptoNodeID src, E_CryptoNodeID dest, BroadcastInfo info) {
		
            super(Message.BROADCAST_DATA_MSG, src, dest);
		this.info=info;
       
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
