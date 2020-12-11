# Manually Create a SPXP Profile
The main purpose of this document is to learn more about SPXP by manually
creating a new profile.

If you just want to create a profile, you are most likely better off using a
SPXP client which performs all these steps for you and then publishes your new
profile to a SPXP server.

Since SPXP is based on well established technologies, a couple of well crafted
files on a webserver already constitute a valid profile.

We will start with a simple profile and then extend it step by step to learn
more about SPXP along the way.

### Profile KeyPair
Although it is possible to publish profiles without a cryptographic KeyPair, it
is strongly discouraged.  
So as first step, we will create a new KeyPair for our profile. We use the
`SpxpCryptoTool` commandline tool for all cryptographic operations. Since this
tool prints the results to standard out, we pipe the result to a file. To create
your KeyPair, run this command:
```
$ SpxpCryptoTool genprofilekeypair > john-doe-profile-keypair.json
```
We can then take a look at your profile KeyPair:
```
$ cat john-doe-profile-keypair.json
{
    "kty": "OKP",
    "d": "cimnMK-C7_RERO2c9IyY9Ch4lGkvROPz7ohAbQVR7W8",
    "crv": "Ed25519",
    "kid": "-MUelyMS_IJ6Lc5b",
    "x": "F1MQVHdxYR62bmvepQXKbNOUxfy0wCynqaCraXPzMGs"
}
```
You need to make sure to properly secure and backup this file! If you loose it,
you will not be able to reclaiming your own profile. And if it gets accidentally
exposed, your profile is compromised.  
This KeyPair contains the private key `d` as well as the public key `x`. Later
on, we will just need the public key to include it in our new profile. You can
now simply copy this file and remove your private key `d` in this copy. Or you
can use the `SpxpCryptoTool` for this operation as well:
```
$ SpxpCryptoTool extractprofilepublic john-doe-profile-keypair.json > john-doe-profile-publickey.json
$ cat john-doe-profile-publickey.json
{
    "kty": "OKP",
    "crv": "Ed25519",
    "kid": "-MUelyMS_IJ6Lc5b",
    "x": "F1MQVHdxYR62bmvepQXKbNOUxfy0wCynqaCraXPzMGs"
}
```

