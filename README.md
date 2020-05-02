# SPXP Crypto

The Social Profile Exchange Protocol (SPXP) is defined based on well established
standards - JSON and HTTP. There are already numerous implementations of these
standards out there for all kinds of platforms and in any programming language.

The SPXP specific data structures on top of these protocols are limited. By
providing a complete full-stack API for SPXP, we would most likely interfere
with the developer's preferences and cause conflicts with other libraries
already used by the client or server application.

But for cryptographic operations, the picture is slightly different. While a
developer can visually check the conformance of JSON objects, this is almost
impossible for binary data in encryption and signing operations.

Since the cryptographic operations in SPXP are mainly based on JOSE, you can
also find some libraries out there that provide the cryptographic operations
required for SPXP. But these libraries typically lack support for Ed25519 and
fail when SPXP is not closely following the JOSE standard (e.g. with embedded
signatures).

This project provides a reference implementation of the cryptographic operations
in SPXP in plain Java and accompanies the SPXP specification. It can either be
used to validate custom implementations or used as-is in your open source or
commercial projects.

All code in this Git Repository is licensed under the Apache License.

This project comes as a multi-module maven build. Building everything locally
works as usual:
```
$ git clone https://github.com/spxp/spxp-crypto
$ cd spxp-crypto
$ mvn package
```

## [spxp-crypto-sdk](./spxp-crypto-sdk)
This library implements the latest version 0.3 of SPXP in plain Java. It only
depends on `org.json` for JSON operations and on Bouncycastle for the
Ed25519 signature algorithm.

## [spxp-crypto-sdk-V02](./spxp-crypto-sdk-V02)
Version 0.2 of SPXP used plain JOSE for all cryptograhic operations and did not
make any limitations in the use of JOSE. Hence implementations can freely chose
what algorithm and encoding they use.  
To provide this freedom, this implementation relies on Apache's CXF libraries.
Since these libraries have a quite heavy footprint and version 0.2 is outdated
anyway, we chose to remove this implementation from the SDK and provide it as
an extra dependancy on top of the standard SDK.

## [spxp-crypto-tools](./spxp-crypto-tools)
This project provides a commandline application for the signing and encrypting
operations in SPXP.  
It can be used for example to manually create a SPXP profile by hand and to
validate the signatures of published profiles.