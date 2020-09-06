# Comprehend Examples from SPXP Spec V 0.3
The [SPXP Specification V 0.3](https://github.com/spxp/spxp-specs/blob/master/SpxpProfileSpec-V03.md)
contains multiple examples using digital signatures and encryption. You can use
the `SpxpCryptoTool` commandline tool to comprehend these examples by signing
and verifying these examples yourself.

You can find all relevant examples as individual json files in the
[./spec-examples](./spec-examples/) folder.

Play around with the verification command to see how the signatures are resilient
against insignificant whitespace, character encoding and the ordering of members.

## Used Profile Keypairs and Symmetric Keys
The keypairs used for the examples are listed in [Appendix A](https://github.com/spxp/spxp-specs/blob/master/SpxpProfileSpec-V03.md#appendix-a-private-keys-used-in-examples)
of the spec. You can find these keypairs as json files here:  
[Profile keypair of “Crypto Alice”](./spec-examples/alice-profile-keypair.json)  
[Profile keypair of “Crypto Bob”](./spec-examples/bob-profile-keypair.json)  
[Symmetric AES key “ABCD.1234” used in chapter 10.1](./spec-examples/symkey-ABCD1234.json)  
[Connect keypair of “Crypto Alice”](./spec-examples/alice-connect-keypair.json)  
[Connect keypair of “Crypto Bob”](./spec-examples/bob-connect-keypair.json)  
[Ephemeral Connection Establishment Key between “Crypto Alice” and “Crypto Bob” used in chapter 15](./spec-examples/alice-bob-ece-key.json)  

## Signed Profile of “Crypto Alice” in Chapter 9.1
You can sign this profile yourself as follows:
```
$ SpxpCryptoTool sign ./spec-examples/alice-short-profile-unsigned.json ./spec-examples/alice-profile-keypair.json
```
And to verify the signature:
```
$ SpxpCryptoTool verify ./spec-examples/alice-short-profile-signed.json ./spec-examples/alice-profile-keypair.json
```

## Certificate for “Crypto Bob” signed by “Crypto Alice” in Chapter 9.2
You can sign this certificate yourself as follows:
```
$ SpxpCryptoTool sign ./spec-examples/bob-cert-unsigned.json ./spec-examples/alice-profile-keypair.json
```
And to verify the signature:
```
$ SpxpCryptoTool verify ./spec-examples/bob-cert-signed.json ./spec-examples/alice-profile-keypair.json
```

## Individual Posts in Chapter 9
As [specified in 9.1](https://github.com/spxp/spxp-specs/blob/master/SpxpProfileSpec-V03.md#91-signing-and-encrypting-posts),
posts are signed individually. Each post in the example in chapter 9 can be signed as follows:
```
$ SpxpCryptoTool sign ./spec-examples/alice-post1-unsigned.json ./spec-examples/alice-profile-keypair.json
$ SpxpCryptoTool sign ./spec-examples/alice-post2-unsigned.json ./spec-examples/alice-profile-keypair.json
$ SpxpCryptoTool sign ./spec-examples/alice-post3-unsigned.json ./spec-examples/bob-profile-keypair.json
```
Please note that post 3 is signed by Bob. You need to later replace the String member `key` in
this signature with Bob's certificate.  
To verify these signatures:
```
$ SpxpCryptoTool verify ./spec-examples/alice-post1-signed.json ./spec-examples/alice-profile-keypair.json post
$ SpxpCryptoTool verify ./spec-examples/alice-post2-signed.json ./spec-examples/alice-profile-keypair.json post
$ SpxpCryptoTool verify ./spec-examples/alice-post3-signed.json ./spec-examples/alice-profile-keypair.json post
```
Please note that we verify post 3 also against Alice's signature instead of Bob's.
The additional parameter `post` tells the verification tool to also accept
certificates that grant the “post” permission and are ultimately signed by Alice.

## Signed and Encrypted private data in Chapter 10.5
In this example, we limit the  visibility of Alice's website to the audience
which has access to the symmetric reader key “ABCD.1234”.  
First, we have prepared a small json object that only contains the website [here](./spec-examples/alice-privatewebsite-unsigned.json)
```json
{
    "website": "https://example.com"
}
```
This object then needs to be signed:
```
$ SpxpCryptoTool sign ./spec-examples/alice-privatewebsite-unsigned.json ./spec-examples/alice-profile-keypair.json
```
resulting in
```json
{
    "website": "https://example.com",
    "signature": {
        "key": "C8xSIBPKRTcXxFix",
        "sig": "nEd-NXLlBDjcmCJHhzn9CaVYuRBsG4SDDgdHql85xdGtgb_bql2SnZh2oeMf-dk_g-YhT3uRyZHZRTriUEnCBA"
    }
}
```
Next, insignificant whitespace is removed and [this object](./spec-examples/alice-privatewebsite-signed-condensed.json) is then encrypted with the key “ABCD.1234”:
```
$ SpxpCryptoTool encryptsymcompact ./spec-examples/alice-privatewebsite-signed-condensed.json ./spec-examples/symkey-ABCD1234.json
```
This object is then embedded into the profile root object:
```json
{
    "ver" : "0.3",
    "name" : "Crypto Alice",
    "private" : [
        "eyJraWQiOiJBQkNELjEyMzQiLCJlbmMiOiJBMjU2R0NNIiwiYWxnIjoiZGlyIn0..SfT0skkIjzru5ylj.eDnedk0RIWIk6m6YQwwwzeZg7q1GH87HW5wUqKJcWRCNZHgI5hCUmDATDzW_eeUsQp8mkkQ4fpqlrBmX5lwv3vsdmgL4r-18GVhxGhbq6GxtbR8YE2MPTxJUZ3D56QHld8ZkOV5pOu7h5BhO9f2zKNEB2j0xbNEqgr259_T983VEoqqp0Rrze1qgmshMQLkZsUrbHsnDaPsp28bhRb_zMInvhBNfa6M.zYtiVMmo-TC_BhJDGPwoHA"
    ],
    "profilePhoto" : " https://images.example.com/alice.jpg",
    "friendsEndpoint" : "friends/alice",
    "postsEndpoint" : "posts/alice",
    "publicKey": {
        "kid": "C8xSIBPKRTcXxFix",
        "kty": "OKP",
        "crv": "Ed25519",
        "x": "skpRppgAopeYo9MWRdExl26rGA_z701tMoiuJ-jIjU8"
    }
}
```
And then the entire profile root object gets signed:
```
$ SpxpCryptoTool sign ./spec-examples/alice-private-profile-unsigned.json ./spec-examples/alice-profile-keypair.json
```
Resulting in
```json
{
    "ver" : "0.3",
    "name" : "Crypto Alice",
    "private" : [
        "eyJraWQiOiJBQkNELjEyMzQiLCJlbmMiOiJBMjU2R0NNIiwiYWxnIjoiZGlyIn0..SfT0skkIjzru5ylj.eDnedk0RIWIk6m6YQwwwzeZg7q1GH87HW5wUqKJcWRCNZHgI5hCUmDATDzW_eeUsQp8mkkQ4fpqlrBmX5lwv3vsdmgL4r-18GVhxGhbq6GxtbR8YE2MPTxJUZ3D56QHld8ZkOV5pOu7h5BhO9f2zKNEB2j0xbNEqgr259_T983VEoqqp0Rrze1qgmshMQLkZsUrbHsnDaPsp28bhRb_zMInvhBNfa6M.zYtiVMmo-TC_BhJDGPwoHA"
    ],
    "profilePhoto" : " https://images.example.com/alice.jpg",
    "friendsEndpoint" : "friends/alice",
    "postsEndpoint" : "posts/alice",
    "publicKey": {
        "kid": "C8xSIBPKRTcXxFix",
        "kty": "OKP",
        "crv": "Ed25519",
        "x": "skpRppgAopeYo9MWRdExl26rGA_z701tMoiuJ-jIjU8"
    },
    "signature": {
        "key": "C8xSIBPKRTcXxFix",
        "sig": "kS-ByECFG-QWN4M5XNpAkCsvpbpX7KU_JrOzLrPHFdoP1YBaP4TKqa-tAz4yqr3BWqMky0SN_fJcMv2VTAE_Aw"
    }
}
```
To verify this entire profile root object:
```
$ SpxpCryptoTool verify ./spec-examples/alice-private-profile-signed.json ./spec-examples/alice-profile-keypair.json
Signature valid.
```
You can remove the `private` array here or add additional entries and check that
the signature remains valid. This allows the server to hide private data items if
the client does not have the required reader keys.  
To print the key that is required to decrypt this private item, you can use the `decryptsymcompact` command without a key:
```
$ SpxpCryptoTool decryptsymcompact ./spec-examples/alice-privatewebsite-encrypted.json
ABCD.1234
```
To finally reveal the information in this private item, decrypt it with the reader key “ABCD.1234”:
```
$ SpxpCryptoTool decryptsymcompact ./spec-examples/alice-privatewebsite-encrypted.json ./spec-examples/symkey-ABCD1234.json
{"website":"https://example.com","signature":{"key":"C8xSIBPKRTcXxFix","sig":"nEd-NXLlBDjcmCJHhzn9CaVYuRBsG4SDDgdHql85xdGtgb_bql2SnZh2oeMf-dk_g-YhT3uRyZHZRTriUEnCBA"}}
```
Remember to check the signature of this fragment before merging it into the profile:
```
$ SpxpCryptoTool verify ./spec-examples/alice-privatewebsite-signed-condensed.json ./spec-examples/alice-profile-keypair.json
Signature valid.
```

## Signed Profile of “Crypto Alice” in Chapter 15.2
You can sign the profile in chapter 15.2 yourself as follows:
```
$ SpxpCryptoTool sign ./spec-examples/alice-profile-with-connect-unsigned.json ./spec-examples/alice-profile-keypair.json
```
And to verify the signature:
```
$ SpxpCryptoTool verify ./spec-examples/alice-profile-with-connect-signed.json ./spec-examples/alice-profile-keypair.json
```

## Signed Connection Package of “Crypto Alice” for “Crypto Bob” in Chapter 15.3
You can sign the connection package created by “Crypto Alice” for “Crypto Bob” yourself as follows:
```
$ SpxpCryptoTool sign ./spec-examples/alice-connection_package-unsigned.json ./spec-examples/alice-profile-keypair.json
```
And to verify the signature:
```
$ SpxpCryptoTool verify ./spec-examples/alice-connection_package-signed.json ./spec-examples/alice-profile-keypair.json
```

## Signed Connection Request of “Crypto Alice” for “Crypto Bob” in Chapter 15.5
You can sign the connection request from “Crypto Alice” to “Crypto Bob” yourself as follows:
```
$ SpxpCryptoTool sign ./spec-examples/alice-connection_request-unsigned.json ./spec-examples/alice-profile-keypair.json
```
And to verify the signature:
```
$ SpxpCryptoTool verify ./spec-examples/alice-connection_request-signed.json ./spec-examples/alice-profile-keypair.json
```

## Encrypted Connection Request of “Crypto Alice” for “Crypto Bob” in Chapter 15.7
The `connection_request` request sent in chapter 15.7 contains the encrypted
connection message Alice has created for Bob. This message is encrypted with an
asymmetric encryption scheme, so that only Bob can decrypt it with his private
key, while everybody can encrypt messages.  
You can decrypt the `msg` with Bob's private connection key stored in his
connection keypair as follows:
```
$ SpxpCryptoTool decryptasymjson ./spec-examples/alice-connection_request-signed-encrypted.json ./spec-examples/bob-connect-keypair.json
```
You can also encrypt this message yourself with Bob's public key, which is
announced in the `connect` object of his profile root document:
```
$ SpxpCryptoTool encryptasymjson ./spec-examples/alice-connection_request-signed.json ./spec-examples/bob-connect-publickey.json
```
(This will create a different ciphertext and tag as a new random IV is chosen
for each encryption)

## Encrypted Connection Package for “Crypto Alice” from “Crypto Bob” in Chapter 15.8
The `connection_accept` request sent in chapter 15.8 contains the encrypted
connection package Bob has created for Alice. You can decrypt the `package`
with the Ephemeral Connection Establishment key from the connect request message
as follows:
```
$ SpxpCryptoTool decryptsymjson ./spec-examples/bob-connection_package-signed-encrypted.json ./spec-examples/alice-bob-ece-key.json
```
You can then check the signature on this package as follows:
```
$ SpxpCryptoTool verify ./spec-examples/bob-connection_package-signed.json ./spec-examples/bob-profile-keypair.json
```
You can also sign and encrypt this package yourself as follows:
```
$ SpxpCryptoTool sign ./spec-examples/bob-connection_package-unsigned.json ./spec-examples/bob-profile-keypair.json
$ SpxpCryptoTool encryptsymjson ./spec-examples/bob-connection_package-signed.json ./spec-examples/alice-bob-ece-key.json
```
(This will create a different ciphertext and tag as a new random IV is chosen
for each encryption)

## Encrypted Connection Package for “Crypto Bob” from “Crypto Alice” in Chapter 15.8
The connection package Alice has created and signed in chapter 15.2 appears in
the server response in chapter 15.8 encrypted with the Ephemeral Connection
Establishment key. You can decrypt the `package` from the server response as
follows:
```
$ SpxpCryptoTool decryptsymjson ./spec-examples/alice-connection_package-signed-encrypted.json ./spec-examples/alice-bob-ece-key.json
```
You can also sign and encrypt this package yourself as follows:
```
$ SpxpCryptoTool encryptsymjson ./spec-examples/alice-connection_package-signed.json ./spec-examples/alice-bob-ece-key.json
```
(This will create a different ciphertext and tag as a new random IV is chosen
for each encryption)
