package org.spxp.crypto;

public class SpxpProfilePublicKey {
    
    private String keyId;
    
    private byte[] publicKey;

    public SpxpProfilePublicKey(String keyId, byte[] publicKey) {
        if(keyId == null || publicKey == null || publicKey.length != 32) {
            throw new IllegalArgumentException("Invalid keys");
        }
        this.keyId = keyId;
        this.publicKey = publicKey;
    }
    
    public String getKeyId() {
        return keyId;
    }
    
    public byte[] getPublicKey() {
        return publicKey;
    }
    
    public boolean equals(Object other) {
        if(!(other instanceof SpxpProfilePublicKey)) {
            return false;
        }
        SpxpProfilePublicKey otherPublicKey = (SpxpProfilePublicKey) other;
        return this.keyId.equals(otherPublicKey.getKeyId()) && this.publicKey.equals(otherPublicKey.getPublicKey());
    }

}