### Initial simple profile
Now that we have a KeyPair, we can start to write our profile. Use your favorite
text editor to start a new file and name it as you like:
```
$ touch john-doe-profile-unsigned.json
$ vi john-doe-profile-unsigned.json
```
Then add this initial JSON object and the **public** key you have extracted:
```json
{
    "ver": "0.3",
    "name":  "John Doe",
    "publicKey"  : {
        "kid": "-MUelyMS_IJ6Lc5b",
        "kty": "OKP",
        "crv": "Ed25519",
        "x": "F1MQVHdxYR62bmvepQXKbNOUxfy0wCynqaCraXPzMGs"
    }
}
```
Please double check that you did not accidentally include your private key `d`.
This object is the smallest possible profile. You can now add more information
that you want to be publicly available, like your website or an additional
descriptive text. You can find all possible profile members in the [SPXP Spec in
chapter 5](https://github.com/spxp/spxp-specs/blob/v0.3/SPXP-Spec.md#5-social-profile-root-document).
For example:
```json
{
    "ver": "0.3",
    "name":  "John Doe",
    "about":  "Hey, look, I have a SPXP profile!",
    "website":  "https://example.com",
    "publicKey"  : {
        "kid": "-MUelyMS_IJ6Lc5b",
        "kty": "OKP",
        "crv": "Ed25519",
        "x": "F1MQVHdxYR62bmvepQXKbNOUxfy0wCynqaCraXPzMGs"
    }
}
```
When you are happy with your profile, we need to sign it before it can be
published. We use again the `SpxpCryptoTool` for this operation:
```
$ SpxpCryptoTool sign john-doe-profile-unsigned.json john-doe-profile-keypair.json > john-doe-1.json
```
And you successfully created your first profile!
```json
{
    "ver": "0.3",
    "name": "John Doe",
    "about": "Hey, look, I have a SPXP profile!",
    "website": "https://example.com",
    "publicKey": {
        "kid": "-MUelyMS_IJ6Lc5b",
        "kty": "OKP",
        "crv": "Ed25519",
        "x": "F1MQVHdxYR62bmvepQXKbNOUxfy0wCynqaCraXPzMGs"
    },
    "signature": {
        "key": "-MUelyMS_IJ6Lc5b",
        "sig": "jzRBoJvSD4s9nA8MK3WQAyqna5acLug1TAVxtWtleSO7pAWBz4XCIQkoz2Wcl1fJ3XYuooEg3D7f9IX7Uck7DQ"
    }
}
```
This signature is resilient against reordering the members in an object and
against adding or removing insignificant whitespace. So you can format this
object to your liking without breaking the signature.

### Publish your initial Profile
To publish your profile, simply copy it anywhere on your webserver. For example,
you could copy it as `john-doe` to the folder `/spxp` on your server. Then the
Profile URI of your profile would be:
```
https://example.com/spxp/john-doe
```
Depending on your web server software, you might need to add the `.json` file
extension or to configure your server so that it sends the required
```
Content-Type: application/json
```
HTTP header. Some SPXP clients might be able to deal with a missing or invalid
Content Type header, but you cannot rely on this.

To validate your profile, you can simply open your profile URI in any web
browser or `curl` it from the command line. If your file gets delivered as
expected, it is time to pick your favorite SPXP client and see your profile live
for the first time!

### Exposing Friends
A Profile *can* expose a list of other profiles as “Friends”. These are
typically used by visitors to explore the social relationships and jump through
the links in a social group.  
If you want to expose other profiles as your friends, start another new file
with your favorite text editor
```
$ touch john-doe-friends-unsigned.json
$ vi john-doe-friends-unsigned.json
```
and add a list of social profile URIs you want to expose publicly as your
“Friends”:
```json
{
    "data" : [ {
        "uri": "http://testbed.spxp.org/0.3/greenrabbit943",
        "publicKey": {
            "kid": "key-greenrabbit943",
            "kty": "OKP",
            "crv": "Ed25519",
            "x": "YX_7SvvFUGiMMsCdPhnG-DvK7tY2tisq23e--46Pa_k"
        }
    }, {
        "uri": "http://testbed.spxp.org/0.3/bigcat321",
        "publicKey": {
            "kid": "key-bigcat321",
            "kty": "OKP",
            "crv": "Ed25519",
            "x": "jrZeWJFn2ORfzOjwaHrybyq7XTEvexzEOwEnHo7Z9bE"
        }
    } ]
}
```
We are referencing other profiles not just by their profile URI, but combine
this with their public key. So clients looking at your friends at some point in
the future are able to validate that the information exposed through this URI is
still the profile you considered your “Friend”.  
As any information published via SPXP, it then has to be signed:
```
$ SpxpCryptoTool sign john-doe-friends-unsigned.json john-doe-profile-keypair.json > john-doe-friends.json
```
resulting in
```json
{
    "data" : [ {
        "uri": "http://testbed.spxp.org/0.3/greenrabbit943",
        "publicKey": {
            "kid": "key-greenrabbit943",
            "kty": "OKP",
            "crv": "Ed25519",
            "x": "YX_7SvvFUGiMMsCdPhnG-DvK7tY2tisq23e--46Pa_k"
        }
    }, {
        "uri": "http://testbed.spxp.org/0.3/bigcat321",
        "publicKey": {
            "kid": "key-bigcat321",
            "kty": "OKP",
            "crv": "Ed25519",
            "x": "jrZeWJFn2ORfzOjwaHrybyq7XTEvexzEOwEnHo7Z9bE"
        }
    } ],
    "signature": {
        "key": "-MUelyMS_IJ6Lc5b",
        "sig": "ILo5iT4xY-QAcYEQ5oXiemM8DvzZw2tBsc81tqcz5CCCSA41wpPuE0fJoJ41QijmQSyUsnlTJnFh-OllZ9mbBA"
    }
}
```
Now we need to reference this file from our profile root object. We first need
to think about how and where we are going to publish this file. We decide to
upload it to the same webserver and place it next to our profile file under the
name `john-doe-friends`. So the absolute URI to file file will be:
```
https://example.com/spxp/john-doe-friends
```
Now that we know its URI, we can add a `friendsEndpoint` to the profile file:
```json
{
    "ver": "0.3",
    "name": "John Doe",
    "about": "Hey, look, I have a SPXP profile!",
    "website": "https://example.com",
    "friendsEndpoint": "john-doe-friends",
    "publicKey": {
        "kid": "-MUelyMS_IJ6Lc5b",
        "kty": "OKP",
        "crv": "Ed25519",
        "x": "F1MQVHdxYR62bmvepQXKbNOUxfy0wCynqaCraXPzMGs"
    }
}
```
The member `friendsEndpoint` contains a *URI-reference*. This can either be
the entire absolute URI, or a reference relative to the profile root URI. We
decide for the latter option and use the plain filename.  
After having modified this file, we need to sign it again:
```
$ SpxpCryptoTool sign john-doe-profile-unsigned.json john-doe-profile-keypair.json > john-doe-2.json
```
To publish your changes, simply upload both files to your webserver. Check your
profile again in your SPXP client. Keep in mind that most clients do not refresh
the information immediately.

### Publishing Posts
One of the key functions of social profiles is to keep your followers updated
by publishing “Posts”. In contrast to the files we have created so far, posts
are signed individually and then combined in one file. Let's start our very
first Post, again with our favorite text editor:
```
$ touch john-doe-post1-unsigned.json
$ vi john-doe-post1-unsigned.json
```
and add this information:
```json
{
    "seqts" : "2020-01-01T12:00:00.000",
    "type" : "text",
    "message" : "Hey, I have a SPXP profile!"
}
```
The format of the timestamp in `seqts` is year-month-day, plus the character
`T` and then the time in 24h format including milliseconds. This timestamp is
always in UTC. This post then needs to be signed:
```
$ SpxpCryptoTool sign john-doe-post1-unsigned.json john-doe-profile-keypair.json > john-doe-post1-signed.json
```
And we can create a second post, this time with a picture:
```
$ touch john-doe-post2-unsigned.json
$ vi john-doe-post2-unsigned.json
```
and add this information:
```json
{
    "seqts" : "2020-01-02T12:00:00.000",
    "type" : "photo",
    "message" : "I love Wikipedia",
    "small": "https://en.wikipedia.org/static/images/project-logos/enwiki.png"
}
```
...and sign it...
```
$ SpxpCryptoTool sign john-doe-post2-unsigned.json john-doe-profile-keypair.json > john-doe-post2-signed.json
```
Both posts are then combined in one single file:
```
$ touch john-doe-posts.json
$ vi john-doe-posts.json
```
with this content
```json
{
    "data": [
        {
            "seqts": "2020-01-01T12:00:00.000",
            "type": "text",
            "message": "Hey, I have a SPXP profile!",
            "signature": {
                "sig": "LpHhmVX6ea2Am7prfu-2c6vi7D0sa50-NENsEdQA5IodQu7wdoIf0r_fGM2Fw56DacG7LnmAQ18Ba47aYqp4DQ",
                "key": "-MUelyMS_IJ6Lc5b"
            }
        }, {
            "seqts": "2020-01-02T12:00:00.000",
            "type": "photo",
            "message": "I love Wikipedia",
            "small": "https://en.wikipedia.org/static/images/project-logos/enwiki.png",
            "signature": {
                "sig": "8C6G2i6GjG1f_2M8b4PARz2_Qjec-r1vH0eIcdWKNDfzwjcWp8c47lPJwHzNtBz_c7xxQDXlWqwlbQv2dbtZAA",
                "key": "-MUelyMS_IJ6Lc5b"
            }
        }
    ],
    "more": false
}
```
As before, this new file must be referenced in the profile root file. We decide
to upload it to the same folder on our webserver under the filename
`john-doe-posts`. So we extend the profile root file with a `postsEndpoint`:
```json
{
    "ver": "0.3",
    "name": "John Doe",
    "about": "Hey, look, I have a SPXP profile!",
    "website": "https://example.com",
    "friendsEndpoint": "john-doe-friends",
    "postsEndpoint": "john-doe-posts",
    "publicKey": {
        "kid": "-MUelyMS_IJ6Lc5b",
        "kty": "OKP",
        "crv": "Ed25519",
        "x": "F1MQVHdxYR62bmvepQXKbNOUxfy0wCynqaCraXPzMGs"
    }
}
```
As before, we need to sign this profile root file again. Then we can upload the
new `john-doe-posts` and this modified profile root file to our webserver.
These two posts should now appear in the timeline of this profile.

### Limited visibility with End-to-End Encryption
One key feature of SPXP is the ability to limit the visibility of selected
information to a restricted audience &ndash; with full end-to-end encryption.  
To publish encrypted information, we first need to create a symmetric encryption
key:
```
$ SpxpCryptoTool gensymkey > john-doe-closefriends-key.json
```
Out of interest, we can then take a look at this key:
```
$ cat john-doe-closefriends-key.json
{
    "kty": "oct",
    "kid": "97hAmlFbPIEGgJ8E",
    "k": "-llkzENMGpuIGWA4SUwOAjphsBFn-9bmreNXF7FvRH4",
    "alg": "A256GCM"
}
```
For example, we could chose to publish the date of birth only to our close
friends. So we start a new file `john-doe-dob-unsigned.json` with a JSON
object that only contains this additional information:
```json
{
    "birthDayAndMonth" : "20-02",
    "birthYear" : "1960"
}
```
This file then needs to be signed:
```
$ SpxpCryptoTool sign john-doe-dob-unsigned.json john-doe-profile-keypair.json > john-doe-dob-signed.json
```
And then we can encrypt the signed file with the symmetric key:
```
$ SpxpCryptoTool encryptsymcompact john-doe-dob-signed.json john-doe-closefriends-key.json > john-doe-private-dob.json
```
The ciphertext in `john-doe-private-dob.json` can then be added as String to
the `private` array in the profile root file:
```json
{
    "ver": "0.3",
    "name": "John Doe",
    "about": "Hey, look, I have a SPXP profile!",
    "website": "https://example.com",
    "friendsEndpoint": "john-doe-friends",
    "postsEndpoint": "john-doe-posts",
    "publicKey": {
        "kty": "OKP",
        "crv": "Ed25519",
        "kid": "-MUelyMS_IJ6Lc5b",
        "x": "F1MQVHdxYR62bmvepQXKbNOUxfy0wCynqaCraXPzMGs"
    },
    "signature": {
        "sig": "uEB7YJtlYAp2cOqaKj-yfBwGnxVOIFIx89DdYS-W-mivSvsy9M56MPp3NoOFhTOrlOwF49PFZeUiiI_b2SGNCQ",
        "key": "-MUelyMS_IJ6Lc5b"
    },
    "private": [
        "eyJraWQiOiI5N2hBbWxGYlBJRUdnSjhFIiwiZW5jIjoiQTI1NkdDTSIsImFsZyI6ImRpciJ9..ipaEFU9UUheLH4mU.ZE0OSdYJrxYipaXAI_LHG1ZXFAe7wrckpXpI8K714hudlbTgjAR9KriFcLOZiXZIyYEnl9xb7CvNsl2JHuB7lSWiXTrqZBNgtmZ_P86XS06wzW57xj5bj5C-3H8dUNDAlbfNMzFVbJRYFh35NDTo4Bk62rv1tt4SQ95s7xzRtIDMsbOhJMVlg0K2UVBLxFsEDbjopzIO5oojSfi1LfvpY12SqV1cJG_bPinv6UEGthX5RXm2dGmaS1DX-2hq3MKFDE6rUKVeOx9LgybxEpnkADDq0efF7Z5Gq8yggktey-8z.RVCqXB-v6ZiThpogr-IrcA"
    ]
}
```
This time, the profile root file does not need to be signed again. The signature
does not cover the `private` array to allow the server to redact this data
if the client cannot prove knowledge of the reader key.  
After uploading the changed profile root file to the webserver, you will not see
any change in your SPXP client. To be able to decrypt the date of birth, the
client needs to know the symmetric key we used to encrypt this fragment.  
Some client applications are able to add additional reader keys for profiles. If
you use such a SPXP client, you can send the key in the file
`john-doe-closefriends-key.json` to your client and see the date of birth
appearing on this profile.

### Encrypted Information for Multiple Audiences
Limiting the visibility of the date of birth to our close friends worked quite
well. So we chose to also publish our email address. But this piece of
information should also be visible to our work colleagues and not just be
limited to our close friends.  
For this new group of work colleagues, we first need to create a new symmetric
key:
```
$ SpxpCryptoTool gensymkey > john-doe-work-key.json
```
Out of interest, we can then take a look at this key:
```
$ cat john-doe-work-key.json
{
    "kty": "oct",
    "kid": "aTS9WBP2n5oOr3o4",
    "k": "J0-k2UvYY2ef_FC9fMp0JHcZIzXcSqrSKT8V5R1JHKQ",
    "alg": "A256GCM"
}
```
We again create a new file `john-doe-email-unsigned.json` with a JSON object
that only contains our email address:
```json
{
    "email" : "john.doe@example.com"
}
```
And sign it:
```
$ SpxpCryptoTool sign john-doe-email-unsigned.json john-doe-profile-keypair.json > john-doe-email-signed.json
```
But this time, we want both keys, the one for our friends and the one for our
work colleagues, to be able to decrypt our email address. This is only possible
by using JSON serialization for the ciphertext. We can simply pass multiple keys
as comma  separated list:
```
$ SpxpCryptoTool encryptsymjson john-doe-email-signed.json john-doe-friends-key.json,john-doe-work-key.json > john-doe-private-email.json
```
If we take a look at the  result in `john-doe-private-email.json`, we will find
a complex JSON object describing the encrypted data:
```json
{
    "ciphertext": "KtUPkw-b6hUvV9OTrAaF1455NU1BAPobDt63laufWSWjOV1IUJy9BWW8DohKIhwlqDi1tP6jQV_WLjh5PIrHo5U7mYCiPt1W7CWWXSyrNftONFC1k6BR96bmwtG6abG-x3dGpdDvdqgqZ6QCdYZj3RjjXDRpz2JyQolWmSf1rKvWTn3fYiKN2PB1WWLMQsWlB5w2WyfCNG9HxT9vbDBvnfR1XjnlrAKRY1GBhCP7AoYpnXcSrst7Xz6fTuVKWYbmRj8dPKL5STqQLONq",
    "protected": "eyJlbmMiOiJBMjU2R0NNIn0",
    "recipients": [
        {
            "encrypted_key": "ZCZyrhlSC6s8UHAexa9bfT2q67MBAwuuCYw7NqcrSd0",
            "header": {
                "kid": "97hAmlFbPIEGgJ8E",
                "tag": "KF0W2Ap4S22oDbMieNjTXw",
                "iv": "P96L1GeqcBSAkyAK"
            }
        },
        {
            "encrypted_key": "Hi4yNfEu9d6SYYHclRJs76DhYau0dLkmqj7la_7E8ro",
            "header": {
                "kid": "aTS9WBP2n5oOr3o4",
                "tag": "_jyHQbIHUXb00jMJ1uZx7g",
                "iv": "asBf81T-hoY2WZPF"
            }
        }
    ],
    "unprotected": {"alg": "A256GCMKW"},
    "tag": "s7qB_RdOTlI1k9EBR9E98g",
    "iv": "FA9Vdb4Ol6xdwYkf"
}
```
This entire object is then added as additional element to the `private` array
in the profile root object:
```json
{
    "ver": "0.3",
    "name": "John Doe",
    "about": "Hey, look, I have a SPXP profile!",
    "website": "https://example.com",
    "friendsEndpoint": "john-doe-friends",
    "postsEndpoint": "john-doe-posts",
    "publicKey": {
        "kty": "OKP",
        "crv": "Ed25519",
        "kid": "-MUelyMS_IJ6Lc5b",
        "x": "F1MQVHdxYR62bmvepQXKbNOUxfy0wCynqaCraXPzMGs"
    },
    "signature": {
        "sig": "uEB7YJtlYAp2cOqaKj-yfBwGnxVOIFIx89DdYS-W-mivSvsy9M56MPp3NoOFhTOrlOwF49PFZeUiiI_b2SGNCQ",
        "key": "-MUelyMS_IJ6Lc5b"
    },
    "private": [
        "eyJraWQiOiI5N2hBbWxGYlBJRUdnSjhFIiwiZW5jIjoiQTI1NkdDTSIsImFsZyI6ImRpciJ9..ipaEFU9UUheLH4mU.ZE0OSdYJrxYipaXAI_LHG1ZXFAe7wrckpXpI8K714hudlbTgjAR9KriFcLOZiXZIyYEnl9xb7CvNsl2JHuB7lSWiXTrqZBNgtmZ_P86XS06wzW57xj5bj5C-3H8dUNDAlbfNMzFVbJRYFh35NDTo4Bk62rv1tt4SQ95s7xzRtIDMsbOhJMVlg0K2UVBLxFsEDbjopzIO5oojSfi1LfvpY12SqV1cJG_bPinv6UEGthX5RXm2dGmaS1DX-2hq3MKFDE6rUKVeOx9LgybxEpnkADDq0efF7Z5Gq8yggktey-8z.RVCqXB-v6ZiThpogr-IrcA",
         {
            "ciphertext": "KtUPkw-b6hUvV9OTrAaF1455NU1BAPobDt63laufWSWjOV1IUJy9BWW8DohKIhwlqDi1tP6jQV_WLjh5PIrHo5U7mYCiPt1W7CWWXSyrNftONFC1k6BR96bmwtG6abG-x3dGpdDvdqgqZ6QCdYZj3RjjXDRpz2JyQolWmSf1rKvWTn3fYiKN2PB1WWLMQsWlB5w2WyfCNG9HxT9vbDBvnfR1XjnlrAKRY1GBhCP7AoYpnXcSrst7Xz6fTuVKWYbmRj8dPKL5STqQLONq",
            "protected": "eyJlbmMiOiJBMjU2R0NNIn0",
            "recipients": [
                {
                    "encrypted_key": "ZCZyrhlSC6s8UHAexa9bfT2q67MBAwuuCYw7NqcrSd0",
                    "header": {
                        "kid": "97hAmlFbPIEGgJ8E",
                        "tag": "KF0W2Ap4S22oDbMieNjTXw",
                        "iv": "P96L1GeqcBSAkyAK"
                    }
                },
                {
                    "encrypted_key": "Hi4yNfEu9d6SYYHclRJs76DhYau0dLkmqj7la_7E8ro",
                    "header": {
                        "kid": "aTS9WBP2n5oOr3o4",
                        "tag": "_jyHQbIHUXb00jMJ1uZx7g",
                        "iv": "asBf81T-hoY2WZPF"
                    }
                }
            ],
            "unprotected": {"alg": "A256GCMKW"},
            "tag": "s7qB_RdOTlI1k9EBR9E98g",
            "iv": "FA9Vdb4Ol6xdwYkf"
         }
    ]
}
```
As before, the signature is still valid. After copying this modified file to
your webserver, you can try the behaviour in your SPXP client. See what happens
if you have one or the other reader key registered with this profile.

### Encrypting Resources
Let's assume we want to publish a post with a private photo just to our Friends.
We already know how we can encrypt objects like post message itself. But this
object just contains a URI to the image resource, which is located somewhere
accessible by everyone who knows the URI.  
To limit the visibility of resources like photos and videos, SPXP provides a
mechanism to encrypt these resources. To encrypt a private photo like the file
`private-photo.jpg`, you can run
```
$ SpxpCryptoTool encryptresource private-photo.jpg private-photo.encrypted > encrypted-private-photo.json
```
This tool will encrypt the binary file, write it as `private-photo.encrypted`
and then print out a JSON object describing this object:
```json
{
    "iv": "VjHEHo0wIwSf-i7-",
    "k": "Iok36Sqr4sl7VHFB_K9dtPTndmqbY2Mh4zm_IuJpvcg",
    "tag": "kbWD8V3dDYXcPGpx3A6Mag"
}
```
But this object is not yet sufficient to describe an encrypted photo. In
addition, we need to add a `uri` member to this object describing the location
of the *encrypted* data. We decide to put the encrypted photo next to our other
files on our webserver. So we simply use the file name as relative URI:
```json
{
    "iv": "VjHEHo0wIwSf-i7-",
    "k": "Iok36Sqr4sl7VHFB_K9dtPTndmqbY2Mh4zm_IuJpvcg",
    "tag": "kbWD8V3dDYXcPGpx3A6Mag",
    "uri": "private-photo.encrypted"
}
```
If we give this URI as optional 4th parameter to the SpxpCryptoTool, it is
already integrated in the object printed to standard out.  
Once we have an encrypted photo, we then start a new private post:
```
$ touch john-doe-post3-private-unsigned.json
$ vi john-doe-post3-private-unsigned.json
```
We add all post members, except for `seqts`:
```json
{
    "type" : "photo",
    "message" : "Look at this!",
    "small": {
        "iv": "VjHEHo0wIwSf-i7-",
        "k": "Iok36Sqr4sl7VHFB_K9dtPTndmqbY2Mh4zm_IuJpvcg",
        "tag": "kbWD8V3dDYXcPGpx3A6Mag",
        "uri": "private-photo.encrypted"
    }
}
```
then sign it:
```
$ SpxpCryptoTool sign john-doe-post3-private-unsigned.json john-doe-profile-keypair.json > john-doe-post3-private-signed.json
```
And encrypt it for our close friends:
```
$ SpxpCryptoTool encryptsymcompact john-doe-post3-private-signed.json john-doe-closefriends-key.json > john-doe-post3-private.json
```
Then we can start a new post object:
```
$ touch john-doe-post3.json
$ vi john-doe-post3.json
```
solely consisting of the `seqts` and this `private` data:
and add this information:
```json
{
    "seqts" : "2020-01-03T10:23:14.623",
    "private" : [
        "eyJraWQiOiI5N2hBbWxGYlBJRUdnSjhFIiwiZW5jIjoiQTI1NkdDTSIsImFsZyI6ImRpciJ9..FS6rUCmyYJHIf1Wo.89a6oN9c4yDDIEpTmGo0YdxYKbjsyTaObXv2zp2ia2OdXz0UzBX1F2SidQARsakEtpJB_9s47shkVxMipJhSKVKA1hhyX86Ph8sEVWFpmd4njwQvti-jtnoCrzsgdIylv4zand7B0e__J66KANflEMjTQ7pRxHJyDLjbtc10KyJVmM_X2mFs6USrQ2_NfKbi_jaRXVTqMTdpKGoPmhBBSXTuxGhuK8FLFvijPdFOkfTwvtKX-fucI59w6zJmyNmg2CNcZgSGT_DtxD7u4vUVBqYQ2eZIUWPNemaSufPBuEj1hRLCHuRR3FMsd0ZdCnZPEx-FpsEb8V1guOZFVcf1AfkgWo_YKMLQ5OjxzuKA3Ojt4hSTMdPAGCPzC18sVplN2WlRlDdFGK0_mkAkcf8iKvYA6gxArUs57-Wo2GEWgMscLrANbXo0W7Fth3fI3m0yaAEKQVH0gLcAwx5CIw4IWoXWduOxapn0PAK2hTSP31r47bTDLvP_NfmsD3NfsiAg3JWhY7fbNRW2H9uvoIMOPluaM2xae_01lKcGp6X7ybeKgQ.7MbiJ8jcwxFAGvr2DI_FMg"
    ]
}
```
In this special case, the post does not need to be signed. Both fields, the
`seqts` and the `private` array are not covered by signatures. So signing
this object would actually sign a completely empty object.  
We can then add this new post to our existing `john-doe-posts.json` file and
copy it to our webserver.  
Open this profile in your SPXP client and watch what happens with the timeline
of this profile when you add the reader key to your profile.

### Managing Multiple Readers
If you are lucky, you most likely have more than one close friend and more than
one work colleague. In this case, you can simply share the symmetric reader
keys we have created above among multiple people. But what happens if you want
to stop sharing information with one of these readers? Or if one of these keys
gets exposed to a wider audience than intended?

Another option would be to create separate keys for each individual friend and
work colleague. But then, each piece of information needs to be encrypted for
potentially hundreds of readers. And what if you want to add a new work
colleague later on? You would need to change many existing objects to include
this new key.

To cope with all of that, SPXP provides the possibility to encrypt keys with
other keys (key wrapping) to create a tree structure of keys. Lets assume you
have two close friends Alice and Bob and two work colleagues Charles and Dave.
Frist, lets create individual keys for all four:
```
$ SpxpCryptoTool gensymkey > alice-key.json
$ SpxpCryptoTool gensymkey > bob-key.json
$ SpxpCryptoTool gensymkey > charles-key.json
$ SpxpCryptoTool gensymkey > dave-key.json
```
Then lets create initial *round keys* for our two groups “Close Friends” and
“Business Contacts”:
```
$ SpxpCryptoTool genroundkey > closefriends-round1.json
$ SpxpCryptoTool genroundkey > businesscontacts-round1.json
```
These round keys are normal symmetric AES keys, but use a specific format for
the key id.  
Next, we encrypt these round keys with the keys of the individuals that belong
to these groups. So we encrypt the “Close Friends” round key with the keys of
“Alice” and “Bob” individually:
```
$ SpxpCryptoTool encryptsymcompact closefriends-round1.json alice-key.json > closefriends-round1-alice.json
$ SpxpCryptoTool encryptsymcompact closefriends-round1.json bob-key.json > closefriends-round1-bob.json
```
And we encrypt the “Business Contacts” round key with the keys of “Charles” and
“Dave” individually:
```
$ SpxpCryptoTool encryptsymcompact businesscontacts-round1.json charles-key.json > businesscontacts-round1-charles.json
$ SpxpCryptoTool encryptsymcompact businesscontacts-round1.json dave-key.json > businesscontacts-round1-dave.json
```
Those four are then combined in one single file:
```
$ touch john-doe-keys.json
$ vi john-doe-keys.json
```
Let's assume we have the following key ids:
| keyid | Key |
|---|---|
| `AliceAliceAliceA` | Individual key for “Alice” (alice-key.json) |
| `BobBobBobBobBobB` | Individual key for “Bob” (bob-key.json) |
| `CharlesCharlesCh` | Individual key for “Charles” (charles-key.json) |
| `DaveDaveDaveDave` | Individual key for “Dave” (dave-key.json) |
| `CloseFriendsClos.11111111` | Round 1 key for “Close Friends” (closefriends-round1.json) |
| `BusinessContacts.11111111` | Round 1 key for “Business Contacts” (businesscontacts-round1.json) |

Then we organise the keys in a JSON structure as follows:
```json
{
    "AliceAliceAliceA": {
        "CloseFriendsClos": {
            "11111111": "<closefriends-round1-alice.json>"
        }
    },
    "BobBobBobBobBobB": {
        "CloseFriendsClos": {
            "11111111": "<closefriends-round1-bob.json>"
        }
    },
    "CharlesCharlesCh": {
        "BusinessContacts": {
            "11111111": "<businesscontacts-round1-charles.json>"
        }
    },
    "DaveDaveDaveDave": {
        "BusinessContacts": {
            "11111111": "<businesscontacts-round1-dave.json>"
        }
    }
}
```
As before, this new file must be referenced in the profile root file. We decide
to upload it to the same folder on our webserver under the filename
`john-doe-keys`. So we extend the profile root file with a `keysEndpoint`:
```json
{
    "ver": "0.3",
    "name": "John Doe",
    "about": "Hey, look, I have a SPXP profile!",
    "website": "https://example.com",
    "friendsEndpoint": "john-doe-friends",
    "postsEndpoint": "john-doe-posts",
    "keysEndpoint": "john-doe-keys",
    "publicKey": {
        "kid": "-MUelyMS_IJ6Lc5b",
        "kty": "OKP",
        "crv": "Ed25519",
        "x": "F1MQVHdxYR62bmvepQXKbNOUxfy0wCynqaCraXPzMGs"
    }
}
```
The file `john-doe-keys` does not need to be signed, but the profile root file
must be signed before uploading it to the webserver.  
You can now hand out a personal reader key to “Alice”. Once she adds this key to
your profile in her SPXP client, she will be able to see all information that
has been encrypted with the “Close Friends” round key.