package protocol.communication;

import java.util.LinkedList;
import java.util.List;

import protocol.communication.Message;

import runtime.executor.E_CryptoNodeID;

public class HITV_MSG extends Message {
	private static final long serialVersionUID = 1L;
	private List<E_CryptoNodeID> view;
	private int groupId;

	public HITV_MSG(E_CryptoNodeID src, E_CryptoNodeID dest, List<E_CryptoNodeID> view, int groupId) {
		super(Message.HITV, src, dest);
		this.view = view;
		this.groupId = groupId;
	}

	public List<E_CryptoNodeID> getView() {
		return view;
	}
	
	public int getGroupId() {
		return groupId;
	}
	

	@Override
	public void doCopy(Message msg) {
		super.doCopy(msg);
		
		HITV_MSG m = (HITV_MSG) msg;
		view = new LinkedList<E_CryptoNodeID>();
		for (E_CryptoNodeID id : m.view) {
			view.add(id);
		}
		groupId = m.groupId;
	}
}
