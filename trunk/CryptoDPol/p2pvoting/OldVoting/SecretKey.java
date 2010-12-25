package OldVoting;

import java.math.*;

public class SecretKey {

    public BigInteger key;
    public int nr;

    public SecretKey () {}

    public SecretKey (BigInteger _key, int _nr) {
	key = _key;
	nr = _nr;
    }

    public String toString () {
	String str = new String ();
	
	str = str + "Secret Key:\n  key = ";
	str = str + key.toString ();
	str = str + "\n\n";

        return str;
    }


}
