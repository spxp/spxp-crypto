package org.spxp.crypto;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.cxf.common.util.Base64UrlUtility;
import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.rs.security.jose.common.JoseException;
import org.apache.cxf.rs.security.jose.jwa.ContentAlgorithm;
import org.apache.cxf.rs.security.jose.jwa.KeyAlgorithm;
import org.apache.cxf.rs.security.jose.jwe.AesGcmContentEncryptionAlgorithm;
import org.apache.cxf.rs.security.jose.jwe.ContentEncryptionProvider;
import org.apache.cxf.rs.security.jose.jwe.DirectKeyDecryptionAlgorithm;
import org.apache.cxf.rs.security.jose.jwe.DirectKeyJweEncryption;
import org.apache.cxf.rs.security.jose.jwe.JweCompactConsumer;
import org.apache.cxf.rs.security.jose.jwe.JweCompactProducer;
import org.apache.cxf.rs.security.jose.jwe.JweDecryptionProvider;
import org.apache.cxf.rs.security.jose.jwe.JweEncryption;
import org.apache.cxf.rs.security.jose.jwe.JweEncryptionProvider;
import org.apache.cxf.rs.security.jose.jwe.JweHeaders;
import org.apache.cxf.rs.security.jose.jwe.JweJsonConsumer;
import org.apache.cxf.rs.security.jose.jwe.JweJsonEncryptionEntry;
import org.apache.cxf.rs.security.jose.jwe.JweJsonProducer;
import org.apache.cxf.rs.security.jose.jwe.JweUtils;
import org.apache.cxf.rt.security.crypto.CryptoUtils;

public class SpxpCryptoToolsV02 {

    private SpxpCryptoToolsV02() {
        // prevent instantiation
    }

    private static SecureRandom secureRandom = new SecureRandom();

    public static String encryptSymmetricCompact(String payload, String keyId, byte[] key) throws SpxpCryptoException
    {
        try
        {
            KeyAlgorithm keyAlgo = KeyAlgorithm.DIRECT;
            ContentAlgorithm ctAlgo = ContentAlgorithm.A256GCM;
            JweHeaders headers = new JweHeaders();
            headers.setKeyEncryptionAlgorithm(keyAlgo);
            headers.setContentEncryptionAlgorithm(ctAlgo);
            headers.setKeyId(keyId);
            JweCompactProducer p = new JweCompactProducer(headers, payload);
            return p.encryptWith(new DirectKeyJweEncryption(new AesGcmContentEncryptionAlgorithm(key, ctAlgo)));
        }
        catch(JoseException je)
        {
            throw new SpxpCryptoException(je);
        }
    }

    public static String decryptSymmetricCompact(String compact, SpxpKeyProvider keyProvider) throws SpxpCryptoException
    {
        try
        {
            JweCompactConsumer consumer = new JweCompactConsumer(compact);
            ContentAlgorithm ctAlgo = consumer.getJweHeaders().getContentEncryptionAlgorithm();
            String keyId = consumer.getJweHeaders().getKeyId();
            SecretKey secretKey = keyProvider.getKey(keyId);
            // KeyAlgorithm keyAlgo = consumer.getJweHeaders().getKeyEncryptionAlgorithm();
            // JweDecryptionProvider decryptProvider = JweUtils.createJweDecryptionProvider(secretKey, keyAlgo, ctAlgo);
            JweDecryptionProvider decryptProvider = JweUtils.createJweDecryptionProvider(new DirectKeyDecryptionAlgorithm(secretKey), ctAlgo);
            return consumer.getDecryptedContentText(decryptProvider);
        }
        catch(JoseException je)
        {
            throw new SpxpCryptoException(je);
        }
    }

    public static String encryptSymmetricJson(String payload, String extraAAD, List<SpxpSymmetricKeySpec> recipientKeys) throws SpxpCryptoException
    {
        try
        {
            ContentEncryptionProvider contentEncryption = new AesGcmContentEncryptionAlgorithm(ContentAlgorithm.A256GCM, true);
            List<JweEncryptionProvider> jweProviders = new LinkedList<JweEncryptionProvider>();
            List<JweHeaders> perRecipientHeades = new LinkedList<JweHeaders>();
            for(SpxpSymmetricKeySpec recipientKey : recipientKeys)
            {
                jweProviders.add(new JweEncryption(JweUtils.getSecretKeyEncryptionAlgorithm(CryptoUtils.createSecretKeySpec(recipientKey.getSymmetricKey(), "AES"), KeyAlgorithm.A256GCMKW), contentEncryption));
                perRecipientHeades.add(new JweHeaders(recipientKey.getKeyId()));
            }
            JweHeaders sharedUnprotectedHeaders = new JweHeaders();
            sharedUnprotectedHeaders.setKeyEncryptionAlgorithm(KeyAlgorithm.A256GCMKW);
            JweHeaders protectedHeaders = new JweHeaders(ContentAlgorithm.A256GCM);
            JweJsonProducer p = new JweJsonProducer(protectedHeaders,
                    sharedUnprotectedHeaders,
                    StringUtils.toBytesUTF8(payload.toString()),
                    extraAAD == null ? null : StringUtils.toBytesUTF8(extraAAD),
                    false);
            return p.encryptWith(jweProviders, perRecipientHeades);
        }
        catch(JoseException je)
        {
            throw new SpxpCryptoException(je);
        }
    }

