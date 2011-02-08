package protocol.communication;

import protocol.communication.Message;
import runtime.NodeID;

public class INDIVIDUAL_TALLY_MSG extends Message {
	private static final long serialVersionUID = 1L;
	private int tally;	
	
	public INDIVIDUAL_TALLY_MSG(NodeID src, NodeID dest, int tally) {
		super(Message.INDIVIDUAL_TALLY_MSG, src, dest);
		this.tally = tally;
	}
	
	public int getTally() {
		return tally;
	}
	
	@Override
	public void doCopy(Message msg) {
		super.doCopy(msg);
		
		INDIVIDUAL_TALLY_MSG m = (INDIVIDUAL_TALLY_MSG) msg;
		tally = m.tally;
	}

}
