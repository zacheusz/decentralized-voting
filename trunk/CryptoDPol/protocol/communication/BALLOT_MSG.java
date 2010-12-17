package protocol.communication;

import protocol.communication.Message;
import runtime.NodeID;

public class BALLOT_MSG extends Message {
	private static final long serialVersionUID = 1L;
	private boolean vote;	
	
	public BALLOT_MSG(NodeID src, NodeID dest, boolean vote) {
		super(Message.BALLOT, src, dest);
		this.vote = vote;
	}
	
	public boolean getVote() {
		return vote;
	}
	
	@Override
	public void doCopy(Message msg) {
		super.doCopy(msg);
		
		BALLOT_MSG m = (BALLOT_MSG) msg;
		vote = m.vote;
	}

}
