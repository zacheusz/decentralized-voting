package protocol.communication;

import protocol.communication.Message;
import runtime.NodeID;
import OldVoting.Vote;

public class CRYPTO_INDIVIDUAL_TALLY_MSG extends Message {
	private static final long serialVersionUID = 1L;
	private Vote tally;
	
	public CRYPTO_INDIVIDUAL_TALLY_MSG(NodeID src, NodeID dest, Vote tally) {
		super(Message.CRYPTO_INDIVIDUAL_TALLY_MSG, src, dest);
		this.tally = tally;
	}
	
	public Vote getTally() {
		return tally;
	}
	
	@Override
	public void doCopy(Message msg) {
		super.doCopy(msg);
		
		CRYPTO_INDIVIDUAL_TALLY_MSG m = (CRYPTO_INDIVIDUAL_TALLY_MSG) msg;
		tally = m.tally;
	}

}
