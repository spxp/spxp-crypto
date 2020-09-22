package org.spxp.crypto;

public class SpxpProfileKeyPair {
    
    private String keyId;
    
    private byte[] secretKey;
    
    private byte[] publicKey;

    public SpxpProfileKeyPair(String keyId, byte[] secretKey, byte[] publicKey) {
        if(keyId == null || secretKey == null || publicKey == null || secretKey.length != 32 || publicKey.length != 32) {
            throw new IllegalArgumentException("Invalid keys");
        }
        this.keyId = keyId;
        this.secretKey = secretKey;
        this.publicKey = publicKey;
    }
    
    public String getKeyId() {
        return keyId;
    }
    
    public byte[] getSecretKey() {
        return secretKey;
    }
    
    public byte[] getPublicKey() {
        return publicKey;
    }
    
    public boolean equals(Object other) {
        if(!(other instanceof SpxpProfileKeyPair)) {
            return false;
        }
        SpxpProfileKeyPair otherProfileKeyPair = (SpxpProfileKeyPair) other;
        return this.keyId.equals(otherProfileKeyPair.getKeyId()) && this.publicKey.equals(otherProfileKeyPair.getPublicKey()) && this.secretKey.equals(otherProfileKeyPair.getSecretKey());
    }

}
