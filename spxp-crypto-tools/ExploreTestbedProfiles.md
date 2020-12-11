# Explore Testbed Profiles
We provide a set of automatically created profiles on http://testbed.spxp.org
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
    "shortInfo": "Enjoying life...",
    "website": "https://example.com",
    "email": "anthony.wells@example.com",
    "birthYear": "1960",
    "location": {
        "uri": "http://testbed.spxp.org/0.3/place.london",
        "publicKey": {
            "kid": "ZBONfQw6IGNtirJP",
            "kty": "OKP",
            "crv": "Ed25519",
            "x": "zeuMGOI0PM469kKuxTPIcDfUFb7C15ZhjT1SNDXEXPs"
        }
    },
    "coordinates": {
        "latitude": "51.5063",
        "longitude": "-.1371"
    },
    "profilePhoto": {
        "iv": "7A8l_p-KenSN-gqM",
        "k": "wiiBtEJDTXfNJAGPM9ggrAk0hDLYtlZLnc__QAk1duo",
        "tag": "pk3JARRrVZsqoEUrQetF0g",
        "uri": "images_enc/angrybear287.encrypted"
    },
    "friendsEndpoint": "friends/angrybear287",
    "postsEndpoint": "posts/_read-posts.php?profile=angrybear287",
    "keysEndpoint": "keys/_read-keys.php?profile=angrybear287",
    "publicKey": {
        "kid": "key-angrybear287",
        "kty": "OKP",
        "crv": "Ed25519",
        "x": "Vgg41CueruHoBd0FwOXpHLJodH9KT36k8J3IOd5qTcg"
    },
    "private": ["eyJraWQiOiJncnAtYW5ncnliZWFyMjg3LWZyaWVuZHMuRnhrcWZIR3UiLCJlbmMiOiJBMjU2R0NNIiwiYWxnIjoiZGlyIn0..iBm1cu9l9KUQ54M_.yrRvIPldfGO2dMO3kYXbj_ffTclfWMn3LuSpoSsiCj-ju9mW4cg-BLRcV0yRUOp3VCbV_ngjmCCNl2gIldxOd4QkDDuvio_Z3N27ZtVMFCtb9mW27KC0n7P50dxvZdhLWvDuSXXJCOdc3PvdAZyGKqUzIN0SLh70Jx08VrzJc-JTbF9yBtniifhoxp9OasmuDtd8n5LbaiG6s0ywyq91IOMloQVWy3AjJtJTb0mbHTvzZug8KSlzwXGvIjZsV0HB3Svr-oSHVVsOhtVkqpfLTPneHJXZ8ycKoQVwnoNv9cM7UVD0kFnPQytyu-QbGljyiAh0iAztSSxEunBKDabcJvuxTsRt0j5cxH3F-5b1q81hcOYgDFgcQpLy1erEOTqtWxwCQzN4Jtoag261Ep5rzv9iVEiL5b3F2cx0HTbdGxFKQNHtnFPqDfuFpUkirbOlm-WWCbcVaNsOEv83dUqukH5fDoy1itfyW1VNtdYJk-1p3u8Xf6af6CsYKugzuFk-sPelss8mcsy18v2Mlw2FuqNsbkjsPQ-zOu18m8aVLAqPU1eZ5EEnT9aSKH-eRhOPCIHsE9KwClvqC_WFTSapiIeHgGh1OQeqRed2vS1k_ss2FJAi5KeRxWCfDncwtB2fYn3EOY4RVk5UITXpze-iN5R35ZFi__7JW0hbEpzYZZwRyDm72zcM8ywB8ycah2C9s5LagYYtSUJ45u-q7S5boX9p_Zw7RaP8HC9L7Jf_BWhsKh50OShM_9QDJuASkYNZMTaLI-xCCtByAmnBog4yoCNzLngoOpeIhXGJfpBZk4mRiVlfkMDuQE3NTQpp7dkuRzwnQxJiai4EL15mLwHqybsfzB5xJ4pdXFivN-xy7Ghld0KhWxnTOu1QgpgoKoVd11Q7-2NLZ88tVp9kUY_wGEGcVx6eCJVY8SQqPb3U-In23nQS-M0MwxbqYjqHmx3MvptviyaKmoY6TdA0UdGZcAuS544qncHKdpKl_pZ7ZEdYX1y0XzA-lg8u5rPlxAkNHCletI1PxqBCJYzx5AITltqELES0JogJEzA._y1ggxE6gaUayZDqebA-pw"],
    "signature": {
        "sig": "uvJ2TCCX4fLkGRvQEwfveOmpobXXcZOIR4sfOvJQgYhqEZtI9QQw2hNSAh_zndfsRllrNjRkJA6V7iFTWjrABQ",
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
        {
            "uri": "http://testbed.spxp.org/0.3/greenrabbit943",
            "publicKey": {
                "kid": "key-greenrabbit943",
                "kty": "OKP",
                "crv": "Ed25519",
                "x": "YX_7SvvFUGiMMsCdPhnG-DvK7tY2tisq23e--46Pa_k"
            }
        },
        {
            "uri": "http://testbed.spxp.org/0.3/bigcat321",
            "publicKey": {
                "kid": "key-bigcat321",
                "kty": "OKP",
                "crv": "Ed25519",
                "x": "jrZeWJFn2ORfzOjwaHrybyq7XTEvexzEOwEnHo7Z9bE"
            }
        },
        ...
    ],
    "private": [
        ...
    ]
}
```
The posts endpoint is slightly different, as each post is signed individually:
```
$ curl "http://testbed.spxp.org/0.3/posts/_read-posts.php?profile=angrybear287" > angrybear287-posts-page1.json
$ cat angrybear287-posts-page1.json | jq '.data[2]' > angrybear287-temp-post.json
$ SpxpCryptoTool verify angrybear287-temp-post.json angrybear287-publickey.json post,impersonate
Signature valid.
$ cat angrybear287-temp-post.json
{
  "createts": "2019-12-02T21:09:22.029",
  "type": "text",
  "message": "@LilliJ only 4 more hours til Akron!",
  "place": {
    "uri": "http://testbed.spxp.org/0.3/place.tokyo"
  },
  "signature": {
    "sig": "gmRxsb4Z0OKCjOgmAuyZMxN2SkL_K3Maa24TKj5Guq8z028oydZ-ABEoyxag_MZxLNQn5sulPmqm9GR_ggotAw",
    "key": {
      "publicKey": {
        "kid": "gIBPPPv8zaCHvr8N",
        "kty": "OKP",
        "crv": "Ed25519",
        "x": "WZaTpxIGmbbLMtbCMEHR0tMu-lvTQBGa3CWDYWxIF2Y"
      },
      "grant": [
        "impersonate",
        "post"
      ],
      "signature": {
        "sig": "riHvySkudKWFJVDihbm1YsRRnIWbtErEtbrKTxeFUkCRd1FToODfGsax9NT93XQfl8fhI4vSB8BDHi-HItHWDw",
        "key": "key-angrybear287"
      }
    }
  },
  "seqts": "2019-12-03T00:37:37.953"
}
```
This post has not been signed by the profile keypair directly, but by the
keypair `gIBPPPv8zaCHvr8N` which brings a certificate chain certifying that
this keypair is allowed to publish posts in the name of this profile. The
additional parameter `post,impersonate` on the `SpxpCryptoTool` allows
certificate chains that grant at least the “post” and the “impersonate”
permission to the signing key pair.

### Reading encrypted resources
Let's take a look at another profile:
```
$ curl http://testbed.spxp.org/0.3/lazyfish483
{
    "ver": "0.3",
    "name": "Flenn Fields",
    ...
    "profilePhoto": {
        "iv": "I92PkQaG537-gY1-",
        "k": "rxzTGcOntEN9hM6Zv6gC_MT2OHfTdWubWUuOOPa9sG0",
        "tag": "hoaJQjZSGj_14YCPGkglng",
        "uri": "images_enc/lazyfish483.encrypted"
    },
    "friendsEndpoint": "friends/lazyfish483",
    "postsEndpoint": "posts/_read-posts.php?profile=lazyfish483",
    "keysEndpoint": "keys/_read-keys.php?profile=lazyfish483",
    "publicKey": {
        "kid": "key-lazyfish483",
        "kty": "OKP",
        "crv": "Ed25519",
        "x": "WohuBobmXFOgFWQ2PoDEEKBUYTmRNB2V6BfMIA5ydzU"
    },
    "private": [
        ...
    ],
    "signature": {
        "sig": "x9EntBjVgzxkzcMVKJm34Vk37xPZuIZTuQHlTMkK02ZwgoPE9O1boTpyJ3SCBLbYsR4vwKNxyjyvHHI75x51AA",
        "key": "key-lazyfish483"
    }
}
```
This profile uses an encrypted resource as profile photo. First, we need to
download the encrypted resource and extract the profile photo object:
```
$ curl http://testbed.spxp.org/0.3/images_enc/lazyfish483.encrypted --output lazyfish483.encrypted 
$ curl http://testbed.spxp.org/0.3/lazyfish483 | jq '.profilePhoto' > lazyfish483-profilephoto.json
$ SpxpCryptoTool decryptresource lazyfish483.encrypted lazyfish483-profilephoto.json lazyfish483.jpeg
```
You can now open the decrypted profile image.

### Reading private data
The profile root object of `angrybear287` has a `private` array with one element. A SPXP server
would typically redact this information unless the client provides a `reader`
parameter conatining a list of reader keys.  
This testbed server does not perform this redaction to make these profiles
easier to explore.  
Let's extract the first element and investigate the required key:
```
$ cat angrybear287-profile-root.json | jq --raw-output '.private[0]' | tr -d '\n' > angrybear287-profile-private0-encrypted.txt
$ SpxpCryptoTool decryptsymcompact angrybear287-profile-private0-encrypted.txt
grp-angrybear287-friends.FxkqfHGu
```
We can now assume we are one of angrybear287's friends. From the firends list
above, we simply pick the first entry: “greenrabbit943”.  
The private data that each profile holds on the device is also available on this
testbed. Let's find out what “greenrabbit943” has stored for the connection to
“angrybear287”:
```
$ curl http://testbed.spxp.org/0.3/private_profile_data/greenrabbit943.json | jq '.connections | .[] | select(.profileUri == "http://testbed.spxp.org/0.3/angrybear287") | .grantedReaderKey' > angrybear287-readerkey-for-greenrabbit943.json
$ cat angrybear287-readerkey-for-greenrabbit943.json
{
    "kid": "angrybear287-readerkey-for-greenrabbit943",
    "kty": "oct",
    "alg": "A256GCM",
    "k": "b9tAZ7BVfBEMLFw_ZlJJBss5qJuSadeEL2n-ef9LYx0"
}
```
With this information, we can now ask the keys endpoint to give us all
intermediate keys so  that we are able to finally read the private data:
```
$ curl "http://testbed.spxp.org/0.3/keys/_read-keys.php?profile=angrybear287&reader=angrybear287-readerkey-for-greenrabbit943&request=grp-angrybear287-friends.FxkqfHGu" | jq . > angrybear287-keys-response.json
$ cat angrybear287-keys-response.json
{
  "grp-angrybear287-friends-virt_0": {
    "grp-angrybear287-friends": {
      "FxkqfHGu": "eyJraWQiOiJncnAtYW5ncnliZWFyMjg3LWZyaWVuZHMtdmlydF8wLlg1UEgxVU5QIiwiZW5jIjoiQTI1NkdDTSIsImFsZyI6ImRpciJ9..z90481PfoXspYvso.aAYyYm-yzA49ZY0FfLFiJLMJrZLMQkHLXljQGYDbcfpOkfwVEvJXvumNm8j4Xw1tUpd_vxv90maSu06qr-u9NhzSU1Uj7hzCuef3npRiCGdh-ztCKMmswx334l7UvEUCdsV75ssNmapkSlcFiWYxgKm9cksqFD7IrQ.9ECf-Nmbn3o5_L9z5Ohqmw"
    }
  },
  "angrybear287-readerkey-for-greenrabbit943": {
    "grp-angrybear287-friends-virt_0": {
      "X5PH1UNP": "eyJraWQiOiJhbmdyeWJlYXIyODctcmVhZGVya2V5LWZvci1ncmVlbnJhYmJpdDk0MyIsImVuYyI6IkEyNTZHQ00iLCJhbGciOiJkaXIifQ..q9l-Te6kkzRDZzF_.sqlpjmUOL9MQpdQPRqwjJGOqEtkn0EMIqbtKyCXHPWHoZrYmml37QDK5fZmVyB_pkycKy95AcN9G8FI0e5WJEFWU77JIWgsGTDDX95ai50D1BKagMQzUN_lnhuZLlvjU7JpxK3jwoNwIjuhTpS0gcNIcJ3EyrG25SCnbzLUzWfQ.hrWDW3yPNf17gB2UepX_dQ"
    }
  }
}
```
The second object is telling us that the reader key `angrybear287-readerkey-for-greenrabbit943`
can decrypt round keys for the key group `grp-angrybear287-friends-virt_0`.  
Let's decrypt the first key:
```
$ cat angrybear287-keys-response.json | jq --raw-output '.["angrybear287-readerkey-for-greenrabbit943"]["grp-angrybear287-friends-virt_0"]["X5PH1UNP"]' | tr -d '\n' > grp-angrybear287-friends-virt_0_X5PH1UNP_encrypted.txt
$ SpxpCryptoTool decryptsymcompact grp-angrybear287-friends-virt_0_X5PH1UNP_encrypted.txt angrybear287-readerkey-for-greenrabbit943.json > grp-angrybear287-friends-virt_0_X5PH1UNP.json
```
Now we can decrypt the key we need on the private data:
```
$ cat angrybear287-keys-response.json | jq --raw-output '.["grp-angrybear287-friends-virt_0"]["grp-angrybear287-friends"]["FxkqfHGu"]' | tr -d '\n' > grp-angrybear287-friends_FxkqfHGu_encrypted.txt
$ SpxpCryptoTool decryptsymcompact grp-angrybear287-friends_FxkqfHGu_encrypted.txt grp-angrybear287-friends-virt_0_X5PH1UNP.json > grp-angrybear287-friends_FxkqfHGu.json
```
Now we finally have the key `grp-angrybear287-friends.FxkqfHGu` to decrypt the
private data in the profile root object. We need to check the signature on each
decrypted private object:
```
$ SpxpCryptoTool decryptsymcompact angrybear287-profile-private0-encrypted.txt grp-angrybear287-friends_FxkqfHGu.json > angrybear287-profile-private0.json
$ SpxpCryptoTool verify angrybear287-profile-private0.json angrybear287-publickey.json
Signature valid.
$ cat angrybear287-profile-private0.json
{"shortInfo":"Much like music, great art is also found in the spaces between your graphic elements.","about":"Lorem ipsum ...","gender":"male","birthDayAndMonth":"20-02","hometown":{"uri":"http://testbed.spxp.org/0.3/place.rome"},"signature":{...}}
```
With the reader key `angrybear287-readerkey-for-greenrabbit943` we have now been
able to decrypt this additional private information. A client would merge this
information into the profile root object before interpreting and displaying it.