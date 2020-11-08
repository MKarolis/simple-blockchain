package com.karolismed.simple_blockchain.blockchain.model.transaction;

import com.karolismed.simple_blockchain.hashing.HashingService;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Getter;

@Getter
public class Transaction {
    private String txId;
    private long creationTimeStamp;

    public Transaction(List<TransactionInput> inputs, List<TransactionOutput> outputs) {
        this.inputs = new ArrayList<>(inputs);
        this.outputs = new ArrayList<>(outputs);

        Collections.sort(outputs);
        Collections.sort(inputs);

        creationTimeStamp = new Timestamp(System.currentTimeMillis()).getTime();
        txId = computeTransactionId();
    }

    private List<TransactionInput> inputs;
    private List<TransactionOutput> outputs;

    public String computeTransactionId() {
        HashingService hashingService = new HashingService();

        StringBuilder builder = new StringBuilder();
        outputs.forEach(
            output -> builder.append(output.getValue()).append(output.getDestPublicKey().toString())
        );
        inputs.forEach(
            input -> builder.append(input.getTxId()).append(input.getIndex()).append(input.getTxId())
        );
        builder.append(creationTimeStamp);

        return hashingService.hash(
            builder.toString()
        );
    }

    @Override
    public String toString() {
        return txId;
    }
}
