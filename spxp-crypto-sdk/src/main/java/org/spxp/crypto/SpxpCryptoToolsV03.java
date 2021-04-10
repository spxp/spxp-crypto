package org.spxp.crypto;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONString;

public class SpxpCryptoToolsV03 {

    private static final int A256GCM_KEY_SIZE = 256;

    private static final int A256GCM_IV_SIZE = 96;

    private static final int A256GCM_AUTH_TAG_LENGTH = 128;

    private static final String A256GCM_JCE_ALGO_SPEC = "AES/GCM/NoPadding";

    private static final String AES_JCE_KEY_SPEC = "AES";

    private static SecureRandom secureRandom = new SecureRandom();

    private static Encoder urlEncoder = Base64.getUrlEncoder().withoutPadding();

    private static Decoder urlDecoder = Base64.getUrlDecoder();

    public static HashSet<String> OMIT_MEMBERS_SIGN = new HashSet<String>(Arrays.asList(new String[] {"private", "seqts"}));

    public static HashSet<String> OMIT_MEMBERS_VERIFY = new HashSet<String>(Arrays.asList(new String[] {"private", "seqts", "signature"}));

    private SpxpCryptoToolsV03() {
        // prevent instantiation
    }

    public static String encodeBase64Url(byte[] data) {
        return urlEncoder.encodeToString(data);
    }

    public static byte[] decodeBase64Url(String data) {
        return urlDecoder.decode(data);
    }

    public static String encryptSymmetricCompact(String payload, SpxpSymmetricKeySpec key) throws SpxpCryptoException
    {
        try
        {
            // generate header
            JSONObject header = new JSONObject();
            header.put("alg", "dir");
            header.put("enc", "A256GCM");
            header.put("kid", key.getKeyId());
            String headersJson = header.toString();
            // create random IV
            byte[] iv = new byte[A256GCM_IV_SIZE / 8];
            secureRandom.nextBytes(iv);
            // algo spec
            AlgorithmParameterSpec algoSpec = new GCMParameterSpec(A256GCM_AUTH_TAG_LENGTH, iv);
            // calculate additional authentication data
            byte[] aad  = calculateAAD(headersJson, null);
            // secret key
            SecretKey secretKey = new SecretKeySpec(key.getSymmetricKey(), AES_JCE_KEY_SPEC);
            // init Cipher
            int mode = Cipher.ENCRYPT_MODE;
            Cipher c = Cipher.getInstance(A256GCM_JCE_ALGO_SPEC);
            c.init(mode, secretKey, algoSpec);
            c.updateAAD(aad);
            // encrypt
            byte[] encryptedContent = c.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            // split result in cipher and authTag
            byte[] cipher = Arrays.copyOf(encryptedContent, encryptedContent.length - A256GCM_AUTH_TAG_LENGTH / 8);
            byte[] authTag = Arrays.copyOfRange(encryptedContent, encryptedContent.length - A256GCM_AUTH_TAG_LENGTH / 8, encryptedContent.length);
            // create compact JWE result
            String encodedHeaders = encodeBase64Url(headersJson.getBytes(StandardCharsets.UTF_8));
            String encodedContentEncryptionKey = "";
            String encodedInitVector = encodeBase64Url(iv);
            String encodedEncryptedContent = encodeBase64Url(cipher);
            String encodedAuthTag = encodeBase64Url(authTag);
            StringBuilder sb = new StringBuilder();
            sb.append(encodedHeaders)
              .append('.')
              .append(encodedContentEncryptionKey == null ? "" : encodedContentEncryptionKey)
              .append('.')
              .append(encodedInitVector == null ? "" : encodedInitVector)
              .append('.')
              .append(encodedEncryptedContent)
              .append('.')
              .append(encodedAuthTag);
            return sb.toString();
        }
        catch(JSONException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException e)
        {
            throw new SpxpCryptoException(e);
        }
    }
 
    private static byte[] calculateAAD(String headersJson, byte[] customAAD) {
        byte[] headerBytes = headersJson.getBytes(StandardCharsets.UTF_8);
        String base64UrlHeadersInJson = encodeBase64Url(headerBytes);
        byte[] headersAAD = base64UrlHeadersInJson.getBytes(StandardCharsets.US_ASCII);
        if(customAAD == null || customAAD.length == 0) {
            return headersAAD;
        }
        byte[] newAAD = Arrays.copyOf(headersAAD, headersAAD.length + 1 + customAAD.length);
        newAAD[headersAAD.length] = '.';
        System.arraycopy(customAAD, 0, newAAD, headersAAD.length + 1, customAAD.length);
        return newAAD;
    }

