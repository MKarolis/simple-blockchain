package com.karolismed.simple_blockchain.blockchain;

import com.karolismed.simple_blockchain.blockchain.model.User;
import com.karolismed.simple_blockchain.hashing.HashingService;

import java.security.PublicKey;
import java.security.Signature;
import java.util.Map;

import static com.karolismed.simple_blockchain.utils.StringHelper.log;

public class BlockChainRunner {

    private final HashingService hashingService;
    private final BlockChainSetupService setupService;

    private static final int USER_COUNT = 10;

    private Map<PublicKey, User> userMap;

    public BlockChainRunner() {
        hashingService = new HashingService();
        setupService = new BlockChainSetupService();
    }

    public void init() {
        log("Setting up BlockChain...");
        userMap = setupService.setupUsers(USER_COUNT);
    }

//    Signature sign = Signature.getInstance("SHA256withDSA");
//    byte[] signature = user.sign(sign, "labas".getBytes());
//                sign.initVerify(user.getPublicKey());
//
//                sign.update("labas".getBytes());
//                sign.verify(signature);
}
