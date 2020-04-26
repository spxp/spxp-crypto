package org.spxp.crypto;

public class SpxpSymmetricKeySpec {
	
	private final String keyId;
	
	private final byte[] symmetricKey;

	public SpxpSymmetricKeySpec(String keyId, byte[] symmetricKey) {
		super();
		this.keyId = keyId;
		this.symmetricKey = symmetricKey;
	}

	public String getKeyId() {
		return keyId;
	}

	public byte[] getSymmetricKey() {
		return symmetricKey;
	}

}
