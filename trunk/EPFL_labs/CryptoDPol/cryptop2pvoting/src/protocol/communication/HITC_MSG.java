//package protocol.communication;
//
//import java.util.LinkedList;
//import java.util.List;
//
//import protocol.communication.Message;
//import runtime.NodeID;
//
//public class HITC_MSG extends Message {
//	private static final long serialVersionUID = 1L;
//	private int viewSize;
//	private int groupId;
//
//	public HITC_MSG(NodeID src, NodeID dest, int viewSize, int groupId) {
//		super(Message.HITC, src, dest);
//		this.viewSize = viewSize;
//		this.groupId = groupId;
//	}
//
//	public int getViewSize() {
//		return viewSize;
//	}
//
//	public int getGroupId() {
//		return groupId;
//	}
//
//
//
//	@Override
//	public void doCopy(Message msg) {
//		super.doCopy(msg);
//
//		HITC_MSG m = (HITC_MSG) msg;
//		viewSize = m.viewSize;
//		groupId = m.groupId;
//	}
//}
