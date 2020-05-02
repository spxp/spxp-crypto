package org.spxp.crypto.tool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONObject;
import org.spxp.crypto.SpxpCryptoNoSuchKeyException;
import org.spxp.crypto.SpxpCryptoToolsV03;
import org.spxp.crypto.SpxpCryptoToolsV03.KeyIdSize;
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
		if(args[0].equals("genkeypair")) {
			genkeypair(args);
		} else if(args[0].equals("sign")) {
			sign(args);
		} else if(args[0].equals("verify")) {
			verify(args);
		} else if(args[0].equals("gensymkey")) {
			gensymkey(args);
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
		System.out.println("  genkeypair");
		System.out.println("      generates a new profile keypair");
		System.out.println("  sign <jsonFileToSign> <keyPairFile>");
		System.out.println("      signs the json object in <jsonFileToSign> with the secret key stored in <keyPairFile>");
		System.out.println("  verify <signedJsonFile> <publicKeyFile> [<requiredGrant>[,<requiredGrant>]*]");
		System.out.println("      verifies the signature in <signedJsonFile> with the public key stored in <publicKeyFile>");
		System.out.println("      if certificate chains are accepted, then <requiredGrant>s give a comma separated list of");
		System.out.println("      grant values that must be authorized from the root <publicKeyFile>");
		System.out.println("      exits with 0 status code on success and 1 on a broken signature");
		System.out.println("  gensymkey");
		System.out.println("      generates a new 256 bit AES key");
		System.out.println("  encryptsymcompact <fileToEncrypt> <symmetricKey>");
		System.out.println("      encrypts <fileToEncrypt> with the 256 bit AES key from <symmetricKey> in JWE compact serialization");
		System.out.println("  decryptsymcompact <fileToDecrypt> [<symmetricKey>]");
		System.out.println("      decrypts <fileToDecrypt> in JWE compact serialization with the 256 bit AES key from <symmetricKey>");
		System.out.println("      prints the key id of the required key if no <symmetricKey> is given");
		System.out.println("  encryptsymjson <fileToEncrypt> [<symmetricKey>[,<symmetricKey>]*]");
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
		System.out.println("  help");
		System.out.println("      print this screen");
		System.out.println();
		System.out.println("Find a detailed decription on https://github.com/spxp/spxp-crypto/spxp-crypto-tool/README.md");
	}
	
	public void genkeypair(String[] args) throws Exception {
		if(args.length != 1) {
			System.out.println("Error: Command 'genkeypair' does not take any options");
		}
		SpxpProfileKeyPair keypair = SpxpCryptoToolsV03.generateProfileKeyPair();
		try(PrintWriter writer = new PrintWriter(System.out)) {
			SpxpCryptoToolsV03.getKeypairJWK(keypair).write(writer, 4, 0);
		}
	}
	
	public void sign(String[] args) throws Exception {
		if(args.length != 3) {
			System.out.println("Error: Invalid number of options for command 'sign'");
		}
		JSONObject objToSign = new JSONObject(new String(Files.readAllBytes(Paths.get(args[1])), StandardCharsets.UTF_8));
		JSONObject keypairJwkObj = new JSONObject(new String(Files.readAllBytes(Paths.get(args[2])), StandardCharsets.UTF_8));
		SpxpProfileKeyPair keypair = SpxpCryptoToolsV03.getProfileKeyPair(keypairJwkObj);
		SpxpCryptoToolsV03.signObject(objToSign, keypair);
		try(PrintWriter writer = new PrintWriter(System.out)) {
			objToSign.write(writer, 4, 0);
		}
	}
	
	public void verify(String[] args) throws Exception {
		if(args.length < 3 || args.length > 4) {
			System.out.println("Error: Invalid number of options for command 'verify'");
		}
		JSONObject signedObject = new JSONObject(new String(Files.readAllBytes(Paths.get(args[1])), StandardCharsets.UTF_8));
		JSONObject publicJwkObj = new JSONObject(new String(Files.readAllBytes(Paths.get(args[2])), StandardCharsets.UTF_8));
		List<String> requiredGrants = args.length > 3 ? Arrays.asList(args[3].split(",")) : null;
		SpxpProfilePublicKey publicKey = SpxpCryptoToolsV03.getProfilePublicKey(publicJwkObj);
		if(SpxpCryptoToolsV03.verifySignature(signedObject, publicKey, requiredGrants)) {
			System.out.println("Signature valid.");
		} else {
			System.out.println("BROKEN Signature.");
		}
	}
	
	public void gensymkey(String[] args) throws Exception {
		if(args.length != 1) {
			System.out.println("Error: Command 'gensymkey' does not take any options");
		}
		byte[] key = SpxpCryptoToolsV03.generateSymmetricKey(256);
		String kid = SpxpCryptoToolsV03.generateRandomKeyId(KeyIdSize.LONG);
		SpxpSymmetricKeySpec keySpec = new SpxpSymmetricKeySpec(kid, key);
		try(PrintWriter writer = new PrintWriter(System.out)) {
			SpxpCryptoToolsV03.getSymmetricJWK(keySpec).write(writer, 4, 0);
		}
	}
	
	public void encryptsymcompact(String[] args) throws Exception {
		if(args.length != 3) {
			System.out.println("Error: Invalid number of options for command 'encryptsymcompact'");
		}
		String payload = (new String(Files.readAllBytes(Paths.get(args[1])), StandardCharsets.UTF_8));
		JSONObject symJwkObj = new JSONObject(new String(Files.readAllBytes(Paths.get(args[2])), StandardCharsets.UTF_8));
		SpxpSymmetricKeySpec keySpec = SpxpCryptoToolsV03.getSymmetricKeySpec(symJwkObj);
		System.out.println(SpxpCryptoToolsV03.encryptSymmetricCompact(payload, keySpec));
	}
	
	public void decryptsymcompact(String[] args) throws Exception {
		if( (args.length < 2) || (args.length >  3) ) {
			System.out.println("Error: Invalid number of options for command 'decryptsymcompact'");
		}
		String ciphertext = (new String(Files.readAllBytes(Paths.get(args[1])), StandardCharsets.UTF_8));
		JSONObject symJwkObj = args.length > 2 ? new JSONObject(new String(Files.readAllBytes(Paths.get(args[2])), StandardCharsets.UTF_8)) : null;
		final SpxpSymmetricKeySpec keySpec = symJwkObj != null ? SpxpCryptoToolsV03.getSymmetricKeySpec(symJwkObj) : null;
		System.out.println(SpxpCryptoToolsV03.decryptSymmetricCompact(ciphertext, new SpxpKeyProvider() {
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
		}
		String payload = (new String(Files.readAllBytes(Paths.get(args[1])), StandardCharsets.UTF_8));
		List<SpxpSymmetricKeySpec> keys = new LinkedList<>();
		for(String s : Arrays.asList(args[2].split(","))) {
			JSONObject symJwkObj = new JSONObject(new String(Files.readAllBytes(Paths.get(s)), StandardCharsets.UTF_8));
			SpxpSymmetricKeySpec keySpec = SpxpCryptoToolsV03.getSymmetricKeySpec(symJwkObj);
			keys.add(keySpec);
		}
		JSONObject obj = new JSONObject(SpxpCryptoToolsV03.encryptSymmetricJson(payload, keys));
		try(PrintWriter writer = new PrintWriter(System.out)) {
			obj.write(writer, 4, 0);
		}
	}
	
	public void decryptsymjson(String[] args) throws Exception {
		if( (args.length < 2) || (args.length >  3) ) {
			System.out.println("Error: Invalid number of options for command 'decryptsymjson'");
		}
		String ciphertext = (new String(Files.readAllBytes(Paths.get(args[1])), StandardCharsets.UTF_8));
		JSONObject symJwkObj = args.length > 2 ? new JSONObject(new String(Files.readAllBytes(Paths.get(args[2])), StandardCharsets.UTF_8)) : null;
		final SpxpSymmetricKeySpec keySpec = symJwkObj != null ? SpxpCryptoToolsV03.getSymmetricKeySpec(symJwkObj) : null;
		System.out.println(SpxpCryptoToolsV03.decryptSymmetricJson(ciphertext, new SpxpKeyProvider() {
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
		}
		FileInputStream src = new FileInputStream(new File(args[0]));
		FileOutputStream dest = new FileOutputStream(new File(args[1]));
		String uri = args.length > 3 ? args[2] : null;
		JSONObject obj = new JSONObject(SpxpCryptoToolsV03.encryptResource(src, dest, uri));
		try(PrintWriter writer = new PrintWriter(System.out)) {
			obj.write(writer, 4, 0);
		}
	}
	
	public void decryptresource(String[] args) throws Exception {
		if(args.length != 4) {
			System.out.println("Error: Invalid number of options for command 'decryptresource'");
		}
		String resJson = new String(Files.readAllBytes(Paths.get(args[1])), StandardCharsets.UTF_8);
		try(FileInputStream src = new FileInputStream(new File(args[0]))) {
			try(FileOutputStream dest = new FileOutputStream(new File(args[2]))) {
				SpxpCryptoToolsV03.decryptResource(src, dest, resJson);
			}
		}
	}

}
