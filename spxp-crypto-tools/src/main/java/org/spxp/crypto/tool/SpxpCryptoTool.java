package org.spxp.crypto.tool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONException;
import org.json.JSONObject;
import org.spxp.crypto.SpxpConnectKeyPair;
import org.spxp.crypto.SpxpConnectPublicKey;
import org.spxp.crypto.SpxpCryptoException;
import org.spxp.crypto.SpxpCryptoNoSuchKeyException;
import org.spxp.crypto.SpxpCryptoToolsV04;
import org.spxp.crypto.SpxpCryptoToolsV04.KeyIdSize;
import org.spxp.crypto.SpxpKeyProvider;
import org.spxp.crypto.SpxpProfileKeyPair;
import org.spxp.crypto.SpxpProfilePublicKey;
import org.spxp.crypto.SpxpSymmetricKeySpec;

public class SpxpCryptoTool {

    public static void main(String[] args) throws Exception {
        (new SpxpCryptoTool()).run(args);
    }

    private SpxpCryptoTool() {
    }
    
    public void run(String[] args) throws Exception {
        if(args.length < 1) {
            usage();
            return;
        }
        if(args[0].equals("genprofilekeypair")) {
            genprofilekeypair(args);
        } else if(args[0].equals("extractprofilepublic")) {
            extractprofilepublic(args);
        } else if(args[0].equals("sign")) {
            sign(args);
        } else if(args[0].equals("verify")) {
            verify(args);
        } else if(args[0].equals("canonicalize")) {
            canonicalize(args);
        } else if(args[0].equals("gensymkey")) {
            gensymkey(args);
        } else if(args[0].equals("genroundkey")) {
            genroundkey(args);
        } else if(args[0].equals("encryptsymcompact")) {
            encryptsymcompact(args);
        } else if(args[0].equals("decryptsymcompact")) {
            decryptsymcompact(args);
        } else if(args[0].equals("encryptsymjson")) {
            encryptsymjson(args);
        } else if(args[0].equals("decryptsymjson")) {
            decryptsymjson(args);
        } else if(args[0].equals("encryptresource")) {
            encryptresource(args);
        } else if(args[0].equals("decryptresource")) {
            decryptresource(args);
        } else if(args[0].equals("genconnectkeypair")) {
            genconnectkeypair(args);
        } else if(args[0].equals("extractconnectpublic")) {
            extractconnectpublic(args);
        } else if(args[0].equals("encryptasymjson")) {
            encryptasymjson(args);
        } else if(args[0].equals("decryptasymjson")) {
            decryptasymjson(args);
        } else if(args[0].equals("help")) {
            usage();
        } else {
            usage();
        }
    }
    