    public static String decryptSymmetricJson(String ciphertext, SpxpKeyProvider keyProvider) throws SpxpCryptoException
    {
        try
        {
            JweJsonConsumer consumer = new JweJsonConsumer(ciphertext);
            KeyAlgorithm keyAlgo = consumer.getSharedUnprotectedHeader().getKeyEncryptionAlgorithm();
            ContentAlgorithm ctAlgo = consumer.getProtectedHeader().getContentEncryptionAlgorithm();
            SecretKey secretKey = null;
            String usedKid = null;
            for(JweJsonEncryptionEntry jjee : consumer.getRecipients()) {
                String kid = jjee.getUnprotectedHeader().getKeyId();
                try {
                    secretKey = keyProvider.getKey(kid);
                    usedKid = kid;
                } catch(SpxpCryptoNoSuchKeyException nske) {
                    continue;
                }
            }
            if(secretKey == null) {
                throw new SpxpCryptoNoSuchKeyException();
            }
            JweDecryptionProvider decryptProvider = JweUtils.createJweDecryptionProvider(secretKey, keyAlgo, ctAlgo);
            return consumer.decryptWith(decryptProvider, Collections.singletonMap("kid", (Object) usedKid)).getContentText();
        }
        catch(JoseException je)
        {
            throw new SpxpCryptoException(je);
        }
    }

    public static String encryptWithSharedSecret(String groupKeyJwk, byte[] peerSharedSecret) throws SpxpCryptoException
    {
        try
        {
            byte[] randomKeyIdBytes = new byte[8];
            secureRandom.nextBytes(randomKeyIdBytes);
            String randomKeyId = Base64UrlUtility.encode(randomKeyIdBytes);
            return encryptSymmetricCompact(groupKeyJwk, randomKeyId, calculateDerivedKey(peerSharedSecret, randomKeyId, 256));
        }
        catch(JoseException je)
        {
            throw new SpxpCryptoException(je);
        }
    }

    public static String decryptWithSharedSecret(String compact, byte[] peerSharedSecret) throws SpxpCryptoException
    {
        try
        {
            JweCompactConsumer consumer = new JweCompactConsumer(compact);
            KeyAlgorithm keyAlgo = consumer.getJweHeaders().getKeyEncryptionAlgorithm();
            ContentAlgorithm ctAlgo = consumer.getJweHeaders().getContentEncryptionAlgorithm();
            String keyId = consumer.getJweHeaders().getKeyId();
            SecretKey secretKey = new SecretKeySpec(calculateDerivedKey(peerSharedSecret, keyId, 256), "AES");
            JweDecryptionProvider decryptProvider = JweUtils.createJweDecryptionProvider(secretKey, keyAlgo, ctAlgo);
            return consumer.getDecryptedContentText(decryptProvider);
        }
        catch(JoseException je)
        {
            throw new SpxpCryptoException(je);
        }
    }

    public static byte[] calculateECDHSharedSecret(ECPrivateKey myPrivateKey, ECPublicKey peerPublicKey) throws SpxpCryptoException
    {
        try
        {
            KeyAgreement ka = KeyAgreement.getInstance("ECDH");
            ka.init(myPrivateKey);
            ka.doPhase(peerPublicKey, true);
            return ka.generateSecret();
        }
        catch(NoSuchAlgorithmException | InvalidKeyException e)
        {
            throw new SpxpCryptoException(e);
        }
    }

    public static byte[] calculateDerivedKey(byte[] sharedSecret, String keyId, int outputBitlen) throws SpxpCryptoException
    {
        if(outputBitlen % 8 != 0)
        {
            throw new SpxpCryptoException("key bitlen must be multiple of 8");
        }
        if(outputBitlen > 256)
        {
            throw new SpxpCryptoException("max key bitlen is 256 for now (may be extended to multiple rounds)");
        }
        try
        {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update("SPXP-KDF".getBytes());
            md.update(sharedSecret);
            md.update(keyId.getBytes("UTF-8"));
            byte[] round1Hash = md.digest();
            byte[] result = new byte[outputBitlen/8];
            System.arraycopy(round1Hash, 0, result, 0, result.length);
            return result;
        }
        catch(NoSuchAlgorithmException | UnsupportedEncodingException e)
        {
            throw new SpxpCryptoException(e);
        }
    }

    public static byte[] generateSymmetricKey(int bitlen)
    {
        if(bitlen % 8 != 0)
        {
            throw new IllegalArgumentException("key bitlen must be multiple of 8");
        }
        byte[] result = new byte[bitlen/8];
        secureRandom.nextBytes(result);
        return result;
    }

    public static String generateRandomKeyId(boolean longFormat)
    {
        byte[] result = new byte[longFormat ? 6 : 12];
        secureRandom.nextBytes(result);
        return Base64UrlUtility.encode(result);
    }

}
