package protocol.communication;

import runtime.NodeID;

public class GMAV_MSG extends Message {
	private static final long serialVersionUID = 1L;
	private int groupId;

	public GMAV_MSG(NodeID src, NodeID dest, int groupId) {
		super(Message.GMAV, src, dest);
		this.groupId = groupId;
	}

	public int getGroupId() {
		return groupId;
	}
	
	
	@Override
	public void doCopy(Message msg) {
		super.doCopy(msg);
		
		GMAV_MSG m = (GMAV_MSG) msg;
		groupId = m.groupId;
	}
}
