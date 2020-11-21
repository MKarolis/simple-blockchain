package com.karolismed.simple_blockchain.blockchain.model.block;

import com.karolismed.simple_blockchain.blockchain.MerkleTreeConstructor;
import com.karolismed.simple_blockchain.blockchain.model.transaction.Transaction;
import com.karolismed.simple_blockchain.hashing.HashingService;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import java.util.List;

import static com.karolismed.simple_blockchain.constants.GlobalConstants.BLOCKCHAIN_VERSION;

@EqualsAndHashCode
@Getter
public class Block {
    private String hash;
    private List<Transaction> transactions;
    private BlockHeader header;

    public Block(
        String prevBlockHash,
        long timestamp,
        int nonce,
        int difficulty,
        List<Transaction> transactions
    ) {
        MerkleTreeConstructor merkleTreeConstructor = new MerkleTreeConstructor();
        this.header = BlockHeader.builder()
            .difficulty(difficulty)
            .merkleRootHash(merkleTreeConstructor.getMerkleTreeRoot(transactions))
            .nonce(nonce)
            .prevBlockHash(prevBlockHash)
            .timestamp(timestamp)
            .version(BLOCKCHAIN_VERSION)
            .build();

        this.transactions = transactions;
        this.hash = computeHash();
    }

    public String computeHash() {
        HashingService hashingService = new HashingService();

        return hashingService.hash(
            header.toString()
        );
    }

    @Override
    public String toString() {
        return String.format(
            "{hash=%s, merkleRoot=%s transactionCount=%s, difficulty=%s, timestamp=%s, nonce=%s }",
            hash, header.getMerkleRootHash(), transactions.size(), header.getDifficulty(), header.getTimestamp(), header.getNonce()
        );
    }
}
