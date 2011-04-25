package protocol.communication;

import java.io.Serializable;

import runtime.executor.E_CryptoNodeID;

public abstract class Message implements Serializable, Cloneable {
	private static final long serialVersionUID = 1L;
	public final static byte STOP = 0;
	public final static byte IAM = 1; 	// I Announce My Self
	public final static byte GMAV = 2; 	// Give Me A View
	public final static byte HITV = 3; 	// Here Is The View
	public static final byte BALLOT = 4; // A boolean ballot
	public static final byte LOCAL_TALLY_MSG = 5;
	public static final byte INDIVIDUAL_TALLY_MSG = 6;
        public static final byte CRYPTO_BALLOT = 7; // A crypto ballot
	public static final byte CRYPTO_PARTIAL_TALLY_MSG = 8;
	public static final byte CRYPTO_INDIVIDUAL_TALLY_MSG = 9;
	public static final byte CRYPTO_DECRYPTION_SHARE_MSG = 10;
        public static final byte DEAD = 11;
        public static final byte CLUSTER_ASSIGN_MSG = 12;
        public static final byte CRYPTO_FINAL_RESULT_MSG=13;
        public static final byte FINAL_CLUSTER_ASSIGN_MSG = 14;
        public static final byte POSITION_ASSIGN_MSG = 15;

//	public static final byte HITC = 11;

	protected byte header;
	protected E_CryptoNodeID src;
	protected E_CryptoNodeID dest;
	protected long timestamp;

	public Message(byte header, E_CryptoNodeID src, E_CryptoNodeID dest) {
		this.header = header;
		this.src = src;
		this.dest = dest;
		this.timestamp = System.currentTimeMillis();
	}

	public byte getHeader() {
		return header;
	}

	public E_CryptoNodeID getDest() {
		return dest;
	}

	public E_CryptoNodeID getSrc() {
		return src;
	}

	public long getTimeStamp() {
		return timestamp;
	}

	public void doCopy(Message msg) {
		this.header = msg.header;
		this.src = msg.src;
		this.dest = msg.dest;
		this.timestamp = msg.timestamp;
	}

	public Message copy() {
		Message toRet = null;
		try {
			toRet = (Message) clone();
			toRet.doCopy(this);
		} catch (CloneNotSupportedException e) {
			// Cannot happen
			e.printStackTrace();
			System.exit(1);
		}
		return toRet;
	}

	@Override
	public boolean equals(Object msg) {
		if (!(msg instanceof Message)) {
			return false;
		} else {
			Message theMsg = (Message) msg;
			return this.header == theMsg.header && this.src.equals(theMsg.src)
					&& this.dest.equals(theMsg.dest)
					&& this.timestamp == theMsg.timestamp;
		}
	}



}
