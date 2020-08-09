package org.spxp.crypto;

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
	
	public boolean equals(Object other) {
		if(!(other instanceof SpxpConnectPublicKey)) {
			return false;
		}
		SpxpConnectPublicKey otherPublicKey = (SpxpConnectPublicKey) other;
		return this.keyId.equals(otherPublicKey.getKeyId()) && this.publicKey.equals(otherPublicKey.getPublicKey());
	}

}