    public void usage() {
        System.out.println("SPXP Crypto Tool V 0.3");
        System.out.println("Usage: SpxpCryptoTool <command> [<option>*]");
        System.out.println("Commands:");
        System.out.println("  genprofilekeypair");
        System.out.println("      generates a new profile keypair");
        System.out.println("  extractprofilepublic <keyPairFile>");
        System.out.println("      extracts just the public key part from the profile keypair stored in <keyPairFile>");
        System.out.println("  sign [-aad <additionallyAuthenticatedData>] <jsonFileToSign> <keyPairFile>");
        System.out.println("      signs the json object in <jsonFileToSign> with the secret key stored in <keyPairFile>");
        System.out.println("  verify <signedJsonFile> <publicKeyFile> [<requiredGrant>[,<requiredGrant>]*]");
        System.out.println("      verifies the signature in <signedJsonFile> with the public key stored in <publicKeyFile>");
        System.out.println("      if certificate chains are accepted, then <requiredGrant>s give a comma separated list of");
        System.out.println("      grant values that must be authorized from the root <publicKeyFile>");
        System.out.println("      exits with 0 status code on success and 1 on a broken signature");
        System.out.println("  canonicalize [-omitSpecial] <jsonFile>");
        System.out.println("      canonicalizes <jsonFile>. If -omitSpecial is given, the fields \"private\", \"seqts\", \"signature\" are omitted");
        System.out.println("  gensymkey");
        System.out.println("      generates a new 256 bit AES key");
        System.out.println("  genroundkey");
        System.out.println("      generates a new 256 bit AES key with a key-id suitable as round key");
        System.out.println("  encryptsymcompact <fileToEncrypt> <symmetricKey>");
        System.out.println("      encrypts <fileToEncrypt> with the 256 bit AES key from <symmetricKey> in JWE compact serialization");
        System.out.println("  decryptsymcompact <fileToDecrypt> [<symmetricKey>]");
        System.out.println("      decrypts <fileToDecrypt> in JWE compact serialization with the 256 bit AES key from <symmetricKey>");
        System.out.println("      prints the key id of the required key if no <symmetricKey> is given");
        System.out.println("  encryptsymjson <fileToEncrypt> <symmetricKey>[,<symmetricKey>]*");
        System.out.println("      encrypts <fileToEncrypt> with the 256 bit AES keys from the comma separated lis of <symmetricKey>s in JWE json serialization");
        System.out.println("  decryptsymjson <fileToDecrypt> [<symmetricKey>]");
        System.out.println("      decrypts <fileToDecrypt> in JWE json serialization with the 256 bit AES key from <symmetricKey>");
        System.out.println("      prints the list of key ids of all keys that can be used to decrypt the content if no <symmetricKey> is given");
        System.out.println("  encryptresource <inputFile> <outputFile> [<resourceUri>]");
        System.out.println("      encrypts the binary data in <inputFile> with a new random 256 bit AES key, writes the encrypted data");
        System.out.println("      to <outputFile> and prints the JSON object describing the encrypted resource to std out");
        System.out.println("  decryptresource <inputFile> <decryptionKeyFile> <outputFile>");
        System.out.println("      decrypts the binary data in <inputFile> with the key described in <decryptionKeyFile> and writes");
        System.out.println("      the decrypted data to <outputFile>");
        System.out.println("  genconnectkeypair");
        System.out.println("      generates a new connect keypair");
        System.out.println("  extractconnectpublic <keyPairFile>");
        System.out.println("      extracts just the public key part from the connect keypair stored in <keyPairFile>");
        System.out.println("  encryptasymjson <fileToEncrypt> <publicKeyFile>");
        System.out.println("      encrypts <fileToEncrypt> with the public key of the connect keypair stored in <publicKeyFile> in JWE json serialization");
        System.out.println("  decryptsymjson <fileToDecrypt> <keyPairFile>");
        System.out.println("      decrypts <fileToDecrypt> in JWE json serialization with the private key of the connect keypair from <keyPairFile>");
        System.out.println("  help");
        System.out.println("      print this screen");
        System.out.println();
        System.out.println("Find a detailed decription on https://github.com/spxp/spxp-crypto/spxp-crypto-tool/README.md");
    }
    
    public void genprofilekeypair(String[] args) throws Exception {
        if(args.length != 1) {
            System.out.println("Error: Command 'genprofilekeypair' does not take any options");
            return;
        }
        SpxpProfileKeyPair keypair = SpxpCryptoToolsV04.generateProfileKeyPair();
        try(PrintWriter writer = new PrintWriter(System.out)) {
            SpxpCryptoToolsV04.getKeypairJWK(keypair).write(writer, 4, 0);
        }
    }
    
    public void extractprofilepublic(String[] args) throws Exception {
        if(args.length != 2) {
            System.out.println("Error: Invalid number of options for command 'extractprofilepublic'");
            return;
        }
        JSONObject keypairJwkObj = new JSONObject(new String(Files.readAllBytes(Paths.get(args[1])), StandardCharsets.UTF_8));
        SpxpProfileKeyPair keypair = SpxpCryptoToolsV04.getProfileKeyPair(keypairJwkObj);
        JSONObject publicJwk = SpxpCryptoToolsV04.getPublicJWK(keypair.extractProfilePublicKey());
        try(PrintWriter writer = new PrintWriter(System.out)) {
            publicJwk.write(writer, 4, 0);
        }
    }
    
