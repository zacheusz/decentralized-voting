package paillierp.zkp;



import java.io.Serializable;
import java.nio.ByteBuffer;
import java.security.DigestException;
import java.security.MessageDigestSpi;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;

public abstract class MessageDigest1 extends MessageDigestSpi implements Serializable{

 /*   static class Delegate extends MessageDigest1 {

        private MessageDigestSpi digestSpi;

        public Delegate(MessageDigestSpi mds, String string) {
            //compiled code
            throw new RuntimeException("Compiled Code");
        }

        public Object clone() throws CloneNotSupportedException {
            //compiled code
            throw new RuntimeException("Compiled Code");
        }

        protected int engineGetDigestLength() {
            //compiled code
            throw new RuntimeException("Compiled Code");
        }

        protected void engineUpdate(byte b) {
            //compiled code
            throw new RuntimeException("Compiled Code");
        }

        protected void engineUpdate(byte[] bytes, int i, int i1) {
            //compiled code
            throw new RuntimeException("Compiled Code");
        }

        protected void engineUpdate(ByteBuffer bb) {
            //compiled code
            throw new RuntimeException("Compiled Code");
        }

        protected byte[] engineDigest() {
            //compiled code
            throw new RuntimeException("Compiled Code");
        }

        protected int engineDigest(byte[] bytes, int i, int i1) throws DigestException {
            //compiled code
            throw new RuntimeException("Compiled Code");
        }

        protected void engineReset() {
            //compiled code
            throw new RuntimeException("Compiled Code");
        }
    }*/
    private String algorithm;
    private static final int INITIAL = 0;
    private static final int IN_PROGRESS = 1;
    private int state;
    private Provider provider;

    protected MessageDigest1(String string) {
        //compiled code
        throw new RuntimeException("Compiled Code");
    }

    public static MessageDigest1 getInstance(String string) throws NoSuchAlgorithmException {
        //compiled code
        throw new RuntimeException("Compiled Code");
    }

    public static MessageDigest1 getInstance(String string, String string1) throws NoSuchAlgorithmException, NoSuchProviderException {
        //compiled code
        throw new RuntimeException("Compiled Code");
    }

    public static MessageDigest1 getInstance(String string, Provider prvdr) throws NoSuchAlgorithmException {
        //compiled code
        throw new RuntimeException("Compiled Code");
    }

    public final Provider getProvider() {
        //compiled code
        throw new RuntimeException("Compiled Code");
    }

    public void update(byte b) {
        //compiled code
        throw new RuntimeException("Compiled Code");
    }

    public void update(byte[] bytes, int i, int i1) {
        //compiled code
        throw new RuntimeException("Compiled Code");
    }

    public void update(byte[] bytes) {
        //compiled code
        throw new RuntimeException("Compiled Code");
    }

    public final void update(ByteBuffer bb) {
        //compiled code
        throw new RuntimeException("Compiled Code");
    }

    public byte[] digest() {
        //compiled code
        throw new RuntimeException("Compiled Code");
    }

    public int digest(byte[] bytes, int i, int i1) throws DigestException {
        //compiled code
        throw new RuntimeException("Compiled Code");
    }

    public byte[] digest(byte[] bytes) {
        //compiled code
        throw new RuntimeException("Compiled Code");
    }

    public String toString() {
        //compiled code
        throw new RuntimeException("Compiled Code");
    }

    public static boolean isEqual(byte[] bytes, byte[] bytes1) {
        //compiled code
        throw new RuntimeException("Compiled Code");
    }

    public void reset() {
        //compiled code
        throw new RuntimeException("Compiled Code");
    }

    public final String getAlgorithm() {
        //compiled code
        throw new RuntimeException("Compiled Code");
    }

    public final int getDigestLength() {
        //compiled code
        throw new RuntimeException("Compiled Code");
    }

    public Object clone() throws CloneNotSupportedException {
        //compiled code
        throw new RuntimeException("Compiled Code");
    }
}
