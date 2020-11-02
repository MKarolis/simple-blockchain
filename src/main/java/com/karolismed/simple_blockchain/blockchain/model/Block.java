package com.karolismed.simple_blockchain.blockchain.model;

import com.karolismed.simple_blockchain.hashing.HashingService;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import static com.karolismed.simple_blockchain.constants.GlobalConstants.BLOCKCHAIN_VERSION;

@ToString
@EqualsAndHashCode
@Getter
public class Block {
    private String hash;
    private String data;
    private BlockHeader header;

    public Block(
        String prevBlockHash,
        long timestamp,
        int nonce,
        int difficulty,
        String data
    ) {
        this.header = BlockHeader.builder()
            .difficulty(difficulty)
            .merkleRootHash(data) // TODO, change
            .nonce(nonce)
            .prevBlockHash(prevBlockHash)
            .timestamp(timestamp)
            .version(BLOCKCHAIN_VERSION)
            .build();

        this.data = data;
        this.hash = computeHash();
    }

    public Block(BlockHeader header, String data) {
        this.header = header;
        this.data = data;
        this.hash = computeHash();
    }

    public String computeHash() {
        HashingService hashingService = new HashingService();

        return hashingService.hash(
            header.toString()
        );
    }
}