    public void sign(String[] args) throws Exception {
        if(args.length != 3 && args.length != 5) {
            System.out.println("Error: Invalid number of options for command 'sign'");
            return;
        }
        String aad = null;
        String jsonFileToSign, keyPairFile;
        if(args.length == 5) {
            if(!args[1].equals("-aad")) {
                System.out.println("Error: Invalid number of options for command 'sign'");
                return;
            }
            aad = args[2];
            jsonFileToSign = args[3];
            keyPairFile = args[4];
        } else {
            jsonFileToSign = args[1];
            keyPairFile = args[2];
        }
        JSONObject objToSign = new JSONObject(new String(Files.readAllBytes(Paths.get(jsonFileToSign)), StandardCharsets.UTF_8));
        JSONObject keypairJwkObj = new JSONObject(new String(Files.readAllBytes(Paths.get(keyPairFile)), StandardCharsets.UTF_8));
        SpxpProfileKeyPair keypair = SpxpCryptoToolsV04.getProfileKeyPair(keypairJwkObj);
        SpxpCryptoToolsV04.signObject(objToSign, keypair, aad);
        try(PrintWriter writer = new PrintWriter(System.out)) {
            objToSign.write(writer, 4, 0);
        }
    }
    
    public void verify(String[] args) throws Exception {
        if(args.length < 3 || args.length > 4) {
            System.out.println("Error: Invalid number of options for command 'verify'");
            return;
        }
        JSONObject signedObject = new JSONObject(new String(Files.readAllBytes(Paths.get(args[1])), StandardCharsets.UTF_8));
        JSONObject publicJwkObj = new JSONObject(new String(Files.readAllBytes(Paths.get(args[2])), StandardCharsets.UTF_8));
        List<String> requiredGrants = args.length > 3 ? Arrays.asList(args[3].split(",")) : null;
        SpxpProfilePublicKey publicKey = SpxpCryptoToolsV04.getProfilePublicKey(publicJwkObj);
        if(SpxpCryptoToolsV04.verifySignature(signedObject, publicKey, null, requiredGrants)) {
            System.out.println("Signature valid.");
            System.exit(0);
        } else {
            System.out.println("BROKEN Signature.");
            System.exit(1);
        }
    }
    
    public void canonicalize(String[] args) throws Exception {
        if(args.length < 2 || args.length > 3) {
            System.out.println("Error: Invalid number of options for command 'canonicalize'");
            return;
        }
        boolean omitSpecial = false;
        String jsonFileName;
        if(args.length > 2) {
            if(args[1].equals("-omitSpecial")) {
                omitSpecial = true;
            } else {
                System.out.println("Error: Invalid parameter");
                return;
            }
            jsonFileName = args[2];
        } else {
            jsonFileName = args[1];
        }
        JSONObject inputObject = new JSONObject(new String(Files.readAllBytes(Paths.get(jsonFileName)), StandardCharsets.UTF_8));
        try {
            Set<String> omitMembers = omitSpecial ? SpxpCryptoToolsV04.OMIT_MEMBERS_VERIFY : null;
            System.out.println(SpxpCryptoToolsV04.canonicalize(inputObject, omitMembers));
        } catch (JSONException | IOException e) {
            throw new SpxpCryptoException("Error canonicalizing object", e);
        }
    }
    
    public void gensymkey(String[] args) throws Exception {
        if(args.length != 1) {
            System.out.println("Error: Command 'gensymkey' does not take any options");
            return;
        }
        byte[] key = SpxpCryptoToolsV04.generateSymmetricKey(256);
        String kid = SpxpCryptoToolsV04.generateRandomKeyId(KeyIdSize.LONG);
        SpxpSymmetricKeySpec keySpec = new SpxpSymmetricKeySpec(kid, key);
        try(PrintWriter writer = new PrintWriter(System.out)) {
            SpxpCryptoToolsV04.getSymmetricJWK(keySpec).write(writer, 4, 0);
        }
    }
    
    public void genroundkey(String[] args) throws Exception {
        if(args.length != 1) {
            System.out.println("Error: Command 'gensymkey' does not take any options");
            return;
        }
        byte[] key = SpxpCryptoToolsV04.generateSymmetricKey(256);
        String kid = SpxpCryptoToolsV04.generateRandomKeyId(KeyIdSize.LONG)+"."+SpxpCryptoToolsV04.generateRandomKeyId(KeyIdSize.SHORT);
        SpxpSymmetricKeySpec keySpec = new SpxpSymmetricKeySpec(kid, key);
        try(PrintWriter writer = new PrintWriter(System.out)) {
            SpxpCryptoToolsV04.getSymmetricJWK(keySpec).write(writer, 4, 0);
        }
    }
    
