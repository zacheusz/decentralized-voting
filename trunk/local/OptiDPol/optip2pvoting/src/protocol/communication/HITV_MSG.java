package protocol.communication;

import java.util.LinkedList;
import java.util.List;

import protocol.communication.Message;
import runtime.NodeID;

public class HITV_MSG extends Message {
	private static final long serialVersionUID = 1L;
	private List<NodeID> view;
	private int groupId;
	private boolean knownModulation;

	public HITV_MSG(NodeID src, NodeID dest, List<NodeID> view, int groupId, boolean knownModulation) {
		super(Message.HITV, src, dest);
		this.view = view;
		this.groupId = groupId;
		this.knownModulation = knownModulation; 
	}

	public List<NodeID> getView() {
		return view;
	}
	
	public int getGroupId() {
		return groupId;
	}
	
	public boolean knownModulation() {
		return knownModulation;
	}
	
	@Override
	public void doCopy(Message msg) {
		super.doCopy(msg);
		
		HITV_MSG m = (HITV_MSG) msg;
		view = new LinkedList<NodeID>();
		for (NodeID id : m.view) {
			view.add(id);
		}
		groupId = m.groupId;
	}
}
