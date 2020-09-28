package org.spxp.crypto;

import java.util.Arrays;

public class SpxpSymmetricKeySpec {
    
    private final String keyId;
    
    private final byte[] symmetricKey;

    public SpxpSymmetricKeySpec(String keyId, byte[] symmetricKey) {
        if(symmetricKey.length != 256 / 8) {
            throw new IllegalArgumentException("Invalid key size. Expected 256bit");
        }
        this.keyId = keyId;
        this.symmetricKey = symmetricKey;
    }

    public String getKeyId() {
        return keyId;
    }

    public byte[] getSymmetricKey() {
        return symmetricKey;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((keyId == null) ? 0 : keyId.hashCode());
        result = prime * result + Arrays.hashCode(symmetricKey);
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
        SpxpSymmetricKeySpec other = (SpxpSymmetricKeySpec) obj;
        if (keyId == null) {
            if (other.keyId != null)
                return false;
        } else if (!keyId.equals(other.keyId))
            return false;
        if (!Arrays.equals(symmetricKey, other.symmetricKey))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "SpxpSymmetricKeySpec [keyId=" + keyId + ", symmetricKey=" + Arrays.toString(symmetricKey) + "]";
    }

}