    public static String decryptSymmetricCompact(String compact, SpxpKeyProvider keyProvider) throws SpxpCryptoException
    {
        try
        {
            // decode compact representation
            String[] parts = compact.split("\\.",-1);
            if(parts.length != 5) {
                throw new SpxpCryptoException("Invalid compact representation");
            }
            String headersJson = new String(decodeBase64Url(parts[0]), StandardCharsets.UTF_8);
            byte[] encryptedCEK = decodeBase64Url(parts[1]);
            byte[] iv = decodeBase64Url(parts[2]);
            byte[] cipher = decodeBase64Url(parts[3]);
            byte[] authTag = decodeBase64Url(parts[4]);
            // read header
            JSONObject header = new JSONObject(headersJson);
            String algHeader = header.getString("alg");
            String encHeader = header.getString("enc");
            String aadHeader = header.optString("aad", null);
            String kidHeader = header.getString("kid");
            // check SPXP algorithm specs
            if(!algHeader.equals("dir") || !encHeader.equals("A256GCM") ) {
                throw new SpxpCryptoException("Unsupported algortithm or encoding");
            }
            if(encryptedCEK != null && encryptedCEK.length > 0) {
                throw new SpxpCryptoException("Unexpected encrypted CEK");
            }
            if(aadHeader != null) {
                throw new SpxpCryptoException("Unexpected AAD");
            }
            if(iv == null || iv.length != A256GCM_IV_SIZE/8) {
                throw new SpxpCryptoException("Missing IV or invalid IV size");
            }
            if(authTag == null || authTag.length != A256GCM_AUTH_TAG_LENGTH/8) {
                throw new SpxpCryptoException("Missing auth tag or invalid auth tag size");
            }
            // get SecretKey
            SecretKey secretKey = keyProvider.getKey(kidHeader);
            if(secretKey == null) {
                throw new SpxpCryptoNoSuchKeyException();
            }
            // calculate additional authentication data
            byte[] aad = calculateAAD(headersJson, null);
            // algo spec
            AlgorithmParameterSpec algoSpec = new GCMParameterSpec(A256GCM_AUTH_TAG_LENGTH, iv);
            // reconstruct encrypted content as used by java
            byte[] encryptedContentWithTag = new byte[cipher.length + authTag.length];
            System.arraycopy(cipher, 0, encryptedContentWithTag, 0, cipher.length);
            System.arraycopy(authTag, 0, encryptedContentWithTag, cipher.length, authTag.length);
            // decrypt
            int mode = Cipher.DECRYPT_MODE;
            Cipher c = Cipher.getInstance(A256GCM_JCE_ALGO_SPEC);
            c.init(mode, secretKey, algoSpec);
            c.updateAAD(aad);
            byte[] decrypted = c.doFinal(encryptedContentWithTag);
            // return as String
            return new String(decrypted, StandardCharsets.UTF_8);
        }
        catch(IllegalArgumentException | JSONException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e)
        {
            throw new SpxpCryptoException(e);
        }
    }

    public static String encryptSymmetricJson(String payload, List<SpxpSymmetricKeySpec> recipientKeys) throws SpxpCryptoException
    {
        try
        {
            // generate CEK
            KeyGenerator keyGen = KeyGenerator.getInstance(AES_JCE_KEY_SPEC);
            keyGen.init(A256GCM_KEY_SIZE);  // keyGen.init(A256GCM_KEY_SIZE, secureRandom)
            SecretKey cek = keyGen.generateKey();
            // create random IV
            byte[] iv = new byte[A256GCM_IV_SIZE / 8];
            secureRandom.nextBytes(iv);
            // algo spec
            AlgorithmParameterSpec algoSpec = new GCMParameterSpec(A256GCM_AUTH_TAG_LENGTH, iv);
            // protected headers
            String protectedHeadersJson = "{\"enc\":\"A256GCM\"}";
            // calculate additional authentication data
            byte[] aad  = calculateAAD(protectedHeadersJson, null);
            // init Cipher
            int mode = Cipher.ENCRYPT_MODE;
            Cipher c = Cipher.getInstance(A256GCM_JCE_ALGO_SPEC);
            c.init(mode, cek, algoSpec);
            c.updateAAD(aad);
            // encrypt
            byte[] encryptedContent = c.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            byte[] cipher = Arrays.copyOf(encryptedContent, encryptedContent.length - A256GCM_AUTH_TAG_LENGTH / 8);
            byte[] authTag = Arrays.copyOfRange(encryptedContent, encryptedContent.length - A256GCM_AUTH_TAG_LENGTH / 8, encryptedContent.length);
            // prepare JSON JWE result
            JSONObject result = new JSONObject();
            result.putOnce("ciphertext", encodeBase64Url(cipher));
            result.putOnce("protected", encodeBase64Url(protectedHeadersJson.getBytes(StandardCharsets.UTF_8)));
            //result.putOnce("aad", encodeBase64Url(extraAAD.getBytes(StandardCharsets.UTF_8)));
            JSONObject unprotectedHeader = new JSONObject();
            unprotectedHeader.putOnce("alg", "A256GCMKW");
            result.putOnce("unprotected", unprotectedHeader);
            result.putOnce("tag", encodeBase64Url(authTag));
            result.putOnce("iv", encodeBase64Url(iv));
            JSONArray recipients = new JSONArray();
            for(SpxpSymmetricKeySpec recipientKeySpec : recipientKeys)
            {
                recipients.put(encryptCEKPerRecipient(cek, recipientKeySpec));
            }
            result.putOnce("recipients", recipients);
            return result.toString();
        }
        catch(IllegalArgumentException | JSONException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException e)
        {
            throw new SpxpCryptoException(e);
        }
    }

    private static JSONObject encryptCEKPerRecipient(SecretKey cek, SpxpSymmetricKeySpec recipientKeySpec) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException {
        // encrypt CEK
        byte[] iv = new byte[A256GCM_IV_SIZE / 8];
        secureRandom.nextBytes(iv);
        AlgorithmParameterSpec paramSpec = new GCMParameterSpec(A256GCM_AUTH_TAG_LENGTH, iv);
        int mode = Cipher.WRAP_MODE;
        Cipher c = Cipher.getInstance(A256GCM_JCE_ALGO_SPEC);
        SecretKey secretKey = new SecretKeySpec(recipientKeySpec.getSymmetricKey(), AES_JCE_KEY_SPEC);
        c.init(mode, secretKey, paramSpec); // c.init(mode, secretKey, algoSpec, random);
        byte[] wrappedKeyAndTag = c.wrap(cek);
        byte[] wrappedKey = Arrays.copyOf(wrappedKeyAndTag, wrappedKeyAndTag.length - A256GCM_AUTH_TAG_LENGTH / 8);
        byte[] authTag = Arrays.copyOfRange(wrappedKeyAndTag, wrappedKeyAndTag.length - A256GCM_AUTH_TAG_LENGTH / 8, wrappedKeyAndTag.length);
        Arrays.fill(wrappedKeyAndTag, (byte) 0);
        JSONObject recipientHeader = new JSONObject();
        recipientHeader.putOnce("kid", recipientKeySpec.getKeyId());
        recipientHeader.putOnce("iv", encodeBase64Url(iv));
        recipientHeader.putOnce("tag", encodeBase64Url(authTag));
        JSONObject recipientObj = new JSONObject();
        recipientObj.putOnce("header", recipientHeader);
        recipientObj.putOnce("encrypted_key", encodeBase64Url(wrappedKey));
        return recipientObj;
    }

