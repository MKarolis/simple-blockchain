package com.karolismed.simple_blockchain.blockchain.model.transaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.security.PublicKey;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionOutput implements Comparable<TransactionOutput> {
    private int value;
    private PublicKey destPublicKey;

    @Override
    public int compareTo(TransactionOutput other) {
        if (other.destPublicKey.equals(this.destPublicKey)) {
            return this.value - other.value;
        }
        return this.destPublicKey.hashCode() - other.hashCode();
    }
}
