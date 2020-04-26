package org.spxp.crypto.tool;

import java.io.PrintWriter;

import org.json.JSONObject;
import org.spxp.crypto.SpxpCryptoToolsV03;
import org.spxp.crypto.SpxpProfileKeyPair;

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
		if(args[0].equals("gen-keypair")) {
			SpxpProfileKeyPair keypair = SpxpCryptoToolsV03.generateProfileKeyPair();
			try(PrintWriter writer = new PrintWriter(System.out)) {
				getOKPJWK(keypair).write(writer, 4, 0);
			}
			return;
		}
	}
	
	public void usage() {
		System.out.println("SPXP Crypto Tool V 0.3");
		System.out.println("Usage: SpxpCryptoTool <command> [<parameter>*]");
		System.out.println("Commands:");
		System.out.println("gen-keypair : generates a new profile keypair");
		//System.out.println("sign <jsonFileToSign> <KeyPairFile> : generates a new profile keypair");
	}
	
	private static JSONObject getOKPJWK(SpxpProfileKeyPair keyPair) {
		JSONObject jwkObj = new JSONObject();
		jwkObj.put("kid", keyPair.getKeyId());
		jwkObj.put("kty", "OKP");
		jwkObj.put("crv", "Ed25519");
		jwkObj.put("x", SpxpCryptoToolsV03.encodeBase64Url(keyPair.getPublicKey()));
		jwkObj.put("d", SpxpCryptoToolsV03.encodeBase64Url(keyPair.getSecretKey()));
		return jwkObj;
	}

}
