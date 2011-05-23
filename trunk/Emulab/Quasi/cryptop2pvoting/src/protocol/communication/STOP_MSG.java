package protocol.communication;

import runtime.executor.E_CryptoNodeID;

public class STOP_MSG extends Message {

	private static final long serialVersionUID = 1L;
	private String message;
	
	public STOP_MSG(E_CryptoNodeID src, E_CryptoNodeID dest) {
		super(Message.STOP, src, dest);
		this.message = new String();
	}
	
	public STOP_MSG(E_CryptoNodeID src, E_CryptoNodeID dest, String message) {
		super(Message.STOP, src, dest);
		this.message = message;
	}	
	
	public String getMessage() {
		return message;
	}
}

