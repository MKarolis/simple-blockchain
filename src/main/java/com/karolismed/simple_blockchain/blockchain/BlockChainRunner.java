package com.karolismed.simple_blockchain.blockchain;

import com.karolismed.simple_blockchain.blockchain.model.block.Block;
import com.karolismed.simple_blockchain.blockchain.model.User;
import com.karolismed.simple_blockchain.blockchain.model.transaction.Transaction;
import com.karolismed.simple_blockchain.blockchain.model.transaction.TransactionInput;
import com.karolismed.simple_blockchain.blockchain.model.transaction.TransactionOutput;
import com.karolismed.simple_blockchain.blockchain.model.transaction.UnspentOutput;
import com.karolismed.simple_blockchain.hashing.HashingService;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.apache.commons.lang3.tuple.Pair;

import static com.karolismed.simple_blockchain.constants.GlobalConstants.BLOCKCHAIN_VERSION;
import static com.karolismed.simple_blockchain.constants.GlobalConstants.TRANSACTIONS_PER_BLOCK;
import static com.karolismed.simple_blockchain.constants.GlobalConstants.TRANSACTION_POOL_SIZE;
import static com.karolismed.simple_blockchain.utils.StringHelper.log;
import static java.util.Objects.isNull;

public class BlockChainRunner {

    private final HashingService hashingService;
    private final BlockChainSetupService setupService;

    private List<User> users;
    private List<Block> blockChain;
    private List<Transaction> unverifiedTransactions;
    private MultiValuedMap<PublicKey, UnspentOutput> unspentOutputMap;
    private Map<String, Integer> txIdBlockIndexMap;
    private Map<PublicKey, User> pkUserMap;

    private int totalTransactionsPooled;

    public BlockChainRunner() {
        hashingService = new HashingService();
        setupService = new BlockChainSetupService();

        blockChain = new LinkedList<>();
        users = new ArrayList<>();
        unverifiedTransactions = new LinkedList<>();
        unspentOutputMap = new HashSetValuedHashMap<>();
        txIdBlockIndexMap = new HashMap<>();
        pkUserMap = new HashMap<>();
    }

    public void init() throws Exception {
        log("Setting up BlockChain...");
        setupService.setupUsers(users, pkUserMap);
        log("Mining genesis block...");
        mineGenesisBlock();

        addTransactionsToPool();

        log("Mining other blocks...");
        while(!unverifiedTransactions.isEmpty()) {
            Block newBlock = mineBlock(4, getRandomTransactionsForBlock(), blockChain.get(blockChain.size() - 1).getHash());
            log("Mined block " + newBlock);
            blockChain.add(newBlock);
            addTransactionsToPool();
        }

        log("Block chain simulation ended");
    }

    private void mineGenesisBlock() {
        List<TransactionOutput> transactionOutputs = new ArrayList<>();

        users.forEach(user -> {
            transactionOutputs.add(
                TransactionOutput.builder().destPublicKey(user.getPublicKey())
                    .value(ThreadLocalRandom.current().nextInt(1, 1000000))
                    .build()
            );
        });

        Transaction transaction = new Transaction(new ArrayList<>(), transactionOutputs);
        Block genesisBlock = mineBlock(1, List.of(transaction), "0".repeat(64));
        blockChain.add(genesisBlock);
    }

    private Block mineBlock(int difficulty, List<Transaction> transactions, String prevBlockHash) {
        String expectedPrefix = "0".repeat(difficulty);
        MerkleTreeConstructor merkleTreeConstructor = new MerkleTreeConstructor();

        int nonce = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE);
        long timestamp = getCurrentTimestamp();

        String baseStr = difficulty +
            merkleTreeConstructor.getMerkleTreeRoot(transactions) +
            prevBlockHash +
            timestamp +
            BLOCKCHAIN_VERSION;

        String currentHash = hashingService.hash(baseStr + nonce);
        int counter = 0;

        while (!currentHash.substring(0, difficulty).equals(expectedPrefix)) {
            nonce = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE);
            counter++;

