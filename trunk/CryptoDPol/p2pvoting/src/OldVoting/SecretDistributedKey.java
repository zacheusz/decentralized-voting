package OldVoting;

import java.math.*;
//import SecretKey;

public class SecretDistributedKey {

    public SecretKey[] keys;

    public SecretDistributedKey (int i) {
	int j;
	
	if (i < 2) return;
	keys = new SecretKey[i];
	for (j = 0 ; j < i; j++)
	    keys[j] = new SecretKey();
    }

    public SecretDistributedKey (SecretKey[] _keys) {
	keys = _keys;
    }

    public String toString () {
	String str = new String ();
	int i;

	str = str + "Secret Distributed Key:\n  shares = ";
	str = str + keys.length;
	for (i = 0; i < keys.length; i++) {
	    str = str + "\n  key (";
	    str = str + (i+1);
	    str = str + ") = ";
	    str = str + keys[i].key.toString ();
	}
	str = str + "\n\n";

        return str;
    }


}
