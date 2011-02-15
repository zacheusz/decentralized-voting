package protocol.communication;

//import OldVoting.DecodingShare;
import protocol.communication.Message;
import runtime.executor.E_CryptoNodeID;
import zkp.DecryptionZKP;

public class CRYPTO_DECRYPTION_SHARE_MSG extends Message {
	private static final long serialVersionUID = 1L;
	private DecryptionZKP share;
        private int shareOrder;
	
	public CRYPTO_DECRYPTION_SHARE_MSG(E_CryptoNodeID src, E_CryptoNodeID dest, DecryptionZKP share) {
		super(Message.CRYPTO_DECRYPTION_SHARE_MSG, src, dest);
		this.share = share;
                this.shareOrder=shareOrder;
	}
	
	public DecryptionZKP getShare() {
		return share;
	}
	public int getShareOrder() {
		return shareOrder;
	}
	@Override
	public void doCopy(Message msg) {
		super.doCopy(msg);
		
		CRYPTO_DECRYPTION_SHARE_MSG m = (CRYPTO_DECRYPTION_SHARE_MSG) msg;
		share = m.share;
	}

}
