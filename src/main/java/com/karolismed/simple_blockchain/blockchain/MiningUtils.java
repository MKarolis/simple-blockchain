package com.karolismed.simple_blockchain.blockchain;

import com.karolismed.simple_blockchain.blockchain.model.transaction.Transaction;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static com.karolismed.simple_blockchain.constants.GlobalConstants.TRANSACTIONS_PER_BLOCK;

public class MiningUtils {
    public static long getCurrentTimestamp() {
        return new Timestamp(System.currentTimeMillis()).getTime();
    }

    public static List<Transaction> getRandomTransactionsForBlock(List<Transaction> transactionPool) {
        List<Transaction> transactions = new ArrayList<>();
        List<Transaction> transactionsLeftInPool = new LinkedList<>(transactionPool);

        if (transactionsLeftInPool.size() <= TRANSACTIONS_PER_BLOCK) {
            return transactionsLeftInPool;
        }

        for (int i = 0; i < TRANSACTIONS_PER_BLOCK; i++) {
            int randIndex = ThreadLocalRandom.current().nextInt(0, transactionsLeftInPool.size());
            transactions.add(transactionsLeftInPool.remove(randIndex));
        }

        return transactions;
    }
}
