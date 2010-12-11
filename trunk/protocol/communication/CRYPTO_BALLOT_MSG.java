package protocol.communication;

import protocol.communication.Message;
import runtime.NodeID;
import OldVoting.Vote;

public class CRYPTO_BALLOT_MSG extends Message {
	private static final long serialVersionUID = 1L;
	private Vote vote;
	
	public CRYPTO_BALLOT_MSG(NodeID src, NodeID dest, Vote vote) {
		super(Message.BALLOT, src, dest);
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
