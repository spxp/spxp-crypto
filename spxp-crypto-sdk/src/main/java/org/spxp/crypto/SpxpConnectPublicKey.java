package org.spxp.crypto;

import java.util.Arrays;

public class SpxpConnectPublicKey {
    
    private String keyId;
    
    private byte[] publicKey;

    public SpxpConnectPublicKey(String keyId, byte[] publicKey) {
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((keyId == null) ? 0 : keyId.hashCode());
        result = prime * result + Arrays.hashCode(publicKey);
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
        SpxpConnectPublicKey other = (SpxpConnectPublicKey) obj;
        if (keyId == null) {
            if (other.keyId != null)
                return false;
        } else if (!keyId.equals(other.keyId))
            return false;
        if (!Arrays.equals(publicKey, other.publicKey))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "SpxpConnectPublicKey [keyId=" + keyId + ", publicKey=" + Arrays.toString(publicKey) + "]";
    }

}
