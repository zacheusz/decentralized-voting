package protocol.communication;

import java.io.Serializable;

import runtime.NodeID;

public abstract class Message implements Serializable, Cloneable {
	private static final long serialVersionUID = 1L;
	public final static byte STOP = 0;
	public final static byte IAM = 1; 	// I Announce My Self
	public final static byte GMAV = 2; 	// Give Me A View
	public final static byte HITV = 3; 	// Here Is The View
	public static final byte BALLOT = 4; // A boolean ballot
	public static final byte LOCAL_TALLY_MSG = 5;
	public static final byte INDIVIDUAL_TALLY_MSG = 6;
        public static final byte DEAD = 7;
	public static final byte HITC = 8;

	protected byte header;
	protected NodeID src;
	protected NodeID dest;
	protected long timestamp;

	public Message(byte header, NodeID src, NodeID dest) {
		this.header = header;
		this.src = src;
		this.dest = dest;
		this.timestamp = System.currentTimeMillis();
	}

	public byte getHeader() {
		return header;
	}

	public NodeID getDest() {
		return dest;
	}

	public NodeID getSrc() {
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
