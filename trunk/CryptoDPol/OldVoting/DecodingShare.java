package OldVoting;

import java.math.*;

public class DecodingShare {

    public BigInteger share;
    public BigInteger vrandom;
    public BigInteger mrandom;
    public BigInteger proof;

    public int tallynr;

    public DecodingShare (BigInteger _share, BigInteger _vrandom,
			  BigInteger _mrandom, BigInteger _proof, int _tallynr) {
 	share = _share;
	vrandom = _vrandom;
	mrandom = _mrandom;
	proof = _proof;
	tallynr = _tallynr;
    }

    public String toString () {
	String str = new String ();
	
	str = str + "Decoding Share (" + tallynr + "):\n  share   = " +
	            share.toString ();
	str = str + "\n  vrandom = " + vrandom.toString ();
	str = str + "\n  mrandom = " + mrandom.toString ();
	str = str + "\n  proof   = " + proof.toString ();
	str = str + "\n\n";

        return str;
    }


}
