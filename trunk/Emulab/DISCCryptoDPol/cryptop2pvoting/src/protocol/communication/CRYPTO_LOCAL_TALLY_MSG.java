package protocol.communication;

import protocol.communication.Message;
import runtime.executor.E_CryptoNodeID;
//import OldVoting.Vote;
import java.math.BigInteger;

public class CRYPTO_LOCAL_TALLY_MSG extends Message {
	private static final long serialVersionUID = 1L;
	private BigInteger tally;
	private int groupId;
	
	public CRYPTO_LOCAL_TALLY_MSG(E_CryptoNodeID src, E_CryptoNodeID dest, BigInteger tally, int groupId) {
		super(Message.CRYPTO_LOCAL_TALLY_MSG, src, dest);
		this.tally = tally;
		this.groupId = groupId;
	}
	
	public BigInteger getTally() {
		return tally;
	}

	public int getGroupId() {
		return groupId;
	}
	
	@Override
	public void doCopy(Message msg) {
		super.doCopy(msg);
		
		CRYPTO_LOCAL_TALLY_MSG m = (CRYPTO_LOCAL_TALLY_MSG) msg;
		tally = m.tally;
		groupId = m.groupId;
	}
}