    public static String decryptSymmetricJson(String json, SpxpKeyProvider keyProvider) throws SpxpCryptoException
    {
        try
        {
            // decode and check headers
            JSONObject obj = new JSONObject(json);
            JSONObject unprotectedHeader = obj.getJSONObject("unprotected");
            String alg = unprotectedHeader.getString("alg");
            if(!alg.equals("A256GCMKW")) {
                throw new SpxpCryptoException("Unsupported algortithm");
            }
            String protectedHeadersJson = new String(decodeBase64Url(obj.getString("protected")), StandardCharsets.UTF_8);
            JSONObject protectedHeader = new JSONObject(protectedHeadersJson);
            String enc = protectedHeader.getString("enc");
            if(!enc.equals("A256GCM")) {
                throw new SpxpCryptoException("Unsupported encoding");
            }
            // 
            JSONArray recipients = obj.getJSONArray("recipients");
            SecretKey cek = null;
            for(Object  o : recipients) {
                if(!(o instanceof JSONObject)) {
                    continue;
                }
                JSONObject candidate = (JSONObject)o;
                try {
                    String kid = candidate.getJSONObject("header").getString("kid");
                    SecretKey keyEncryptionKey = keyProvider.getKey(kid);
                    if(keyEncryptionKey == null) {
                        continue;
                    }
                    cek = decryptCEK(keyEncryptionKey, candidate);
                    if(cek != null) {
                        break;
                    }
                } catch(Exception e) {
                    continue;
                }
            }
            if(cek == null) {
                throw new SpxpCryptoNoSuchKeyException();
            }
            // decode cryptographic material
            String customAADEncoded = obj.optString("aad", null);
            byte[] customAAD = customAADEncoded==null ? null : decodeBase64Url(customAADEncoded);
            byte[] iv = decodeBase64Url(obj.getString("iv"));
            byte[] cipher = decodeBase64Url(obj.getString("ciphertext"));
            byte[] authTag = decodeBase64Url(obj.getString("tag"));
            if(iv.length != (A256GCM_IV_SIZE/8) || authTag.length != A256GCM_AUTH_TAG_LENGTH/8) {
                throw new SpxpCryptoException("Invalid IV or auth tag size");
            }
            // calculate additional authentication data
            byte[] aad = calculateAAD(protectedHeadersJson, customAAD);
            // algo spec
            AlgorithmParameterSpec algoSpec = new GCMParameterSpec(A256GCM_AUTH_TAG_LENGTH, iv);
            // reconstruct encrypted content as used by java
            byte[] encryptedContentWithTag = new byte[cipher.length + authTag.length];
            System.arraycopy(cipher, 0, encryptedContentWithTag, 0, cipher.length);
            System.arraycopy(authTag, 0, encryptedContentWithTag, cipher.length, authTag.length);
            // decrypt
            int mode = Cipher.DECRYPT_MODE;
            Cipher c = Cipher.getInstance(A256GCM_JCE_ALGO_SPEC);
            c.init(mode, cek, algoSpec);
            c.updateAAD(aad);
            byte[] decrypted = c.doFinal(encryptedContentWithTag);
            // return as String
            return new String(decrypted, StandardCharsets.UTF_8);
        }
        catch(IllegalArgumentException | JSONException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e)
        {
            throw new SpxpCryptoException(e);
        }
    }

    private static SecretKey decryptCEK(SecretKey keyEncryptionKey, JSONObject recipient) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, SpxpCryptoException {
        byte[] encryptedKey = decodeBase64Url(recipient.getString("encrypted_key"));
        JSONObject recipientHeader = recipient.getJSONObject("header");
        byte[] iv = decodeBase64Url(recipientHeader.getString("iv"));
        byte[] tag = decodeBase64Url(recipientHeader.getString("tag"));
        AlgorithmParameterSpec paramSpec = new GCMParameterSpec(128, iv);
        // reconstruct encrypted content as used by java
        byte[] encryptedKeyWithTag = new byte[encryptedKey.length + tag.length];
        System.arraycopy(encryptedKey, 0, encryptedKeyWithTag, 0, encryptedKey.length);
        System.arraycopy(tag, 0, encryptedKeyWithTag, encryptedKey.length, tag.length);
        // decrypt
        int mode = Cipher.UNWRAP_MODE;
        Cipher c = Cipher.getInstance(A256GCM_JCE_ALGO_SPEC);
        c.init(mode, keyEncryptionKey, paramSpec);
        SecretKey result = (SecretKey) c.unwrap(encryptedKeyWithTag, AES_JCE_KEY_SPEC, Cipher.SECRET_KEY);
        if(result.getEncoded().length != A256GCM_KEY_SIZE/8) {
            throw new SpxpCryptoException("Invalid key size");
        }
        return result;
    }

