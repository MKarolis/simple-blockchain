package com.karolismed.simple_blockchain.blockchain.model.block;

import com.karolismed.simple_blockchain.blockchain.model.transaction.Transaction;
import com.karolismed.simple_blockchain.hashing.HashingService;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

import static com.karolismed.simple_blockchain.constants.GlobalConstants.BLOCKCHAIN_VERSION;

@ToString
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
        this.header = BlockHeader.builder()
            .difficulty(difficulty)
            .merkleRootHash(transactions.toString()) // TODO, change
            .nonce(nonce)
            .prevBlockHash(prevBlockHash)
            .timestamp(timestamp)
            .version(BLOCKCHAIN_VERSION)
            .build();

        this.transactions = transactions;
        this.hash = computeHash();
    }

    public Block(BlockHeader header, List<Transaction> transactions) {
        this.header = header;
        this.transactions = transactions;
        this.hash = computeHash();
    }

    public String computeHash() {
        HashingService hashingService = new HashingService();

        return hashingService.hash(
            header.toString()
        );
    }
}
