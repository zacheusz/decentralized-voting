package protocol.communication;

import protocol.communication.Message;
import runtime.executor.E_CryptoNodeID;
import OldVoting.Vote;
import java.math.BigInteger;

public class CRYPTO_BALLOT_MSG extends Message {
	private static final long serialVersionUID = 1L;
	private Vote vote;
	
	public CRYPTO_BALLOT_MSG(E_CryptoNodeID src, E_CryptoNodeID dest, Vote vote) {
		super(Message.CRYPTO_BALLOT, src, dest);
		this.vote = vote;
	}
	
	public Vote getVote() {
		return vote;
	}
	
	@Override
	public void doCopy(Message msg) {
		super.doCopy(msg);
		
		CRYPTO_BALLOT_MSG m = (CRYPTO_BALLOT_MSG) msg;
		vote = m.vote;
	}

}
