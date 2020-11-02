package com.karolismed.simple_blockchain.blockchain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;

@ToString
@EqualsAndHashCode
public class User {
    private String name;
    @Getter
    private PublicKey publicKey;
    private PrivateKey privateKey;

    public User(KeyPair pair, String name) {
        this.privateKey = pair.getPrivate();
        this.publicKey = pair.getPublic();

        this.name = name;
    }

    public byte[] sign(Signature signature, byte[] data)
        throws InvalidKeyException, SignatureException
    {
        signature.initSign(privateKey);
        signature.update(data);
        return signature.sign();
    }
}
