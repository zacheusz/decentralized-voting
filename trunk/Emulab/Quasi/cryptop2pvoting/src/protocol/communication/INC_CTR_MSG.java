package protocol.communication;

import protocol.communication.Message;
import runtime.executor.E_CryptoNodeID;
//import OldVoting.Vote;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

public class INC_CTR_MSG extends Message {
	private static final long serialVersionUID = 1L;
	//private BigInteger result;
//        protected Set<E_CryptoNodeID> peerView = new HashSet<E_CryptoNodeID>();
//        protected Set<E_CryptoNodeID> proxyView = new HashSet<E_CryptoNodeID>();
//        protected Set<E_CryptoNodeID> clientView = new HashSet<E_CryptoNodeID>();
	
	public INC_CTR_MSG(E_CryptoNodeID src, E_CryptoNodeID dest) {
		super(Message.INC_CTR_MSG, src, dest);
//		this.peerView.addAll(peerView);
//                this.proxyView.addAll(peerView);
//                this.clientView.addAll(peerView);
	}
	
//	public BigInteger getResult() {
//		return result;
//	}

//	public int getGroupId() {
//		return groupId;
//	}
	
	@Override
	public void doCopy(Message msg) {
		super.doCopy(msg);
		
		INC_CTR_MSG m = (INC_CTR_MSG) msg;
	//	result = m.result;
	//	groupId = m.groupId;
	}
}
