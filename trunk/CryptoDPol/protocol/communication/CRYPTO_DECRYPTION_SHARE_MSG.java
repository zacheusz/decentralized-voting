package protocol.communication;

import OldVoting.DecodingShare;
import protocol.communication.Message;
import runtime.NodeID;

public class CRYPTO_DECRYPTION_SHARE_MSG extends Message {
	private static final long serialVersionUID = 1L;
	private DecodingShare share;
	
	public CRYPTO_DECRYPTION_SHARE_MSG(NodeID src, NodeID dest, DecodingShare share) {
		super(Message.CRYPTO_DECRYPTION_SHARE_MSG, src, dest);
		this.share = share;
	}
	
	public DecodingShare getShare() {
		return share;
	}
	
	@Override
	public void doCopy(Message msg) {
		super.doCopy(msg);
		
		CRYPTO_DECRYPTION_SHARE_MSG m = (CRYPTO_DECRYPTION_SHARE_MSG) msg;
		share = m.share;
	}

}
