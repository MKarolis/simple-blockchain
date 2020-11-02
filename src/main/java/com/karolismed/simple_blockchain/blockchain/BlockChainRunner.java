package com.karolismed.simple_blockchain.blockchain;

import com.karolismed.simple_blockchain.blockchain.model.Block;
import com.karolismed.simple_blockchain.blockchain.model.BlockHeader;
import com.karolismed.simple_blockchain.blockchain.model.User;
import com.karolismed.simple_blockchain.hashing.HashingService;

import java.security.PublicKey;
import java.security.Signature;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static com.karolismed.simple_blockchain.constants.GlobalConstants.BLOCKCHAIN_VERSION;
import static com.karolismed.simple_blockchain.utils.StringHelper.log;

public class BlockChainRunner {

    private final HashingService hashingService;
    private final BlockChainSetupService setupService;

    private Map<PublicKey, User> userMap;
    private List<Block> blockChain;

    public BlockChainRunner() {
        hashingService = new HashingService();
        setupService = new BlockChainSetupService();
        blockChain = new ArrayList<>();
    }

    public void init() {
        log("Setting up BlockChain...");
        userMap = setupService.setupUsers();

        Block genesisBlock = new Block(
            "0".repeat(64),
            getCurrentTimestamp(),
            0,
            0,
            "genesis"
        );

        blockChain.add(genesisBlock);

        while(blockChain.size() < 1000) {
            mineBlock(6, "Block " + blockChain.size());
        }
    }

    private void mineBlock(int difficulty, String data) {
        String expectedPrefix = "0".repeat(difficulty);
        String prevBlockHash = blockChain.get(blockChain.size() - 1).computeHash();

        int nonce = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE);
        long timestamp = getCurrentTimestamp();

        String baseStr = difficulty +
            data + // Merkle Root
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
                    data + // Merkle Root
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
            data
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