    public static String encryptResource(InputStream src, OutputStream dest, String uri) throws IOException, SpxpCryptoException {
        try {
            // content encryption key
            KeyGenerator keyGen = KeyGenerator.getInstance(AES_JCE_KEY_SPEC);
            keyGen.init(A256GCM_KEY_SIZE);  // keyGen.init(A256GCM_KEY_SIZE, secureRandom)
            SecretKey cek = keyGen.generateKey();
            // create random IV
            byte[] iv = new byte[A256GCM_IV_SIZE / 8];
            secureRandom.nextBytes(iv);
            // algo spec
            AlgorithmParameterSpec algoSpec = new GCMParameterSpec(A256GCM_AUTH_TAG_LENGTH, iv);
            // init Cipher
            int mode = Cipher.ENCRYPT_MODE;
            Cipher c = Cipher.getInstance(A256GCM_JCE_ALGO_SPEC);
            c.init(mode, cek, algoSpec);
            // encrypt
            RestrainLastNBytesOutputStream rlnbos = new RestrainLastNBytesOutputStream(dest, A256GCM_AUTH_TAG_LENGTH / 8);
            CipherOutputStream cos = new CipherOutputStream(rlnbos, c);
            try {
                int bytesRead = -1;
                byte[] buffer = new byte[4096];
                while ((bytesRead = src.read(buffer)) != -1) {
                    cos.write(buffer, 0, bytesRead);
                }
            } finally {
                src.close();
                cos.close();
            }
            byte[] tag = rlnbos.getRestrainedBytes();
            // build describing JSON object
            JSONObject result = new JSONObject();
            result.put("iv", encodeBase64Url(iv));
            result.put("k", encodeBase64Url(cek.getEncoded()));
            result.put("tag", encodeBase64Url(tag));
            if(uri != null) {
                result.put("uri", uri);
            }
            return result.toString();
        } catch(NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | JSONException e) {
            throw new SpxpCryptoException(e);
        }
    }

    public static void decryptResource(InputStream src, OutputStream dest, String json) throws IOException, SpxpCryptoException {
        try {
            JSONObject obj = new JSONObject(json);
            byte[] iv = decodeBase64Url(obj.getString("iv"));
            byte[] k = decodeBase64Url(obj.getString("k"));
            byte[] authTag = decodeBase64Url(obj.getString("tag"));
            if(iv.length != (A256GCM_IV_SIZE/8) || authTag.length != A256GCM_AUTH_TAG_LENGTH/8 || k.length != A256GCM_KEY_SIZE/8) {
                throw new SpxpCryptoException("Invalid IV, auth tag or key size");
            }
            // algo spec
            AlgorithmParameterSpec algoSpec = new GCMParameterSpec(A256GCM_AUTH_TAG_LENGTH, iv);
            // init cipher
            int mode = Cipher.DECRYPT_MODE;
            Cipher c = Cipher.getInstance(A256GCM_JCE_ALGO_SPEC);
            c.init(mode,  new SecretKeySpec(k, AES_JCE_KEY_SPEC), algoSpec);
            // decrypt
            CipherOutputStream cos = new CipherOutputStream(dest, c);
            try {
                int bytesRead = -1;
                byte[] buffer = new byte[4096];
                while ((bytesRead = src.read(buffer)) != -1) {
                    cos.write(buffer, 0, bytesRead);
                }
                cos.write(authTag);
            } finally {
                src.close();
                cos.close();
            }
        } catch(IllegalArgumentException | JSONException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException e) {
            throw new SpxpCryptoException(e);
        }
    }

    public static String encryptAsymmetricJson(String payload, SpxpConnectPublicKey recipientKey /*, String aad*/) throws SpxpCryptoException
    {
        try
        {
            // handle aad
            byte[] customAAD = null; //aad != null ? aad.getBytes(StandardCharsets.UTF_8) : null;
            // generate ephemeral keypair
            SpxpConnectKeyPair ephemeralKeypair = generateConnectKeyPair();
            // calculate CEK
            byte[] z = calculateECDHKeyAgreement(ephemeralKeypair, recipientKey);
            byte[] cekBytes = calculateJweDerivedKey(z, "A256GCM", (new byte[0]), (new byte[0]), 256);
            SecretKey cek = new SecretKeySpec(cekBytes, AES_JCE_KEY_SPEC);
            // create random IV
            byte[] iv = new byte[A256GCM_IV_SIZE / 8];
            secureRandom.nextBytes(iv);
            // algo spec
            AlgorithmParameterSpec algoSpec = new GCMParameterSpec(A256GCM_AUTH_TAG_LENGTH, iv);
            // protected headers
            String protectedHeadersJson = "{\"enc\":\"A256GCM\"}";
            // calculate additional authentication data
            byte[] combinedAAD  = calculateAAD(protectedHeadersJson, customAAD);
            // init Cipher
            int mode = Cipher.ENCRYPT_MODE;
            Cipher c = Cipher.getInstance(A256GCM_JCE_ALGO_SPEC);
            c.init(mode, cek, algoSpec);
            c.updateAAD(combinedAAD);
            // encrypt
            byte[] encryptedContent = c.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            byte[] cipher = Arrays.copyOf(encryptedContent, encryptedContent.length - A256GCM_AUTH_TAG_LENGTH / 8);
            byte[] authTag = Arrays.copyOfRange(encryptedContent, encryptedContent.length - A256GCM_AUTH_TAG_LENGTH / 8, encryptedContent.length);
            // prepare JSON JWE result
            JSONObject result = new JSONObject();
            result.putOnce("ciphertext", encodeBase64Url(cipher));
            result.putOnce("protected", encodeBase64Url(protectedHeadersJson.getBytes(StandardCharsets.UTF_8)));
            /*if(customAAD != null &&  customAAD.length > 0)
            {
                result.putOnce("aad", encodeBase64Url(customAAD));
            }*/
            JSONObject unprotectedHeader = new JSONObject();
            unprotectedHeader.putOnce("alg", "ECDH-ES");
            result.putOnce("unprotected", unprotectedHeader);
            result.putOnce("tag", encodeBase64Url(authTag));
            result.putOnce("iv", encodeBase64Url(iv));
            JSONObject recipientHeader = new JSONObject();
            recipientHeader.putOnce("kid", recipientKey.getKeyId());
            recipientHeader.putOnce("epk", getPublicJWK(ephemeralKeypair.extractConnectPublicKey()));
            JSONObject recipientObject = new JSONObject();
            recipientObject.putOnce("header", recipientHeader);
            JSONArray recipients = new JSONArray();
            recipients.put(recipientObject);
            result.putOnce("recipients", recipients);
            return result.toString();
        }
        catch(IllegalArgumentException | JSONException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException e)
        {
            throw new SpxpCryptoException(e);
        }
    }

