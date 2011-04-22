package protocol.communication;


import runtime.executor.E_CryptoNodeID;

public class GMAV_MSG extends Message {
	private static final long serialVersionUID = 1L;
	private int groupId;

	public GMAV_MSG(E_CryptoNodeID src, E_CryptoNodeID dest, int groupId) {
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
