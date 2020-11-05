package com.karolismed.simple_blockchain.blockchain.model.transaction;

import com.karolismed.simple_blockchain.hashing.HashingService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public class Transaction {
    private String txId;

    public Transaction(List<TransactionInput> inputs, List<TransactionOutput> outputs) {
        this.inputs = inputs;
        this.outputs = outputs;
        updateTxId();
    }

    private List<TransactionInput> inputs;
    private List<TransactionOutput> outputs;

    private void updateTxId() {
        HashingService hashingService = new HashingService();

        StringBuilder builder = new StringBuilder();
        Collections.sort(outputs);
        Collections.sort(inputs);
        outputs.forEach(
            output -> builder.append(output.getValue()).append(output.getDestPublicKey().toString())
        );
        inputs.forEach(
            input -> builder.append(input.getTxId()).append(input.getIndex()).append(input.getTxId())
        );
        builder.append(new Timestamp(System.currentTimeMillis()).getTime());

        txId = hashingService.hash(
            builder.toString()
        );
    }
}
