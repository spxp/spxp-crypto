package org.spxp.crypto;

import java.util.ArrayList;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.junit.Assert;
import org.junit.Test;

public class SpxpCryptoToolsV02V03CompatibilityTest {

	@Test
	public void encryptSymmetricCompactV02decryptV03() throws Exception {
		final byte[] key = SpxpCryptoToolsV02.generateSymmetricKey(256);
		String messageIn = "The quick brown fox jumps over the lazy dog";
		final String keyIdIn = "test";
		String compact = SpxpCryptoToolsV02.encryptSymmetricCompact(messageIn, keyIdIn, key);
		Assert.assertNotNull(compact);
		String messageOut = SpxpCryptoToolsV03.decryptSymmetricCompact(compact, new SpxpKeyProvider() {
			@Override
			public SecretKey getKey(String keyId) throws SpxpCryptoNoSuchKeyException {
				Assert.assertEquals(keyIdIn, keyId);
				return new SecretKeySpec(key, "AES");
			}
		});
		Assert.assertEquals(messageIn, messageOut);
	}

	@Test
	public void encryptSymmetricCompactV03decryptV02() throws Exception {
		final byte[] key = SpxpCryptoToolsV03.generateSymmetricKey(256);
		String messageIn = "The quick brown fox jumps over the lazy dog";
		final String keyIdIn = "test";
		String compact = SpxpCryptoToolsV03.encryptSymmetricCompact(messageIn, new SpxpSymmetricKeySpec(keyIdIn, key));
		Assert.assertNotNull(compact);
		String messageOut = SpxpCryptoToolsV02.decryptSymmetricCompact(compact, new SpxpKeyProvider() {
			@Override
			public SecretKey getKey(String keyId) throws SpxpCryptoNoSuchKeyException {
				Assert.assertEquals(keyIdIn, keyId);
				return new SecretKeySpec(key, "AES");
			}
		});
		Assert.assertEquals(messageIn, messageOut);
	}

	@Test
	public void encryptSymmetricJsonV02decryptV03() throws Exception {
		final byte[] key1 = SpxpCryptoToolsV02.generateSymmetricKey(256);
		final byte[] key2 = SpxpCryptoToolsV02.generateSymmetricKey(256);
		String messageIn = "The quick brown fox jumps over the lazy dog";
		final String keyId1 = "test1";
		final String keyId2 = "test2";
		String extraAAD = "extra-aad";
		List<SpxpSymmetricKeySpec> recipientKeys = new ArrayList<>();
		recipientKeys.add(new SpxpSymmetricKeySpec(keyId1, key1));
		recipientKeys.add(new SpxpSymmetricKeySpec(keyId2, key2));
		String json = SpxpCryptoToolsV02.encryptSymmetricJson(messageIn, extraAAD, recipientKeys);
		Assert.assertNotNull(json);
		String messageOut = SpxpCryptoToolsV03.decryptSymmetricJson(json, new SpxpKeyProvider() {
			@Override
			public SecretKey getKey(String keyId) throws SpxpCryptoNoSuchKeyException {
				if(keyId.equals(keyId1)) {
					return new SecretKeySpec(key1, "AES");
				} else if(keyId.equals(keyId2)) {
					return new SecretKeySpec(key2, "AES");
				}
				throw new SpxpCryptoNoSuchKeyException();
			}
		});
		Assert.assertEquals(messageIn, messageOut);
	}

	@Test
	public void encryptSymmetricJsonV03decryptV02() throws Exception {
		final byte[] key1 = SpxpCryptoToolsV03.generateSymmetricKey(256);
		final byte[] key2 = SpxpCryptoToolsV03.generateSymmetricKey(256);
		String messageIn = "The quick brown fox jumps over the lazy dog";
		final String keyId1 = "test1";
		final String keyId2 = "test2";
		List<SpxpSymmetricKeySpec> recipientKeys = new ArrayList<>();
		recipientKeys.add(new SpxpSymmetricKeySpec(keyId1, key1));
		recipientKeys.add(new SpxpSymmetricKeySpec(keyId2, key2));
		String json = SpxpCryptoToolsV03.encryptSymmetricJson(messageIn, recipientKeys);
		Assert.assertNotNull(json);
		String messageOut = SpxpCryptoToolsV02.decryptSymmetricJson(json, new SpxpKeyProvider() {
			@Override
			public SecretKey getKey(String keyId) throws SpxpCryptoNoSuchKeyException {
				if(keyId.equals(keyId1)) {
					return new SecretKeySpec(key1, "AES");
				} else if(keyId.equals(keyId2)) {
					return new SecretKeySpec(key2, "AES");
				}
				throw new SpxpCryptoNoSuchKeyException();
			}
		});
		Assert.assertEquals(messageIn, messageOut);
	}
	
}
