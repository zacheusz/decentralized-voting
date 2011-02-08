package protocol.communication;

import runtime.executor.E_CryptoNodeID;

public class DEAD_MSG extends Message {

	private static final long serialVersionUID = 1L;
	private String message;
	
	public DEAD_MSG(E_CryptoNodeID src, E_CryptoNodeID dest) {
		super(Message.DEAD, src, dest);
		this.message = new String();
	}
	
	public DEAD_MSG(E_CryptoNodeID src, E_CryptoNodeID dest, String message) {
		super(Message.DEAD, src, dest);
		this.message = message;
	}	
	
	public String getMessage() {
		return message;
	}
}