    public static String decryptAsymmetricJson(String json, SpxpConnectKeyPair keyPair) throws SpxpCryptoException
    {
        try
        {
            // decode and check headers
            JSONObject obj = new JSONObject(json);
            JSONObject unprotectedHeader = obj.getJSONObject("unprotected");
            String alg = unprotectedHeader.getString("alg");
            if(!alg.equals("ECDH-ES")) {
                throw new SpxpCryptoException("Unsupported algortithm");
            }
            String protectedHeadersJson = new String(decodeBase64Url(obj.getString("protected")), StandardCharsets.UTF_8);
            JSONObject protectedHeader = new JSONObject(protectedHeadersJson);
            String enc = protectedHeader.getString("enc");
            if(!enc.equals("A256GCM")) {
                throw new SpxpCryptoException("Unsupported encoding");
            }
            // get epk from recipients
            JSONArray recipients = obj.getJSONArray("recipients");
            if(recipients.length() != 1) {
                throw new SpxpCryptoException("Invalid JWE: ECDH-AS algo supports only exactly one recipient");
            }
            JSONObject recipient = recipients.getJSONObject(0);
            JSONObject recipientHeader = recipient.getJSONObject("header");
            String kid = recipientHeader.getString("kid");
            JSONObject epk = recipientHeader.getJSONObject("epk");
            if(!kid.equals(keyPair.getKeyId())) {
                throw new SpxpCryptoNoSuchKeyException();
            }
            SpxpConnectPublicKey ephemeralPublicKey = getConnectPublicKey(epk);
            // calculate CEK
            byte[] z = calculateECDHKeyAgreement(keyPair, ephemeralPublicKey);
            byte[] cekBytes = calculateJweDerivedKey(z, "A256GCM", (new byte[0]), (new byte[0]), 256);
            SecretKey cek = new SecretKeySpec(cekBytes, AES_JCE_KEY_SPEC);
            // decode cryptographic material
            String customAADEncoded = obj.optString("aad", null);
            byte[] customAAD = customAADEncoded==null ? null : decodeBase64Url(customAADEncoded);
            byte[] iv = decodeBase64Url(obj.getString("iv"));
            byte[] cipher = decodeBase64Url(obj.getString("ciphertext"));
            byte[] authTag = decodeBase64Url(obj.getString("tag"));
            if(iv.length != (A256GCM_IV_SIZE/8) || authTag.length != A256GCM_AUTH_TAG_LENGTH/8) {
                throw new SpxpCryptoException("Invalid IV or auth tag size");
            }
            // calculate additional authentication data
            byte[] aad = calculateAAD(protectedHeadersJson, customAAD);
            // algo spec
            AlgorithmParameterSpec algoSpec = new GCMParameterSpec(A256GCM_AUTH_TAG_LENGTH, iv);
            // reconstruct encrypted content as used by java
            byte[] encryptedContentWithTag = new byte[cipher.length + authTag.length];
            System.arraycopy(cipher, 0, encryptedContentWithTag, 0, cipher.length);
            System.arraycopy(authTag, 0, encryptedContentWithTag, cipher.length, authTag.length);
            // decrypt
            int mode = Cipher.DECRYPT_MODE;
            Cipher c = Cipher.getInstance(A256GCM_JCE_ALGO_SPEC);
            c.init(mode, cek, algoSpec);
            c.updateAAD(aad);
            byte[] decrypted = c.doFinal(encryptedContentWithTag);
            // return as String
            return new String(decrypted, StandardCharsets.UTF_8);
        }
        catch(IllegalArgumentException | JSONException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e)
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

    public static enum KeyIdSize {
        SHORT(6), LONG(12);
        private int sizeInBytes;
        private KeyIdSize(int sizeInBytes) {
            this.sizeInBytes = sizeInBytes;
        }
        public int getSizeInBytes() {
            return sizeInBytes;
        }
    }

    public static String generateRandomKeyId(KeyIdSize size)
    {
        byte[] result = new byte[size.getSizeInBytes()];
        secureRandom.nextBytes(result);
        return encodeBase64Url(result);
    }

    public static SpxpProfileKeyPair generateProfileKeyPair() {
        byte[] publicKey = new byte[org.bouncycastle.math.ec.rfc8032.Ed25519.PUBLIC_KEY_SIZE];
        byte[] secretKey = new byte[org.bouncycastle.math.ec.rfc8032.Ed25519.SECRET_KEY_SIZE];
        org.bouncycastle.math.ec.rfc8032.Ed25519.generatePrivateKey(secureRandom, secretKey);
        org.bouncycastle.math.ec.rfc8032.Ed25519.generatePublicKey(secretKey, 0, publicKey, 0);
        return new SpxpProfileKeyPair(generateRandomKeyId(KeyIdSize.LONG), secretKey, publicKey);
    }

    public static SpxpConnectKeyPair generateConnectKeyPair() {
        byte[] publicKey = new byte[org.bouncycastle.math.ec.rfc8032.Ed25519.PUBLIC_KEY_SIZE];
        byte[] secretKey = new byte[org.bouncycastle.math.ec.rfc8032.Ed25519.SECRET_KEY_SIZE];
        org.bouncycastle.math.ec.rfc7748.X25519.generatePrivateKey(secureRandom, secretKey);
        org.bouncycastle.math.ec.rfc7748.X25519.generatePublicKey(secretKey, 0, publicKey, 0);
        return new SpxpConnectKeyPair(generateRandomKeyId(KeyIdSize.LONG), secretKey, publicKey);
    }

    private static byte[] calculateECDHKeyAgreement(SpxpConnectKeyPair privateKey, SpxpConnectPublicKey publicKey) throws SpxpCryptoException
    {
        byte[] secret = new byte[org.bouncycastle.math.ec.rfc7748.X25519.POINT_SIZE];
        if (!org.bouncycastle.math.ec.rfc7748.X25519.calculateAgreement(privateKey.getSecretKey(), 0, publicKey.getPublicKey(), 0, secret, 0))
        {
            throw new SpxpCryptoException("ECDH key agreement failed");
        }
        return secret;
    }

    private static byte[] calculateJweDerivedKey(byte[] z, String algoName, byte[] apu, byte[] apv, int targetKeyBitLen) throws SpxpCryptoException
    {
        if(targetKeyBitLen > 256)
        {
            throw new SpxpCryptoException("this implementation only supports derived keys up to 256 bit");
        }
        try
        {
            byte[] algoNameBytes = algoName.getBytes(StandardCharsets.US_ASCII);
            ByteBuffer otherInfoBuffer = ByteBuffer.allocate(4 + algoNameBytes.length + 4 + apu.length + 4 + apv.length + 4);
            otherInfoBuffer.putInt(algoNameBytes.length);
            otherInfoBuffer.put(algoNameBytes);
            otherInfoBuffer.putInt(apu.length);
            otherInfoBuffer.put(apu);
            otherInfoBuffer.putInt(apv.length);
            otherInfoBuffer.put(apv);
            otherInfoBuffer.putInt(targetKeyBitLen);
            byte[] otherInfo = otherInfoBuffer.array();
            ByteBuffer concatKdfBuffer = ByteBuffer.allocate(4 + z.length + otherInfo.length);
            concatKdfBuffer.putInt(1);
            concatKdfBuffer.put(z);
            concatKdfBuffer.put(otherInfo);
            byte[] concatKdf = concatKdfBuffer.array();
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] round1Hash = md.digest(concatKdf);
            return Arrays.copyOf(round1Hash, targetKeyBitLen / 8);
        }
        catch(Exception e)
        {
            throw new SpxpCryptoException(e);
        }
    }

