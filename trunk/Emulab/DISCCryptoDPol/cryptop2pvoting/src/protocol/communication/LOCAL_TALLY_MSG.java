package protocol.communication;

import protocol.communication.Message;
import runtime.executor.E_CryptoNodeID;

public class LOCAL_TALLY_MSG extends Message {
	private static final long serialVersionUID = 1L;
	private int tally;
	private int groupId;
	
	public LOCAL_TALLY_MSG(E_CryptoNodeID src, E_CryptoNodeID dest, int tally, int groupId) {
		super(Message.LOCAL_TALLY_MSG, src, dest);
		this.tally = tally;
		this.groupId = groupId;
	}
	
	public int getTally() {
		return tally;
	}

	public int getGroupId() {
		return groupId;
	}
	
	@Override
	public void doCopy(Message msg) {
		super.doCopy(msg);
		
		LOCAL_TALLY_MSG m = (LOCAL_TALLY_MSG) msg;
		tally = m.tally;
		groupId = m.groupId;
	}
}