            if (counter >= 1000) {
                timestamp = getCurrentTimestamp();
                baseStr = difficulty +
                    merkleTreeConstructor.getMerkleTreeRoot(transactions) +
                    prevBlockHash +
                    timestamp +
                    BLOCKCHAIN_VERSION;
                counter = 0;
            }
            currentHash = hashingService.hash(baseStr + nonce);
        }

        Block minedBlock = new Block(
            prevBlockHash,
            timestamp,
            nonce,
            difficulty,
            transactions
        );

        transactions.forEach(transaction -> {
            txIdBlockIndexMap.put(transaction.getTxId(), blockChain.size());
            for (int i = 0; i < transaction.getOutputs().size(); i++) {
                TransactionOutput output = transaction.getOutputs().get(i);
                unspentOutputMap.put(
                    output.getDestPublicKey(),
                    UnspentOutput.builder()
                        .destPublicKey(output.getDestPublicKey())
                        .txId(transaction.getTxId())
                        .outputIndex(i)
                        .build()
                );
            }
        });
        unverifiedTransactions.removeAll(transactions);

        return minedBlock;
    }

    private long getCurrentTimestamp() {
        return new Timestamp(System.currentTimeMillis()).getTime();
    }

    private void addTransactionsToPool() throws NoSuchAlgorithmException {
        if (totalTransactionsPooled >= TRANSACTION_POOL_SIZE) return;
        int userCount = users.size();
        List<Pair<PublicKey, UnspentOutput>> mappingsToRemove = new ArrayList<>();

        Signature sign = Signature.getInstance("SHA256withDSA");

        unspentOutputMap.keys().forEach(userKey -> {
            Collection<UnspentOutput> userUnspentOutputs = new ArrayList<>(unspentOutputMap.get(userKey));
            int balance = calculateUserBalance(userKey);
            if (balance < 2) return;

            int sumToSpend = ThreadLocalRandom.current().nextInt(1, balance);

            List<TransactionInput> inputs = new ArrayList<>();
            int sumAlreadySpend = 0;
            User currentUser = pkUserMap.get(userKey);

            Iterator<UnspentOutput> outputIterator = userUnspentOutputs.iterator();
            while (sumAlreadySpend < sumToSpend && outputIterator.hasNext()) {
                UnspentOutput output = outputIterator.next();
                sumAlreadySpend += getUnspentOutputValue(output);

                mappingsToRemove.add(Pair.of(userKey, output));
                inputs.add(
                    TransactionInput.builder()
                        .index(output.getOutputIndex())
                        .txId(output.getTxId())
                        .signedTxId(currentUser.sign(sign, output.getTxId().getBytes()))
                        .build()
                );
            }

            List<TransactionOutput> outputs = new ArrayList<>();
            int destUserIndex = ThreadLocalRandom.current().nextInt(0, userCount);
            User destUser = users.get(destUserIndex);
            outputs.add(
                TransactionOutput.builder().value(sumToSpend).destPublicKey(destUser.getPublicKey()).build()
            );
            if (sumAlreadySpend > sumToSpend) {
                outputs.add(
                    TransactionOutput.builder().value(sumAlreadySpend - sumToSpend).destPublicKey(userKey).build()
                );
            }

            unverifiedTransactions.add(new Transaction(inputs, outputs));
            totalTransactionsPooled++;
        });

        mappingsToRemove.forEach(mapping -> {
            unspentOutputMap.removeMapping(mapping.getKey(), mapping.getValue());
        });
    }

    private List<Transaction> getRandomTransactionsForBlock()
        throws NoSuchAlgorithmException, SignatureException, InvalidKeyException
    {
        Signature sign = Signature.getInstance("SHA256withDSA");
        List<Transaction> transactions = new ArrayList<>();
        List<Transaction> transactionsLeftInPool = new LinkedList<>(unverifiedTransactions);

        if (transactionsLeftInPool.size() <= TRANSACTIONS_PER_BLOCK) {
            return transactionsLeftInPool;
        }

        for (int i = 0; i < TRANSACTIONS_PER_BLOCK; i++) {
            boolean isValid = true;
            int randIndex = ThreadLocalRandom.current().nextInt(0, transactionsLeftInPool.size());
            Transaction transactionToAdd = transactionsLeftInPool.remove(randIndex);

            for (int x = 0; x < transactionToAdd.getInputs().size(); x++) {
                TransactionInput input = transactionToAdd.getInputs().get(x);

                Transaction inputTransaction = getTransactionFromBlockChainById(input.getTxId());
                TransactionOutput sourceOutput = inputTransaction.getOutputs().get(input.getIndex());

                sign.initVerify(sourceOutput.getDestPublicKey());
                sign.update(inputTransaction.getTxId().getBytes());
                isValid = sign.verify(input.getSignedTxId());
                if (!isValid) {
                    log("Invalid Transaction detected: " + transactionToAdd.getTxId());
                    break;
                }
            }

            if (isValid) {
                transactions.add(transactionToAdd);
            }
        }

        return transactions;
    }

    private int getUnspentOutputValue(UnspentOutput output) {
        Transaction transaction = getTransactionFromBlockChainById(output.getTxId());
        if (isNull(transaction) || transaction.getOutputs().size() <= output.getOutputIndex()) {
            return 0;
        }
        return transaction.getOutputs().get(output.getOutputIndex()).getValue();
    }

    private int calculateUserBalance(PublicKey userPublicKey) {
        Collection<UnspentOutput> userUnspentOutputs = unspentOutputMap.get(userPublicKey);
        if (isNull(userUnspentOutputs) || userUnspentOutputs.isEmpty()) {
            return 0;
        }

        return userUnspentOutputs.stream()
            .mapToInt(this::getUnspentOutputValue)
            .sum();
    }

    private Transaction getTransactionFromBlockChainById(String txId) {
        Block block = blockChain.get(txIdBlockIndexMap.get(txId));
        return block.getTransactions().stream().filter(tx -> tx.getTxId().equals(txId))
            .findFirst().orElse(null);
    }
}
