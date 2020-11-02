package com.karolismed.hashfunction.hashing;

import static com.karolismed.hashfunction.hashing.HashingService.HASH_LENGTH;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

public class HashingServiceTest {

    HashingService hashingService;

    @Before
    public void setUp() {
        hashingService = new HashingService();
    }

    @Test
    public void hash_emptyString() {
        assertHashes("", "0");
    }

    @Test
    public void hash_singleCharacter() {
        String str1 = "A";
        String str2 = "B";

        assertHashes(str1, str2);
    }

    @Test
    public void hash_largeString_differsByOneChar() {
        String str1 = "abcdefghijklmn";
        String str2 = "aacdefghijklmn";

        assertHashes(str1, str2);
    }

    private void assertHashes(String str1, String str2) {
        assertThat(hashingService.hash(str1))
            .satisfies(hash -> assertThat(hash.length()).isEqualTo(HASH_LENGTH))
            .isEqualTo(hashingService.hash(str1));
        assertThat(hashingService.hash(str2))
            .satisfies(hash -> assertThat(hash.length()).isEqualTo(HASH_LENGTH))
            .isEqualTo(hashingService.hash(str2));

        assertThat(hashingService.hash(str1)).isNotEqualTo(hashingService.hash(str2));
    }
}
