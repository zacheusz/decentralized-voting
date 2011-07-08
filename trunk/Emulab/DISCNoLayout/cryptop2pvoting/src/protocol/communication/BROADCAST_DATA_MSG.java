package protocol.communication;

//import OldVoting.DecodingShare;
import java.math.BigInteger;
import paillierp.PartialDecryption;
import protocol.communication.Message;
import runtime.executor.E_CryptoNodeID;
import zkp.DecryptionZKP;

public class BROADCAST_DATA_MSG extends Message {
	private static final long serialVersionUID = 1L;
        private BigInteger vote;
	private PartialDecryption share;
        public byte type;
        
    
            
	public BROADCAST_DATA_MSG(E_CryptoNodeID src, E_CryptoNodeID dest,  PartialDecryption share,BigInteger vote, byte type) {
		
            super(Message.BROADCAST_DATA_MSG, src, dest);
		this.share = share;
                this.vote=vote;
                this.type=type;
	}
	
	public BigInteger getVote() {
		return vote;
	}
        
        public PartialDecryption getShare() {
		return share;
	}

	@Override
	public void doCopy(Message msg) {
		super.doCopy(msg);
		
		BROADCAST_DATA_MSG m = (BROADCAST_DATA_MSG) msg;
		share = m.share;
                vote=m.vote;
	}

}
