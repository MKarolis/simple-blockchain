package com.karolismed.simple_blockchain.blockchain;

import com.karolismed.simple_blockchain.blockchain.model.block.Block;
import com.karolismed.simple_blockchain.blockchain.model.transaction.Transaction;
import com.karolismed.simple_blockchain.hashing.HashingService;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;

import static com.karolismed.simple_blockchain.blockchain.MiningUtils.getCurrentTimestamp;
import static com.karolismed.simple_blockchain.blockchain.MiningUtils.getRandomTransactionsForBlock;
import static com.karolismed.simple_blockchain.constants.GlobalConstants.BLOCKCHAIN_VERSION;
import static com.karolismed.simple_blockchain.utils.StringHelper.log;
import static java.util.Objects.isNull;

public class Miner implements Runnable {

    private final List<Transaction> transactionPool;
    private final BlockingQueue<Pair<Block, Integer>> blockQueue;
    private final Semaphore threadSyncSemaphore;

    private final MerkleTreeConstructor merkleTreeConstructor;
    private final HashingService hashingService;

    private volatile boolean isRunning = true;
    private volatile boolean isPaused = true;
    private volatile boolean holdsSemaphoreLock = false;
    @Getter
    private final String name;
    @Getter
    private final int index;

    private volatile String prevBlockHash;
    private volatile String expectedPrefix;
    private volatile int difficulty;
    private volatile List<Transaction> pickedTransactions;

    private volatile long tempTimestamp;
    private volatile int tempHashCounter;
    private volatile String tempBaseStr;


    public void stopExecution() {
        isRunning = false;
    }

    public Miner(String name, List<Transaction> transactionPool, int index, BlockingQueue<Pair<Block, Integer>> blockQueue, Semaphore threadSyncSemaphore) {
        this.transactionPool = transactionPool;
        this.name = name;
        this.merkleTreeConstructor = new MerkleTreeConstructor();
        this.hashingService = new HashingService();
        this.index = index;
        this.blockQueue = blockQueue;
        this.threadSyncSemaphore = threadSyncSemaphore;
    }

    @Override
    public void run() {
        log(name + " is powered up");

        while (this.isRunning) {
            while (this.isPaused && this.isRunning && !holdsSemaphoreLock) {
                // Do nothing, execution is paused
            }
            if (holdsSemaphoreLock) {
                holdsSemaphoreLock = false;
                threadSyncSemaphore.release();
                continue;
            }

            if (!this.isRunning) continue;

            int nonce = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE);

            if (isNull(tempBaseStr) || tempHashCounter > 1000) {
                tempTimestamp = getCurrentTimestamp();
                tempBaseStr = difficulty +
                    merkleTreeConstructor.getMerkleTreeRoot(pickedTransactions) +
                    prevBlockHash +
                    tempTimestamp +
                    BLOCKCHAIN_VERSION;
                tempHashCounter = 0;
            }

            if (hashingService.hash(tempBaseStr + nonce).substring(0, difficulty).equals(expectedPrefix)) {
                handleBlockMinedSuccessfully(nonce);
            }
        }

        log(name + " is shutting down");
    }

    public void mineNextBlock(String prevBlockHash, int difficulty) {
        this.expectedPrefix = "0".repeat(difficulty);
        this.prevBlockHash = prevBlockHash;
        this.difficulty = difficulty;
        this.pickedTransactions = getRandomTransactionsForBlock(transactionPool);
        this.tempHashCounter = 0;
        this.tempBaseStr = null;
        this.tempTimestamp = 0;
        this.isPaused = false;
        this.holdsSemaphoreLock = true;
    }

    public void pauseMining() {
        this.isPaused = true;
        this.holdsSemaphoreLock = true;
    }

    private void handleBlockMinedSuccessfully(int nonce) {
        Block minedBlock = new Block(
            prevBlockHash,
            tempTimestamp,
            nonce,
            difficulty,
            pickedTransactions
        );

        try {
            this.blockQueue.put(Pair.of(minedBlock, index));
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

        this.isPaused = true;
    }

    @Override
    public String toString() {
        return name;
    }
}
