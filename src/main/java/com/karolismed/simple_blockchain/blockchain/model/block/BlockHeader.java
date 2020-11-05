package com.karolismed.simple_blockchain.blockchain.model.block;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
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
        return new StringBuilder()
            .append(difficulty)
            .append(merkleRootHash)
            .append(prevBlockHash)
            .append(timestamp)
            .append(version)
            .append(nonce)
            .toString();
    }
}
