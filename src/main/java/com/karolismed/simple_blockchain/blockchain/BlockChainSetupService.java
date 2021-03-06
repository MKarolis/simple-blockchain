package com.karolismed.simple_blockchain.blockchain;

import com.karolismed.simple_blockchain.blockchain.model.User;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.List;
import java.util.Map;

import static com.karolismed.simple_blockchain.constants.GlobalConstants.USER_COUNT;
import static com.karolismed.simple_blockchain.utils.StringHelper.log;

public class BlockChainSetupService {

    public void setupUsers(List<User> users, Map<PublicKey, User> pkUserMap) {
        System.out.println(String.format("Setting up %s users", USER_COUNT));

        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DSA");
            keyPairGenerator.initialize(1024);

            for (int i = 0; i < USER_COUNT; i++) {
                User user = new User(keyPairGenerator.genKeyPair(), String.format("user_%s", i));
                pkUserMap.put(user.getPublicKey(), user);
                users.add(user);
            }
        } catch (NoSuchAlgorithmException ex) {
            log(String.format("ERROR: %s", ex.getMessage()));
            throw new RuntimeException(ex);
        }
    }
}
