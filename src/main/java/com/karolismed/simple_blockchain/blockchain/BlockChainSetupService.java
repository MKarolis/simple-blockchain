package com.karolismed.simple_blockchain.blockchain;

import com.karolismed.simple_blockchain.blockchain.model.User;
import com.karolismed.simple_blockchain.blockchain.model.transaction.Transaction;

import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static com.karolismed.simple_blockchain.constants.GlobalConstants.TRANSACTION_POOL_SIZE;
import static com.karolismed.simple_blockchain.constants.GlobalConstants.USER_COUNT;
import static com.karolismed.simple_blockchain.utils.StringHelper.log;

public class BlockChainSetupService {

    public void setupUsers(List<User> users) {
        System.out.println(String.format("Setting up %s users", USER_COUNT));

        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DSA");
            keyPairGenerator.initialize(1024);

            for (int i = 0; i < USER_COUNT; i++) {
                User user = new User(keyPairGenerator.genKeyPair(), String.format("user_%s", i));
                users.add(user);
            }
        } catch (NoSuchAlgorithmException ex) {
            log(String.format("ERROR: %s", ex.getMessage()));
            throw new RuntimeException(ex);
        }
    }

    public void setupTransactionPool(List<Transaction> transactions, List<User> users) {
        int userCount = users.size();
        for (int i = 0; i < TRANSACTION_POOL_SIZE; i++) {
            int userIndex = ThreadLocalRandom.current().nextInt(0, userCount);

        }
    }
}
