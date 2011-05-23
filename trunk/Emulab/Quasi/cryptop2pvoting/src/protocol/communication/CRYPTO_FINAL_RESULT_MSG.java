package protocol.communication;

import protocol.communication.Message;
import runtime.executor.E_CryptoNodeID;
//import OldVoting.Vote;
import java.math.BigInteger;

public class CRYPTO_FINAL_RESULT_MSG extends Message {
	private static final long serialVersionUID = 1L;
	private BigInteger result;
//	private int groupId;
	
	public CRYPTO_FINAL_RESULT_MSG(E_CryptoNodeID src, E_CryptoNodeID dest, BigInteger result) {
		super(Message.CRYPTO_FINAL_RESULT_MSG, src, dest);
		this.result = result;
		//this.groupId = groupId;
	}
	
	public BigInteger getResult() {
		return result;
	}

//	public int getGroupId() {
//		return groupId;
//	}
	
	@Override
	public void doCopy(Message msg) {
		super.doCopy(msg);
		
		CRYPTO_FINAL_RESULT_MSG m = (CRYPTO_FINAL_RESULT_MSG) msg;
		result = m.result;
	//	groupId = m.groupId;
	}
}