    public static void signObject(JSONObject value, SpxpProfileKeyPair profileKeyPair) throws SpxpCryptoException {
        if(value.has("signature")) {
            throw new SpxpCryptoException("Object already signed");
        }
        byte[] signature = new byte[org.bouncycastle.math.ec.rfc8032.Ed25519.SIGNATURE_SIZE];
        byte[] canonicalizedBytes;
        try {
            canonicalizedBytes = canonicalize(value, OMIT_MEMBERS_SIGN).getBytes(StandardCharsets.UTF_8);
        } catch (JSONException | IOException e) {
            throw new SpxpCryptoException("Error canonicalizing object", e);
        }
        try {
            org.bouncycastle.math.ec.rfc8032.Ed25519.sign(profileKeyPair.getSecretKey(), 0, canonicalizedBytes, 0, canonicalizedBytes.length, signature, 0);
            JSONObject signatureObject = new JSONObject();
            signatureObject.put("key", profileKeyPair.getKeyId());
            signatureObject.put("sig", encodeBase64Url(signature));
            value.put("signature", signatureObject);
        } catch(IllegalArgumentException | IllegalStateException | JSONException e) {
            throw new SpxpCryptoException(e);
        }
    }

    public static boolean verifySignature(JSONObject signedObject, SpxpProfilePublicKey publicKey, Collection<String> requiredPermissions) throws SpxpCryptoException {
        JSONObject signature = signedObject.optJSONObject("signature");
        if(signature == null) {
            return false;
        }
        String sigStr = signature.optString("sig", null);
        if(sigStr == null) {
            return false;
        }
        byte[] sig;
        try {
            sig = decodeBase64Url(sigStr);
        } catch(IllegalArgumentException e) {
            return false;
        }
        if(sig == null || sig.length != org.bouncycastle.math.ec.rfc8032.Ed25519.SIGNATURE_SIZE) {
            return false;
        }
        Object keyObj = signature.opt("key");
        if(keyObj == null) {
            return false;
        }
        byte[] signingPublicKey;
        if(keyObj instanceof String) {
            if(publicKey.getKeyId().equals((String)keyObj)) {
                signingPublicKey = publicKey.getPublicKey();
            } else {
                return false;
            }
        } else if(keyObj instanceof JSONObject) {
            if(requiredPermissions == null) {
                return false;
            }
            JSONObject certChain = (JSONObject)keyObj;
            SpxpProfilePublicKey signingAuthorityPublicKey;
            try {
                if(!certChain.getJSONArray("grant").toList().containsAll(requiredPermissions)) {
                    return false;
                }
                signingAuthorityPublicKey = getProfilePublicKey(certChain.getJSONObject("publicKey"));
            } catch(IllegalArgumentException | JSONException e) {
                return false;
            }
            ArrayList<String> requiredSignerPermissions = new ArrayList<>(requiredPermissions.size()+1);
            requiredSignerPermissions.addAll(requiredPermissions);
            if(requiredPermissions.contains("grant")) {
                if(!requiredSignerPermissions.contains("ca")) {
                    requiredSignerPermissions.add("ca");
                }
            } else {
                if(!requiredSignerPermissions.contains("grant")) {
                    requiredSignerPermissions.add("grant");
                }
            }
            if(!verifySignature(certChain, publicKey, requiredSignerPermissions)) {
                return false;
            }
            signingPublicKey = signingAuthorityPublicKey.getPublicKey();
        } else {
            return false;
        }
        byte[] canonicalizedBytes;
        try {
            canonicalizedBytes = canonicalize(signedObject, OMIT_MEMBERS_VERIFY).getBytes(StandardCharsets.UTF_8);
        } catch (JSONException | IOException e) {
            return false;
        }
        try {
            return org.bouncycastle.math.ec.rfc8032.Ed25519.verify(sig, 0, signingPublicKey, 0, canonicalizedBytes, 0, canonicalizedBytes.length);
        } catch(IllegalArgumentException | IllegalStateException e) {
            return false;
        }
    }

