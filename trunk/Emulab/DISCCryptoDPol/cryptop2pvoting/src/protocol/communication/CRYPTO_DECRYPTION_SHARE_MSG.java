package protocol.communication;

//import OldVoting.DecodingShare;
import paillierp.PartialDecryption;
import protocol.communication.Message;
import runtime.executor.E_CryptoNodeID;
import zkp.DecryptionZKP;

public class CRYPTO_DECRYPTION_SHARE_MSG extends Message {
	private static final long serialVersionUID = 1L;
	private PartialDecryption share;
        private int shareOrder;
	
	public CRYPTO_DECRYPTION_SHARE_MSG(E_CryptoNodeID src, E_CryptoNodeID dest, PartialDecryption share) {
		super(Message.CRYPTO_DECRYPTION_SHARE_MSG, src, dest);
		this.share = share;
                this.shareOrder=shareOrder;
	}
	
	public PartialDecryption getShare() {
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
