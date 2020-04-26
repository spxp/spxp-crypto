package org.spxp.crypto;

public class SpxpCryptoNoSuchKeyException extends SpxpCryptoException {

	private static final long serialVersionUID = 5033553379782532492L;

	public SpxpCryptoNoSuchKeyException() {
	}

	public SpxpCryptoNoSuchKeyException(String message) {
		super(message);
	}

	public SpxpCryptoNoSuchKeyException(Throwable cause) {
		super(cause);
	}

	public SpxpCryptoNoSuchKeyException(String message, Throwable cause) {
		super(message, cause);
	}

	public SpxpCryptoNoSuchKeyException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