    public static String canonicalize(JSONObject jsonObject, Set<String> omitMembers) throws JSONException, IOException {
        StringWriter writer = new StringWriter();
        writeCanonicalizedJSONObject(writer, jsonObject, omitMembers);
        return writer.toString();
    }

    private static void writeQuotedString(Writer writer, String value) throws JSONException, IOException {
        writer.write('"');
        for(int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch(c) {
            case '"':
                writer.write("\\\"");
                break;
            case '\\':
                writer.write("\\\\");
                break;
            case '\t':
                writer.write("\\t");
                break;
            case '\b':
                writer.write("\\b");
                break;
            case '\n':
                writer.write("\\n");
                break;
            case '\r':
                writer.write("\\r");
                break;
            case '\f':
                writer.write("\\f");
                break;
            default:
                if(c < ' ') {
                    writer.write("\\u");
                    String hex = Integer.toHexString(c);
                    writer.write("0000", 0, 4 - hex.length());
                    writer.write(hex);
                } else {
                    writer.write(c);;
                }
            }
        }
        writer.write('"');
    }

    private static void writeCanonicalizedJSONObject(Writer writer, JSONObject jsonObject, Set<String> omitMembers) throws JSONException, IOException {
        boolean commanate = false;
        writer.write('{');
        TreeSet<String> keys = new TreeSet<>(jsonObject.keySet());
        for(String key : keys) {
            if(omitMembers != null && omitMembers.contains(key)) {
                continue;
            }
            Object value = jsonObject.opt(key);
            if (commanate) {
                writer.write(',');
            }
            writeQuotedString(writer, key);
            writer.write(':');
            try {
                writeJsonValue(writer, value);
            } catch (Exception e) {
                throw new JSONException("Unable to write JSONObject value for key: " + key, e);
            }
            commanate = true;
        }
        writer.write('}');
    }

