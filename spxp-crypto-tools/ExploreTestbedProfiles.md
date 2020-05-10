# Explore Testbed Profiles
We provide a set of automatically created profiles on http://textbed.spxp.org
which can be used as a testbed during the development of SPXP clients. They can
also serve as a reference for SPXP server implementations.

### List of available profiles
This site provides profiles for multiple versions. You can find a list of all
available profiles of version 0.3 on this page:  
http://testbed.spxp.org/0.3/  
This page contains links to the profile URIs of each profile as well as QR code
images of the profile URIs. When you click one of the links, it will open in the
browser.

### Exploring the profile root document
The first operation a SPXP client performs is to download the profile root
document. This document is returned by the SPXP server on a GET request to the
profile URI. On a command prompt, we can run:
```
$ curl http://testbed.spxp.org/0.3/angrybear287
{
    "ver": "0.3",
    "name": "Anthony Wells",
    "about": "Simplicity is not about making something without ornament, but rather about making something very complex, then slicing elements away, until you reveal the very essence.",
    "birthDayAndMonth": "20-02",
    "hometown": "http://testbed.spxp.org/0.3/place.tokyo",
    "profilePhoto": "images/m11.jpg",
    "friendsEndpoint": "friends/angrybear287",
    "postsEndpoint": "posts/_read-posts.php?profile=angrybear287",
    "keysEndpoint": "keys/_read-keys.php?profile=angrybear287",
    "publicKey": {
        "kid": "key-angrybear287",
        "kty": "OKP",
        "crv": "Ed25519",
        "x": "Q6_yDh5lM7S696QoM3GHUp673TCfLFtICmLxpcx-T-o"
    },
    "private": [
        "eyJraWQiOiJncnAtYW5ncnliZWFyMjg3LWZyaWVuZHMudWpsU0JXS3kiLCJlbmMiOiJBMjU2R0NNIiwiYWxnIjoiZGlyIn0..saz4yEhUNyJEdPhZ.34GTffZMOkFaDDTkoASlXD0TSmRYagqRv-yLKbiF5TxSk8j9vX4N6Evmwe97h5NMpx2lp21FnfZgRO0fcuRxBk3xkMnsuxgaI7tGPW1C7t958nN3HhUURGAe4N-WQxACbJIAPWy_mE0BMDCtuaHH2OhNCVO7Fr-xNScuWzQE0kTGyfdjEqtoB_RA-5VGlQ1VvQvWii6tKgo5jB-V4P_3_4TKI-A1AtJbJaOtiDEK9u6n9srRbWs2v-mKMNYoBgue0CHhnVakCw4XhDeDj_qSv9ngGABqDqUxNJZOLFF-I86ToHHNrMJq-NefiXUcMAxlOZbp3zFRZRGKNKPiEbE8J-Ifn46Ym95gaURPn4NcX5D_-A4Q.uesviBsqmfaByMpnxqm7MQ",
        "eyJraWQiOiJncnAtYW5ncnliZWFyMjg3LWZhbWlseS5JRWpBQ1lyMCIsImVuYyI6IkEyNTZHQ00iLCJhbGciOiJkaXIifQ..8Wm85tT8qctUHyOs.rXoLXHdjx_xj4RdzoHpVK9ftkXPwhtbqOspMMuuhx4xtHxVabnwIfqSjzJvx6WhziAKIRLtETGZEV-OwBU6x5wvJAv01p2FIvrYzERknjr1_Gn91vofazYqP5EK3HMHvejrT9l_7FEbi4t5bWASWwACGIhwfY_hMRz_m6xROI3w6MnN0Vk-e8JnFjecdsTEPA1lSGX7kleIsX5mdxnJTeiNZ9KuZ9ifxM7GyilGRlL0SUGuPuN2uzcF81iHhpyIrCvP4UbPFxOcQ0l5KXXI_rUkeaZ3Cg74._IRSZ5u-zKXcCORdV1sZAw"
    ],
    "signature": {
        "sig": "8IjlGLQnV_ZWlrqWhyMn6UZPzPiZOCkdTEMqWLCnX11_2MlDnY_IddecbnPDlc-IuGodPyBtMaRdmGh90SSfCw",
        "key": "key-angrybear287"
    }
}
```
The client then checks the protocol version in the `ver` member. In this case,
this profile is following version 0.3 of the SPXP specification.  
With version 0.3, the next step would be to verify the signature on this JSON
object. Let's safe this profile root object to a file:
```
$ curl http://testbed.spxp.org/0.3/angrybear287 > angrybear287-profile-root.json
```
If the SPXP client is seeing this profile for the first time, it does not yet
have the public key of the profile key pair. You can now either copy and paste
the `publicKey` object of this file into a new file, or use the
[jq](https://stedolan.github.io/jq/) command line tool to extract it:
```
$ cat angrybear287-profile-root.json | jq '.publicKey' > angrybear287-publickey.json
```
Now we can verify the signature of the profile root object:
```
$ SpxpCryptoTool verify angrybear287-profile-root.json angrybear287-publickey.json
Signature valid.
```
If the signature is broken, the entire profile must be rejected. If the client
detects a change in the publicKey, it has to warn the user and must treat
profiles signed with different key pairs as different profiles, even if they are
published under the same URI.

This profile root object contains all the information the profile “angrybear287”
is willing to share publicly. It contains the name, an about line or the
hometown among others.

### Exploring the profile's friends and posts
The profile root object contains relative URIs to the friends and to the posts
endpoints. In the above profile we find:
```
friends/angrybear287
posts/_read-posts.php?profile=angrybear287
```
which resolve relative to the profile URI to
```
http://testbed.spxp.org/0.3/friends/angrybear287
http://testbed.spxp.org/0.3/posts/_read-posts.php?profile=angrybear287
```
Let's curl the friends URI, verify the signature and then take a look at it:
```
$ curl http://testbed.spxp.org/0.3/friends/angrybear287 > angrybear287-friends.json
$ SpxpCryptoTool verify angrybear287-friends.json angrybear287-publickey.json
Signature valid.
$ cat angrybear287-friends.json
{
    "data": [
        "http://testbed.spxp.org/0.2/blackfish356",
        "http://testbed.spxp.org/0.2/orangewolf216",
        "http://testbed.spxp.org/0.2/yellowmeercat889",
        "http://testbed.spxp.org/0.2/greenrabbit943",
        "http://testbed.spxp.org/0.2/yellowrabbit476",
        "http://testbed.spxp.org/0.2/tinyelephant166",
        ...
    ],
    "private": [
        ...
    ]
}
```
The posts endpoint is slightly different, as each post is signed individually:
```
$ curl http://testbed.spxp.org/0.3/posts/_read-posts.php?profile=angrybear287 > angrybear287-posts-page1.json
$ cat angrybear287-posts-page1.json | jq '.data[2]' > angrybear287-temp-post.json
$ SpxpCryptoTool verify angrybear287-temp-post.json angrybear287-publickey.json post,impersonate
Signature valid.
$ cat angrybear287-temp-post.json
{
  "createts": "2019-12-02T02:26:34.878",
  "type": "text",
  "message": "A creepy green glow casts a cold light across the polo field sprawling in front of radiohead",
  "signature": {
    "sig": "xG9moI74f7_oQsFSlAJ22UD8n5mkcIHP1aZkp74qDMossNERiPFK9gXc2tvh6mlgF0PSgNSMVLMxFYnN9sVCCA",
    "key": {
      "publicKey": {
        "kid": "clXZ87xDJxinro2g",
        "kty": "OKP",
        "crv": "Ed25519",
        "x": "Uyu9aE9SXHKiKp42QT9XBTLJfUoyl71Qei3i09PJcR4"
      },
      "grant": [
        "impersonate",
        "post",
        "comment"
      ],
      "signature": {
        "sig": "agsLscsX2Tyyu8FklVdSkhzozhssdEQ0vvrFfTk8NwV3HEs4-xWjbQ_NP8lHTB9eGvnU9f13yyG1RdycnjKTDw",
        "key": {
          "publicKey": {
            "kid": "Z3ar--xalNWagaOo",
            "kty": "OKP",
            "crv": "Ed25519",
            "x": "DsskOJynFa4bgO-bQWbgNEx6WdU0o_iht3vllHubBLQ"
          },
          "grant": [
            "grant",
            "friends",
            "impersonate",
            "post",
            "comment"
          ],
          "signature": {
            "sig": "zW8v89wP-97pVb1kXmbHYLdPdP9CCyf0QVQCMgkSB7CIiYKs2wGiPBTMwWtOXGZOjXU1wV6Zy0PYjFeSFbAuCw",
            "key": "key-angrybear287"
          }
        }
      }
    }
  },
  "seqts": "2019-12-02T04:53:25.866"
}
```
This post has not been signed by the profile keypair directly, but by the
keypair `clXZ87xDJxinro2g` which brings a certificate chain certifying that
this keypair is allowed to publish posts in the name of this profile. The
additional parameter `post,impersonate` on the `SpxpCryptoTool` allows
certificate chains that grant at least the “post” and the “impersonate”
permission to the signing key pair.

### Reading private data
The profile root object has a `private` array with two elements. A SPXP server
would typically redact this information unless the client provides a `reader`
parameter conatining a list of reader keys.  
This testbed server does not perform this redaction to make these profiles
easier explorable.  
Let's extract the first element and investigate the required key:
```
$ cat angrybear287-profile-root.json | jq --raw-output '.private[0]' > angrybear287-profile-private0-encrypted.txt
$ SpxpCryptoTool decryptsymcompact angrybear287-profile-private0-encrypted.txt
grp-angrybear287-friends.ujlSBWKy
```
We can now assume we are one of angrybear287's friends. From the firends list
above, we simply pick the first entry: “blackfish356”.  
The private data that each profile holds on the device is also available on this
testbed. Let's find out what “blackfish356” has stored for the connection to
“angrybear287”:
```
$ curl http://testbed.spxp.org/0.3/private_profile_data/blackfish356.json | jq '.connections | .[] | select(.profileUri == "http://testbed.spxp.org/0.3/angrybear287") | .grantedReaderKey' > angrybear287-readerkey-for-blackfish356.json
$ cat angrybear287-readerkey-for-blackfish356.json
{
  "kid": "angrybear287-readerkey-for-blackfish356",
  "kty": "oct",
  "alg": "A256GCM",
  "k": "lBRFZUwHdplZQ5TcN4d590GOXhfg6MLf95oKDAv_Xb8"
}
```
With this information, we can now ask the keys endpoint to give us all
intermediate keys so  that we are able to finally read the private data:
```
$ curl http://testbed.spxp.org/0.3/keys/_read-keys.php?profile=angrybear287&reader=angrybear287-readerkey-for-blackfish356&request=grp-angrybear287-friends.ujlSBWKy > angrybear287-keys-response.json
$ cat angrybear287-keys-response.json
{
    "angrybear287-readerkey-for-blackfish356": {
        "grp-angrybear287-family-virt_0": {
            "thyxsDtG": "eyJraWQiOiJhbmdyeWJlYXIyODctcmVhZGVya2V5LWZvci1ibGFja2Zpc2gzNTYiLCJlbmMiOiJBMjU2R0NNIiwiYWxnIjoiZGlyIn0..QQE27zDIYbqzyKqR.LQF7oLoGHpnPrT2hKDFbBOBzxY8FspPtDZDSeb00do0AO9QkFs3Cj4fA4_JzLZ3XNywcyC6xGnrUirllk24imWNesH03ob55uKl05VO1TJPgo3wMd3p8sUvi_LhVVsyivNbCaAcgKOeWBZLD1TafQ8Mo9J_7HrgaWggKGTEfUw.LomtSMj51JZykyeHVQj2MA"
        }
    },
    "grp-angrybear287-family-virt_0": {
        "grp-angrybear287-family": {
            "fKQPBoda": "eyJraWQiOiJncnAtYW5ncnliZWFyMjg3LWZhbWlseS12aXJ0XzAudGh5eHNEdEciLCJlbmMiOiJBMjU2R0NNIiwiYWxnIjoiZGlyIn0..jwNbcZ2Qc_NdTWhR.ez4cqQED9joUYRgoW7zouEIikD94ZHlIMHMDzONTnLsO8fsj6iLPhyo-RCmigvUOoDbX1WU390id9CAdWz2TrgSwMeysIldAVUSNzThfba9IAKsHO6rg2BqRhL7D2XF9b51tXrCaJkrPMxDLKpEinyNCaatHtssh.KOTpFDqJxtrEJibd5Quwng"
        }
    },
    "grp-angrybear287-family": {
        "grp-angrybear287-friends": {
            "ujlSBWKy": "eyJraWQiOiJncnAtYW5ncnliZWFyMjg3LWZhbWlseS5mS1FQQm9kYSIsImVuYyI6IkEyNTZHQ00iLCJhbGciOiJkaXIifQ..OY1jc2vktA7Au1lB.Vb0T3Z1REtQBKUI2kvZR54NApf6tr0nrgmEkOzOz6jA3jDvuxo_5GEYCO17uZz4n-hdQyuv7ZMvpYI5uGxgostrRfLr5AcfKnzT8paL7ZAFvQodS97KnX-Vgn-hRXG7q3oSDJu53ZkjLB2hot-qkpKEAe0Q4EdA_GA.-YvjG_pJfUZHOlhHtvTNBQ"
        }
    }
}
```
The first object is telling us that the reader key `angrybear287-readerkey-for-blackfish356`
can decrypt round keys for the key group `grp-angrybear287-family-virt_0`.  
Let's decrypt the first key:
```
$ cat angrybear287-keys-response.json | jq --raw-output '.["angrybear287-readerkey-for-blackfish356"]["grp-angrybear287-family-virt_0"]["thyxsDtG"]' > grp-angrybear287-family-virt_0_thyxsDtG_encrypted.txt
$ SpxpCryptoTool decryptsymcompact grp-angrybear287-family-virt_0_thyxsDtG_encrypted.txt angrybear287-readerkey-for-blackfish356.json > grp-angrybear287-family-virt_0_thyxsDtG.json
```
Next, we can use this key to decrypt the round key in the group `grp-angrybear287-family`:
```
$ cat angrybear287-keys-response.json | jq --raw-output '.["grp-angrybear287-family-virt_0"]["grp-angrybear287-family"]["fKQPBoda"]' > grp-angrybear287-family_fKQPBoda_encrypted.txt
$ SpxpCryptoTool decryptsymcompact grp-angrybear287-family_fKQPBoda_encrypted.txt grp-angrybear287-family-virt_0_thyxsDtG.json > grp-angrybear287-family_fKQPBoda.json
```
And finally we can decrypt the key we need on the private data:
```
$ cat angrybear287-keys-response.json | jq --raw-output '.["grp-angrybear287-family"]["grp-angrybear287-friends"]["ujlSBWKy"]' > grp-angrybear287-friends_ujlSBWKy_encrypted.txt
$ SpxpCryptoTool decryptsymcompact grp-angrybear287-friends_ujlSBWKy_encrypted.txt grp-angrybear287-family_fKQPBoda.json > grp-angrybear287-friends_ujlSBWKy.json
```
Now we finally have the key `grp-angrybear287-friends.ujlSBWKy` to decrypt the
private data in the profile root object. We need to check the signature on each
decrypted private object:
```
$ SpxpCryptoTool decryptsymcompact angrybear287-profile-private0-encrypted.txt grp-angrybear287-friends_ujlSBWKy.json > angrybear287-profile-private0.json
$ SpxpCryptoTool verify angrybear287-profile-private0.json angrybear287-publickey.json
Signature valid.
$ cat angrybear287-profile-private0.json
{"gender":"male","website":"https://example.com","email":"anthony.wells@example.com","location":"http://testbed.spxp.org/0.3/place.wuerzburg","signature":{...}}
```
With the reader key `angrybear287-readerkey-for-blackfish356` we have now been
able to decrypt this additional private information. A client would merge this
information into the profile root object before interpreting and displaying it.