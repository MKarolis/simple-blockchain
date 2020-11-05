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
public class UnspentOutput {
    private PublicKey destPublicKey;
    private String txId;
    private int outputIndex;
}
