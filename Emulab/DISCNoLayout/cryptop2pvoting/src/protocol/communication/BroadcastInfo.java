/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package protocol.communication;

import java.io.Serializable;
import java.math.BigInteger;
import paillierp.PartialDecryption;
import runtime.executor.E_CryptoNodeID;

/**
 *
 * @author Hamza
 */
    public class BroadcastInfo implements Serializable {
	private static final long serialVersionUID = 1L;

        public PartialDecryption share;
        public BigInteger vote;
        public byte type;
        public E_CryptoNodeID actualSrc;
     //   boolean isCorrupt;
        public int seqNum;

        public BroadcastInfo(PartialDecryption share, BigInteger vote, byte type, E_CryptoNodeID actualSrc,  int seqNum) {
            this.share = share;
            this.vote = vote;
            this.type = type;
            this.actualSrc = actualSrc;
           // this.isCorrupt = isCorrupt;
            this.seqNum = seqNum;
        }

        public BroadcastInfo(BroadcastInfo otherInfo) {
            this.share = otherInfo.share;
            this.vote = otherInfo.vote;
            this.type = otherInfo.type;
            this.actualSrc = otherInfo.actualSrc;
      //      this.isCorrupt = otherInfo.isCorrupt;
            this.seqNum = otherInfo.seqNum;

        }
    }
