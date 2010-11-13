package OldVoting;

import java.math.*;

public class Vote {

    public BigInteger vote;

    public BigInteger[] a;
    public byte[] challenge;
    public BigInteger[] z;
    public BigInteger[] e;

    public Vote (BigInteger _vote, BigInteger[] _a, byte[] _challenge,
		 BigInteger[] _z, BigInteger[] _e) {
	vote = _vote;
	a = _a;
	challenge = _challenge;
	z = _z;
	e = _e;
    }

    public String toString () {
	String str = new String ();
	

        return str;
    }


}
