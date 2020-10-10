package org.spxp.crypto;

import java.util.Arrays;

public class SpxpConnectKeyPair {
    
    private String keyId;
    
    private byte[] secretKey;
    
    private byte[] publicKey;

    public SpxpConnectKeyPair(String keyId, byte[] secretKey, byte[] publicKey) {
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
    
    public SpxpConnectPublicKey extractConnectPublicKey() {
        return new SpxpConnectPublicKey(keyId, publicKey);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((keyId == null) ? 0 : keyId.hashCode());
        result = prime * result + Arrays.hashCode(publicKey);
        result = prime * result + Arrays.hashCode(secretKey);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SpxpConnectKeyPair other = (SpxpConnectKeyPair) obj;
        if (keyId == null) {
            if (other.keyId != null)
                return false;
        } else if (!keyId.equals(other.keyId))
            return false;
        if (!Arrays.equals(publicKey, other.publicKey))
            return false;
        if (!Arrays.equals(secretKey, other.secretKey))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "SpxpConnectKeyPair [keyId=" + keyId + ", secretKey=" + Arrays.toString(secretKey) + ", publicKey=" + Arrays.toString(publicKey) + "]";
    }

}
