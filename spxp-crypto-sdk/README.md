# SPXP Crypto SDK

This library is a reference implementation of the cryptographic operations in
SPXP in plain Java.

It uses `org.json:json` for JSON support and the Ed25519 implementation from
`org.bouncycastle:bcprov-jdk15on`.

## Building
This tool is built as part of the parent multi-module maven build. It can also
be built individually with the usual maven commands.

## Using
The best description as of now is probably the [SpxpCryptoTool](./../spxp-crypto-tool/src/main/java/org/spxp/crypto/tool/SpxpCryptoTool.java).