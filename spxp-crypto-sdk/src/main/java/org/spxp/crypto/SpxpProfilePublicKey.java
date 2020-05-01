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

}
