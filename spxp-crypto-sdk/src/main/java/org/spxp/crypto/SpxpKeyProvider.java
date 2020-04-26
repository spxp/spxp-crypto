package org.spxp.crypto;

import javax.crypto.SecretKey;

public interface SpxpKeyProvider {
	
	public SecretKey getKey(String keyId) throws SpxpCryptoNoSuchKeyException;
	
}