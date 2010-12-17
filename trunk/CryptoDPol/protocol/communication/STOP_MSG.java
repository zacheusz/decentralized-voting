package protocol.communication;

import runtime.NodeID;

public class STOP_MSG extends Message {

	private static final long serialVersionUID = 1L;
	private String message;
	
	public STOP_MSG(NodeID src, NodeID dest) {
		super(Message.STOP, src, dest);
		this.message = new String();
	}
	
	public STOP_MSG(NodeID src, NodeID dest, String message) {
		super(Message.STOP, src, dest);
		this.message = message;
	}	
	
	public String getMessage() {
		return message;
	}
}

