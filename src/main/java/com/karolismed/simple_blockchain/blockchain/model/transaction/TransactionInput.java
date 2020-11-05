package com.karolismed.simple_blockchain.blockchain.model.transaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionInput implements Comparable<TransactionInput> {
    private String txId;
    private int index;
    private String signedTxId;

    @Override
    public int compareTo(TransactionInput other) {
        if (this.txId.equals(other.txId)) {
            return this.index - other.index;
        }
        return this.txId.compareTo(other.txId);
    }
}
