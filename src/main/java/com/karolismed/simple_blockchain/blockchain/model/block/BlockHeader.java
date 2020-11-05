package com.karolismed.simple_blockchain.blockchain.model.block;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BlockHeader {
    private String prevBlockHash;
    private long timestamp;
    private String version;
    private String merkleRootHash;
    private int nonce;
    private int difficulty;

    @Override
    public String toString() {
        return difficulty +
            merkleRootHash +
            prevBlockHash +
            timestamp +
            version +
            nonce;
    }
}