    public void encryptsymcompact(String[] args) throws Exception {
        if(args.length != 3) {
            System.out.println("Error: Invalid number of options for command 'encryptsymcompact'");
            return;
        }
        String payload = (new String(Files.readAllBytes(Paths.get(args[1])), StandardCharsets.UTF_8));
        JSONObject symJwkObj = new JSONObject(new String(Files.readAllBytes(Paths.get(args[2])), StandardCharsets.UTF_8));
        SpxpSymmetricKeySpec keySpec = SpxpCryptoToolsV04.getSymmetricKeySpec(symJwkObj);
        System.out.println(SpxpCryptoToolsV04.encryptSymmetricCompact(payload, keySpec));
    }
    
    public void decryptsymcompact(String[] args) throws Exception {
        if( (args.length < 2) || (args.length >  3) ) {
            System.out.println("Error: Invalid number of options for command 'decryptsymcompact'");
            return;
        }
        String ciphertext = (new String(Files.readAllBytes(Paths.get(args[1])), StandardCharsets.UTF_8));
        JSONObject symJwkObj = args.length > 2 ? new JSONObject(new String(Files.readAllBytes(Paths.get(args[2])), StandardCharsets.UTF_8)) : null;
        final SpxpSymmetricKeySpec keySpec = symJwkObj != null ? SpxpCryptoToolsV04.getSymmetricKeySpec(symJwkObj) : null;
        System.out.println(SpxpCryptoToolsV04.decryptSymmetricCompact(ciphertext, new SpxpKeyProvider() {
            @Override
            public SecretKey getKey(String keyId) throws SpxpCryptoNoSuchKeyException {
                if(keySpec == null) {
                    System.out.println(keyId);
                    return null;
                }
                if(!keyId.equals(keySpec.getKeyId())) {
                    throw new SpxpCryptoNoSuchKeyException("Expected key with ID '"+keyId+"' bot got key id '"+keySpec.getKeyId()+"'");
                }
                return new SecretKeySpec(keySpec.getSymmetricKey(), "AES");
            }
        }));
    }
    
    public void encryptsymjson(String[] args) throws Exception {
        if(args.length != 3) {
            System.out.println("Error: Invalid number of options for command 'encryptsymjson'");
            return;
        }
        String payload = (new String(Files.readAllBytes(Paths.get(args[1])), StandardCharsets.UTF_8));
        List<SpxpSymmetricKeySpec> keys = new LinkedList<>();
        for(String s : Arrays.asList(args[2].split(","))) {
            JSONObject symJwkObj = new JSONObject(new String(Files.readAllBytes(Paths.get(s)), StandardCharsets.UTF_8));
            SpxpSymmetricKeySpec keySpec = SpxpCryptoToolsV04.getSymmetricKeySpec(symJwkObj);
            keys.add(keySpec);
        }
        JSONObject obj = new JSONObject(SpxpCryptoToolsV04.encryptSymmetricJson(payload, keys));
        try(PrintWriter writer = new PrintWriter(System.out)) {
            obj.write(writer, 4, 0);
        }
    }
    
    public void decryptsymjson(String[] args) throws Exception {
        if( (args.length < 2) || (args.length >  3) ) {
            System.out.println("Error: Invalid number of options for command 'decryptsymjson'");
            return;
        }
        String ciphertext = (new String(Files.readAllBytes(Paths.get(args[1])), StandardCharsets.UTF_8));
        JSONObject symJwkObj = args.length > 2 ? new JSONObject(new String(Files.readAllBytes(Paths.get(args[2])), StandardCharsets.UTF_8)) : null;
        final SpxpSymmetricKeySpec keySpec = symJwkObj != null ? SpxpCryptoToolsV04.getSymmetricKeySpec(symJwkObj) : null;
        System.out.println(SpxpCryptoToolsV04.decryptSymmetricJson(ciphertext, new SpxpKeyProvider() {
            @Override
            public SecretKey getKey(String keyId) throws SpxpCryptoNoSuchKeyException {
                if(keySpec == null) {
                    System.out.println(keyId);
                    return null;
                }
                if(!keyId.equals(keySpec.getKeyId())) {
                    throw new SpxpCryptoNoSuchKeyException("Expected key with ID '"+keyId+"' bot got key id '"+keySpec.getKeyId()+"'");
                }
                return new SecretKeySpec(keySpec.getSymmetricKey(), "AES");
            }
        }));
    }
    
