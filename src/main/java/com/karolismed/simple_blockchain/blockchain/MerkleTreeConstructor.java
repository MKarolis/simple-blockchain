package com.karolismed.simple_blockchain.blockchain;

import com.karolismed.simple_blockchain.blockchain.model.transaction.Transaction;
import com.karolismed.simple_blockchain.hashing.HashingService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MerkleTreeConstructor {

    private HashingService hashingService;

    public MerkleTreeConstructor() {
        this.hashingService = new HashingService();
    }

    public String getMerkleTreeRoot(List<Transaction> transactions) {
        List<String> currentLevel =
            transactions.stream().map(Transaction::getTxId).collect(Collectors.toList());

        while (currentLevel.size() > 1) {
            List<String> temp = new ArrayList<>();
            for (int i = 0; i < currentLevel.size(); i += 2) {
                if (i == currentLevel.size() - 1) {
                    temp.add(hashingService.hash(currentLevel.get(i)));
                } else {
                    temp.add(hashingService.hash(currentLevel.get(i) + currentLevel.get(i + 1)));
                }
            }
            currentLevel = temp;
        }

        return currentLevel.get(0);
    }
}
