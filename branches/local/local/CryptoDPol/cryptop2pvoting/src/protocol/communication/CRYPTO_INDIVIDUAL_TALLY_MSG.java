package protocol.communication;

import protocol.communication.Message;
import runtime.executor.E_CryptoNodeID;
import java.math.BigInteger;

public class CRYPTO_INDIVIDUAL_TALLY_MSG extends Message {
	private static final long serialVersionUID = 1L;
	private BigInteger tally;
	
	public CRYPTO_INDIVIDUAL_TALLY_MSG(E_CryptoNodeID src, E_CryptoNodeID dest, BigInteger tally) {
		super(Message.CRYPTO_INDIVIDUAL_TALLY_MSG, src, dest);
		this.tally = tally;
	}
	
	public BigInteger getTally() {
		return tally;
	}
	
	@Override
	public void doCopy(Message msg) {
		super.doCopy(msg);
		
		CRYPTO_INDIVIDUAL_TALLY_MSG m = (CRYPTO_INDIVIDUAL_TALLY_MSG) msg;
		tally = m.tally;
	}

}
