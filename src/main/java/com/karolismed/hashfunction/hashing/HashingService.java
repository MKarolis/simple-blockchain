package com.karolismed.hashfunction.hashing;

import java.nio.charset.StandardCharsets;

public class HashingService {
    public static final int HASH_LENGTH = 64;
    static final int FREE_BUCKET_SIZE = 60;

    static final int[] HASH_SEED = {
            0b11000011011111010011011000110000,
            0b10011010101100100100110010010010,
            0b01001001101010101101100100101100,
            0b10100011000100010011011011010101,
            0b10001111011001101101001001001011,
            0b10111011000100110010010010100010,
            0b01010001010001110010000101010110,
            0b11101111001011011010110110011011
    };

    public String hash(String input) {
        HashGenerator generator = new HashGenerator(HASH_SEED);

        byte[] inputBytes =  input.getBytes(StandardCharsets.UTF_8);
        int bucketCount = (int)Math.ceil(inputBytes.length / (double) FREE_BUCKET_SIZE);

        int bucketIndex = 0;
        do {
            generator.processBucket(inputBytes, FREE_BUCKET_SIZE * bucketIndex);
        } while (++bucketIndex < bucketCount);

        return generator.formatHash();
    }
}
