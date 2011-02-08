package protocol.communication;

import protocol.communication.Message;

import runtime.executor.E_CryptoNodeID;

public class IAM_MSG extends Message {
	private static final long serialVersionUID = 1L;
	private int groupID;
	private boolean malicious;
	
	public IAM_MSG(E_CryptoNodeID src, E_CryptoNodeID dest, int groupeID, boolean isMalicious) {
		super(Message.IAM, src, dest);
		this.groupID = groupeID;
		this.malicious = isMalicious;
	}
	
	public int getGroupID() {
		return groupID;
	}
	
	public boolean isMalicious() {
		return malicious;
	}
	
	@Override
	public void doCopy(Message msg) {
		super.doCopy(msg);
		
		IAM_MSG m = (IAM_MSG) msg;
		groupID = m.groupID;
	}

}