    private static void writeJsonValue(Writer writer, Object value) throws JSONException, IOException {
        if (value == null || value.equals(null)) {
            writer.write("null");
        } else if (value instanceof JSONString) {
            throw new JSONException("JSONString interface not supported for canonicalization");
        } else if (value instanceof Number) {
            // not all Numbers may match actual JSON Numbers. i.e. fractions or Imaginary
            final String numberAsString = JSONObject.numberToString((Number) value);
            try {
                // Use the BigDecimal constructor for its parser to validate the format.
                @SuppressWarnings("unused")
                BigDecimal testNum = new BigDecimal(numberAsString);
                // Close enough to a JSON number that we will use it unquoted
                writer.write(numberAsString);
            } catch (NumberFormatException ex){
                // The Number value is not a valid JSON number.
                // Instead we will quote it as a string
                writeQuotedString(writer, numberAsString);
            }
        } else if (value instanceof Boolean) {
            writer.write(value.toString());
        } else if (value instanceof Enum<?>) {
            writeQuotedString(writer, ((Enum<?>)value).name());
        } else if (value instanceof JSONObject) {
            writeCanonicalizedJSONObject(writer, ((JSONObject) value), null);
        } else if (value instanceof JSONArray) {
            writeJSONArray(writer, ((JSONArray) value));
        } else if (value instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) value;
            writeCanonicalizedJSONObject(writer, (new JSONObject(map)), null);
        } else if (value instanceof Collection) {
            Collection<?> coll = (Collection<?>) value;
            writeJSONArray(writer, (new JSONArray(coll)));
        } else if (value.getClass().isArray()) {
            writeJSONArray(writer, (new JSONArray(value)));
        } else {
            writeQuotedString(writer, value.toString());
        }
    }

    private static void writeJSONArray(Writer writer, JSONArray value) throws JSONException, IOException {
        boolean commanate = false;
        int length = value.length();
        writer.write('[');
        for (int i = 0; i < length; i += 1) {
            if (commanate) {
                writer.write(',');
            }
            writeJsonValue(writer, value.get(i));
            commanate = true;
        }
        writer.write(']');
    }

    public static JSONObject getKeypairJWK(SpxpProfileKeyPair keyPair) {
        JSONObject jwkObj = new JSONObject();
        jwkObj.put("kid", keyPair.getKeyId());
        jwkObj.put("kty", "OKP");
        jwkObj.put("crv", "Ed25519");
        jwkObj.put("x", encodeBase64Url(keyPair.getPublicKey()));
        jwkObj.put("d", encodeBase64Url(keyPair.getSecretKey()));
        return jwkObj;
    }

    public static JSONObject getPublicJWK(SpxpProfilePublicKey publicKey) {
        JSONObject jwkObj = new JSONObject();
        jwkObj.put("kid", publicKey.getKeyId());
        jwkObj.put("kty", "OKP");
        jwkObj.put("crv", "Ed25519");
        jwkObj.put("x", encodeBase64Url(publicKey.getPublicKey()));
        return jwkObj;
    }

    public static JSONObject getSymmetricJWK(SpxpSymmetricKeySpec keySpec) {
        JSONObject jwkObj = new JSONObject();
        jwkObj.put("kid", keySpec.getKeyId());
        jwkObj.put("kty", "oct");
        jwkObj.put("alg", "A256GCM");
        jwkObj.put("k", encodeBase64Url(keySpec.getSymmetricKey()));
        return jwkObj;
    }

    public static JSONObject getKeypairJWK(SpxpConnectKeyPair keyPair) {
        JSONObject jwkObj = new JSONObject();
        jwkObj.put("kid", keyPair.getKeyId());
        jwkObj.put("kty", "OKP");
        jwkObj.put("crv", "X25519");
        jwkObj.put("x", encodeBase64Url(keyPair.getPublicKey()));
        jwkObj.put("d", encodeBase64Url(keyPair.getSecretKey()));
        return jwkObj;
    }

    public static JSONObject getPublicJWK(SpxpConnectPublicKey publicKey) {
        JSONObject jwkObj = new JSONObject();
        jwkObj.put("kid", publicKey.getKeyId());
        jwkObj.put("kty", "OKP");
        jwkObj.put("crv", "X25519");
        jwkObj.put("x", encodeBase64Url(publicKey.getPublicKey()));
        return jwkObj;
    }

    public static SpxpProfileKeyPair getProfileKeyPair(JSONObject jwk) throws SpxpCryptoException {
        try {
            String kid = jwk.getString("kid");
            String kty = jwk.getString("kty");
            if(!kty.equals("OKP")) {
                throw new SpxpCryptoException("Invalid key type. Expected OKP");
            }
            String crv = jwk.getString("crv");
            if(!crv.equals("Ed25519")) {
                throw new SpxpCryptoException("Invalid curve. Expected Ed25519");
            }
            byte[] x = decodeBase64Url(jwk.getString("x"));
            byte[] d = decodeBase64Url(jwk.getString("d"));
            return new SpxpProfileKeyPair(kid, d, x);
        } catch(IllegalArgumentException | JSONException e) {
            throw new SpxpCryptoException("Invalid keypair", e);
        }
    }

    public static SpxpProfilePublicKey getProfilePublicKey(JSONObject jwk) throws SpxpCryptoException {
        try {
            String kid = jwk.getString("kid");
            String kty = jwk.getString("kty");
            if(!kty.equals("OKP")) {
                throw new SpxpCryptoException("Invalid key type. Expected OKP");
            }
            String crv = jwk.getString("crv");
            if(!crv.equals("Ed25519")) {
                throw new SpxpCryptoException("Invalid curve. Expected Ed25519");
            }
            byte[] x = decodeBase64Url(jwk.getString("x"));
            return new SpxpProfilePublicKey(kid, x);
        } catch(IllegalArgumentException | JSONException e) {
            throw new SpxpCryptoException("Invalid public key", e);
        }
    }

    public static SpxpSymmetricKeySpec getSymmetricKeySpec(JSONObject jwk) throws SpxpCryptoException {
        try {
            String kid = jwk.getString("kid");
            String kty = jwk.getString("kty");
            if(!kty.equals("oct")) {
                throw new SpxpCryptoException("Invalid key type. Expected oct");
            }
            String alg = jwk.getString("alg");
            if(!alg.equals("A256GCM")) {
                throw new SpxpCryptoException("Invalid alg. Expected A256GCM");
            }
            byte[] k = decodeBase64Url(jwk.getString("k"));
            return new SpxpSymmetricKeySpec(kid, k);
        } catch(IllegalArgumentException | JSONException e) {
            throw new SpxpCryptoException("Invalid symmetric key", e);
        }
    }

    public static SpxpConnectKeyPair getConnectKeyPair(JSONObject jwk) throws SpxpCryptoException {
        try {
            String kid = jwk.getString("kid");
            String kty = jwk.getString("kty");
            if(!kty.equals("OKP")) {
                throw new SpxpCryptoException("Invalid key type. Expected OKP");
            }
            String crv = jwk.getString("crv");
            if(!crv.equals("X25519")) {
                throw new SpxpCryptoException("Invalid curve. Expected Ed25519");
            }
            byte[] x = decodeBase64Url(jwk.getString("x"));
            byte[] d = decodeBase64Url(jwk.getString("d"));
            return new SpxpConnectKeyPair(kid, d, x);
        } catch(IllegalArgumentException | JSONException e) {
            throw new SpxpCryptoException("Invalid keypair", e);
        }
    }

    public static SpxpConnectPublicKey getConnectPublicKey(JSONObject jwk) throws SpxpCryptoException {
        try {
            String kid = jwk.getString("kid");
            String kty = jwk.getString("kty");
            if(!kty.equals("OKP")) {
                throw new SpxpCryptoException("Invalid key type. Expected OKP");
            }
            String crv = jwk.getString("crv");
            if(!crv.equals("X25519")) {
                throw new SpxpCryptoException("Invalid curve. Expected Ed25519");
            }
            byte[] x = decodeBase64Url(jwk.getString("x"));
            return new SpxpConnectPublicKey(kid, x);
        } catch(IllegalArgumentException | JSONException e) {
            throw new SpxpCryptoException("Invalid public key", e);
        }
    }

}