    public void encryptresource(String[] args) throws Exception {
        if( (args.length < 3) || (args.length > 4) ) {
            System.out.println("Error: Invalid number of options for command 'encryptresource'");
            return;
        }
        FileInputStream src = new FileInputStream(new File(args[1]));
        FileOutputStream dest = new FileOutputStream(new File(args[2]));
        String uri = args.length > 3 ? args[3] : null;
        JSONObject obj = new JSONObject(SpxpCryptoToolsV04.encryptResource(src, dest, uri));
        try(PrintWriter writer = new PrintWriter(System.out)) {
            obj.write(writer, 4, 0);
        }
    }
    
    public void decryptresource(String[] args) throws Exception {
        if(args.length != 4) {
            System.out.println("Error: Invalid number of options for command 'decryptresource'");
            return;
        }
        String resJson = new String(Files.readAllBytes(Paths.get(args[2])), StandardCharsets.UTF_8);
        try(FileInputStream src = new FileInputStream(new File(args[1]))) {
            try(FileOutputStream dest = new FileOutputStream(new File(args[3]))) {
                SpxpCryptoToolsV04.decryptResource(src, dest, resJson);
            }
        }
    }
    
    public void genconnectkeypair(String[] args) throws Exception {
        if(args.length != 1) {
            System.out.println("Error: Command 'genconnectkeypair' does not take any options");
            return;
        }
        SpxpConnectKeyPair keypair = SpxpCryptoToolsV04.generateConnectKeyPair();
        try(PrintWriter writer = new PrintWriter(System.out)) {
            SpxpCryptoToolsV04.getKeypairJWK(keypair).write(writer, 4, 0);
        }
    }
    
    public void extractconnectpublic(String[] args) throws Exception {
        if(args.length != 2) {
            System.out.println("Error: Invalid number of options for command 'extractconnectpublic'");
            return;
        }
        JSONObject keypairJwkObj = new JSONObject(new String(Files.readAllBytes(Paths.get(args[1])), StandardCharsets.UTF_8));
        SpxpConnectKeyPair keypair = SpxpCryptoToolsV04.getConnectKeyPair(keypairJwkObj);
        JSONObject publicJwk = SpxpCryptoToolsV04.getPublicJWK(keypair.extractConnectPublicKey());
        try(PrintWriter writer = new PrintWriter(System.out)) {
            publicJwk.write(writer, 4, 0);
        }
    }
    
    public void encryptasymjson(String[] args) throws Exception {
        if(args.length != 3) {
            System.out.println("Error: Invalid number of options for command 'encryptasymjson'");
            return;
        }
        String payload = (new String(Files.readAllBytes(Paths.get(args[1])), StandardCharsets.UTF_8));
        JSONObject asymJwkObj = new JSONObject(new String(Files.readAllBytes(Paths.get(args[2])), StandardCharsets.UTF_8));
        SpxpConnectPublicKey publicKey = SpxpCryptoToolsV04.getConnectPublicKey(asymJwkObj);
        JSONObject obj = new JSONObject(SpxpCryptoToolsV04.encryptAsymmetricJson(payload, publicKey));
        try(PrintWriter writer = new PrintWriter(System.out)) {
            obj.write(writer, 4, 0);
        }
    }
    
    public void decryptasymjson(String[] args) throws Exception {
        if(args.length != 3) {
            System.out.println("Error: Invalid number of options for command 'decryptasymjson'");
            return;
        }
        String ciphertext = (new String(Files.readAllBytes(Paths.get(args[1])), StandardCharsets.UTF_8));
        JSONObject asymJwkObj = new JSONObject(new String(Files.readAllBytes(Paths.get(args[2])), StandardCharsets.UTF_8));
        SpxpConnectKeyPair keyPair = SpxpCryptoToolsV04.getConnectKeyPair(asymJwkObj);
        System.out.println(SpxpCryptoToolsV04.decryptAsymmetricJson(ciphertext, keyPair));
    }

}
