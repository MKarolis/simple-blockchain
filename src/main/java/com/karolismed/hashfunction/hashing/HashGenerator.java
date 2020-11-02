package com.karolismed.hashfunction.hashing;

import static com.karolismed.hashfunction.hashing.HashingService.FREE_BUCKET_SIZE;

class HashGenerator {

    private static final int WORDS_PER_BUCKET = 64;
    private static final int WORD_GENERATION_LOWER_BOUND = 16;

    private int[] hashWords;
    private int hashWordIndex;

    public HashGenerator(int[] initHashWords) {
        hashWords = initHashWords.clone();
        hashWordIndex = 0;
    }

    public void processBucket(byte[] bytes, int startIndex) {
        int[] words = new int[WORDS_PER_BUCKET];
        addWordsFromExistingBytes(bytes, words, startIndex);
        generateAdditionalWords(words);

        mutateHashWords(words);
    }

    public String formatHash() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int word : hashWords) {
            stringBuilder.append(String.format("%08X", word));
        }
        return stringBuilder.toString();
    }

    private void addWordsFromExistingBytes(byte[] bytes, int[] words, int startIndex) {
        int wordsProcessed = 0;

        for (int i = startIndex; i < Math.min(bytes.length, startIndex + FREE_BUCKET_SIZE);) {
            for(int bi = Integer.BYTES - 1;
                bi >= 0 && i < Math.min(bytes.length, startIndex + FREE_BUCKET_SIZE);
                bi--, i++
            ) {
                words[wordsProcessed] |= bytes[i] << bi * Byte.SIZE;
            }

            wordsProcessed++;
        }

        words[WORD_GENERATION_LOWER_BOUND - 1] = bytes.length;
    }

    private void generateAdditionalWords(int[] words) {
        for (int i = WORD_GENERATION_LOWER_BOUND; i < WORDS_PER_BUCKET; i++) {
            words[i] = (op1(words[i - 1]) ^ op6(words[i - 1]))
                ^ op2(words[i - 2])
                ^ op4(words[i - 1])
                ^ op3(words[i - WORD_GENERATION_LOWER_BOUND])
                ^ (
                    op4((words[i - WORD_GENERATION_LOWER_BOUND + 1])
                        & op5(words[i - WORD_GENERATION_LOWER_BOUND + 1])) | op2(words[i - 2])
            );
        }
    }

    private void mutateHashWords(int[] words) {
        for (int word : words) {
            int hw1 = hashWords[(hashWordIndex + 1) % hashWords.length];
            int hw2 = hashWords[(hashWordIndex + 3) % hashWords.length];
            int hw3 = hashWords[(hashWordIndex + 6) % hashWords.length];

            int temp1 = op1(hw1)
                ^ ((~op5(hw2)) & (op2(word)))
                ^ op3(word);
            int temp2 = op5(hw2) ^ op2(
                op4(hw3) & (~op3(word))
            ) ^ op6(word);
            int temp3 = (op4(hw1) & op2(word))
                ^ op5(word)
                ^ op1(hw3);

            hashWords[hashWordIndex] = op1(temp1 ^ temp2) ^ op5(temp3 ^ op1(temp2));

            if (++hashWordIndex == hashWords.length) {
                hashWordIndex = 0;
            }
        }
    }

    private int op1(int word) {
        return Integer.rotateLeft(word, 8)
            ^ Integer.rotateRight(word, 16)
            ^ (word >>> 12);
    }

    private int op2(int word) {
        return word
            ^ Integer.rotateLeft(word, 6)
            ^ Integer.rotateRight(word, 12)
            ^ Integer.rotateLeft(word, 15);
    }

    private int op3(int word) {
        return word
            ^ (Integer.rotateRight(word, 21))
            ^ (word << 7);
    }

    private int op4(int word) {
        return word
            ^ ~(word << 12)
            ^ ~(word >>> 12);
    }

    private int op5(int word) {
        return Integer.rotateRight(word, 8)
            ^ (word << 12)
            ^ (word >>> 16);
    }

    private int op6(int word) {
        return (word << 27)
            | (word >>> 27)
            ^ (word >>> 18)
            ^ (word << 18);
    }
}
