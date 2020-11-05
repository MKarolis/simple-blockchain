package com.karolismed.simple_blockchain.blockchain;

import com.karolismed.simple_blockchain.blockchain.model.block.Block;
import com.karolismed.simple_blockchain.blockchain.model.User;
import com.karolismed.simple_blockchain.blockchain.model.transaction.Transaction;
import com.karolismed.simple_blockchain.blockchain.model.transaction.TransactionOutput;
import com.karolismed.simple_blockchain.blockchain.model.transaction.UnspentOutput;
import com.karolismed.simple_blockchain.hashing.HashingService;

import java.security.PublicKey;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;

/*
unspent output
    transaction id
    index
    value

transaction
    txId,
    inputs [
        { // TransactionInput
            txId,
            index,
            signedTxId,
        }
    ],
    outputs [
        { // Transaction Output
            value,
            destPubKey
        }
    ]

 */

import static com.karolismed.simple_blockchain.constants.GlobalConstants.BLOCKCHAIN_VERSION;
import static com.karolismed.simple_blockchain.utils.StringHelper.log;

public class BlockChainRunner {

    private final HashingService hashingService;
    private final BlockChainSetupService setupService;

    // TODO: Investiagte possibility to add transaction/block cache

    private List<User> users;
    private List<Block> blockChain;
    private List<Transaction> unverifiedTransactions;
    private MultiValuedMap<PublicKey, UnspentOutput> unspentOutputMap;

    public BlockChainRunner() {
        hashingService = new HashingService();
        setupService = new BlockChainSetupService();

        blockChain = new LinkedList<>();
        users = new ArrayList<>();
        unverifiedTransactions = new LinkedList<>();
        unspentOutputMap = new HashSetValuedHashMap<>();
    }

    public void init() {
        log("Setting up BlockChain...");
        setupService.setupUsers(users);
        log("Mining genesis block...");
        mineGenesisBlock();

        setupService.setupTransactionPool(unverifiedTransactions, users);

        while(blockChain.size() < 100) {
            mineBlock(4, "Block " + blockChain.size());
        }
    }

    private void mineGenesisBlock() {
        List<TransactionOutput> transactionOutputs = new ArrayList<>();

        users.forEach(user -> {
            transactionOutputs.add(
                TransactionOutput.builder().destPublicKey(user.getPublicKey())
                    .value(ThreadLocalRandom.current().nextInt(10000, 1000000)) // TODO Change lower bound
                    .build()
            );
        });

        Transaction transaction = new Transaction(new ArrayList<>(), transactionOutputs);
        Block genesisBlock = mineBlock(1, List.of(transaction), "0".repeat(64));
        blockChain.add(genesisBlock);

        for (int i = 0; i < users.size(); i++) {
            unspentOutputMap.put(
                users.get(i).getPublicKey(),
                UnspentOutput.builder()
                    .destPublicKey(users.get(i).getPublicKey())
                    .txId(transaction.getTxId())
                    .outputIndex(i)
                    .build()
            );
        }
    }

    private Block mineBlock(int difficulty, List<Transaction> transactions, String prevBlockHash) {
        String expectedPrefix = "0".repeat(difficulty);

        int nonce = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE);
        long timestamp = getCurrentTimestamp();

        String baseStr = difficulty +
            transactions.toString() + // Merkle Root
            prevBlockHash +
            timestamp +
            BLOCKCHAIN_VERSION;

        String currentHash = hashingService.hash(baseStr + nonce);
        int counter = 0;

        while (!currentHash.substring(0, difficulty).equals(expectedPrefix)) {
            nonce = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE);
            counter++;

            if (counter >= 10) {
                timestamp = getCurrentTimestamp();
                baseStr = difficulty +
                    transactions.toString() + // Merkle Root
                    prevBlockHash +
                    timestamp +
                    BLOCKCHAIN_VERSION;
                counter = 0;
            }
            currentHash = hashingService.hash(baseStr + nonce);
        }

        return new Block(
            prevBlockHash,
            timestamp,
            nonce,
            difficulty,
            transactions
        );
    }

    private void mineBlock(int difficulty, String data) {
        String expectedPrefix = "0".repeat(difficulty);
        String prevBlockHash = blockChain.get(blockChain.size() - 1).computeHash();

        int nonce = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE);
        long timestamp = getCurrentTimestamp();

        String baseStr = difficulty +
            List.of().toString() + // Merkle Root
            prevBlockHash +
            timestamp +
            BLOCKCHAIN_VERSION;

        String currentHash = hashingService.hash(baseStr + nonce);
        int counter = 0;

        while (!currentHash.substring(0, difficulty).equals(expectedPrefix)) {
            nonce = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE);
            counter++;

            if (counter >= 10) {
                timestamp = getCurrentTimestamp();
                baseStr = difficulty +
                    List.of().toString() + // Merkle Root
                    prevBlockHash +
                    timestamp +
                    BLOCKCHAIN_VERSION;
                counter = 0;
            }
            currentHash = hashingService.hash(baseStr + nonce);
        }

        Block newBlock = new Block(
            prevBlockHash,
            timestamp,
            nonce,
            difficulty,
            List.of() // TODO fix
        );

        blockChain.add(newBlock);
        System.out.println("Mined Block: \n" + newBlock.toString());
    }

    private long getCurrentTimestamp() {
        return new Timestamp(System.currentTimeMillis()).getTime();
    }

//    Signature sign = Signature.getInstance("SHA256withDSA");
//    byte[] signature = user.sign(sign, "labas".getBytes());
//                sign.initVerify(user.getPublicKey());
//
//                sign.update("labas".getBytes());
//                sign.verify(signature);
}
