# SPXP Crypto Tools
This project provides a commandline application for the signature and encryption
operations in SPXP.  

## Building
This tool is built as part of the parent multi-module maven build. It can also
be built individually with the usual maven commands.

## Using
We provide a `SpxpCryptoTool` batch file and shell script. These point to the
jar file in the target directory. To use it outside of this build, you need to
adopt these files slightly.

## Example usage
This tool can be used to [comprehend the examples from the SPXP Spec](./ComprehendSpecExamples.md).  
We also have a page showing how to [manually create a SPXP profile](./ManualProfileCreation.md).

## Command Reference
This tool provides these commands:

`$ SpxpCryptoTool genkeypair`  
Generates a new profile keypair and prints it as JWK object to standard out.

`$ SpxpCryptoTool sign <jsonFileToSign> <keyPairFile>`  
Signs the JSON object in the file `<jsonFileToSign>` with the secret key stored
in the file `<keyPairFile>` and writes the resulting object to standard out.

`$ SpxpCryptoTool verify <signedJsonFile> <publicKeyFile> [<requiredGrant>[,<requiredGrant>]*]`  
Verifies the signature on the JSON object stored in `<signedJsonFile>` with the
public key stored in `<publicKeyFile>`.  
If the object must be signed by a profile key directly, like a profile root
object, no 4th must be given.  
If the object may also be signed by a different keypair which brings a
certificate (chain), then a 4th parameter gives a comma separated list of
permissions that the certificate must grant to the signing key. In this case,
the certificate chain must ultimately be signed by the profile keypair given
as `<publicKeyFile>`.  
Exits with 0 status code on success and 1 on a broken signature.

`$ SpxpCryptoTool gensymkey`  
Generates a new 256 bit AES key and prints it as JWK object to standard out.

`$ SpxpCryptoTool encryptsymcompact <fileToEncrypt> <symmetricKey>`  
Encrypts `<fileToEncrypt>` with the 256 bit AES key from `<symmetricKey>` and
prints the result as JWE object in compact serialization to standard out.  
Note: Can only be used on text files in UTF-8 encoding.

`$ SpxpCryptoTool decryptsymcompact <fileToDecrypt> [<symmetricKey>]`  
Decrypts the JWE object in compact serialization stored in `<fileToDecrypt>`
with the 256 bit AES key stored as JWK in `<symmetricKey>` and prints the
result to standard out.  
Prints the key id (kid) of the required key if no `<symmetricKey>` parameter is
given.

`$ SpxpCryptoTool encryptsymjson <fileToEncrypt> [<symmetricKey>[,<symmetricKey>]*]`  
Encrypts `<fileToEncrypt>` with multiple 256 bit AES keys each read as JWK from
a file in the comma separated list of `<symmetricKey>`s and prints the result
as JWE object in JSON serialization to standard out. Each of the given
symmetric keys can be used independently to decrypt the content.  
Note: Can only be used on text files in UTF-8 encoding.

`$ SpxpCryptoTool decryptsymjson <fileToDecrypt> <symmetricKey>`  
Decrypts the JWE object in JSON serialization stored in `<fileToDecrypt>` with
the 256 bit AES key stored as JWK in `<symmetricKey>` and prints the result to
standard out.  
Prints the list of all key ids (kid) that can be used to decrypt the content if
no `<symmetricKey>` parameter is given.

`$ SpxpCryptoTool encryptresource <inputFile> <outputFile> [<resourceUri>]`  
Encrypts the binary data in `<inputFile>` with a new random 256 bit AES key,
writes the encrypted data to `<outputFile>` and prints the JSON object
describing the encrypted resource to standard out.  
If an optional `<resourceUri>` is given, it is embedded into the resource
object.

`$ SpxpCryptoTool decryptresource <inputFile> <decryptionKeyFile> <outputFile>`  
Decrypts the binary data in `<inputFile>` with the key described by the
resource object in `<decryptionKeyFile>` and writes the decrypted binary data
to `<outputFile>`.

`$ SpxpCryptoTool help`  
Prints a help screen.
