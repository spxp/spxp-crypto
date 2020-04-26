package org.spxp.crypto;

public class SpxpCryptoException extends Exception {

	private static final long serialVersionUID = 7875909038563619188L;

	public SpxpCryptoException() {
	}

	public SpxpCryptoException(String message) {
		super(message);
	}

	public SpxpCryptoException(Throwable cause) {
		super(cause);
	}

	public SpxpCryptoException(String message, Throwable cause) {
		super(message, cause);
	}

	public SpxpCryptoException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
