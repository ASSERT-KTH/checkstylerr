package com.healerjean.proj.cache.avengers.H01_hyperloglog.dto;

import java.util.concurrent.ThreadLocalRandom;

public class BitKeeper {

    public int maxbits;
    public void random() {
        long value = ThreadLocalRandom.current().nextLong(2L << 32);
        int bits = lowZeros(value);
        if (bits > this.maxbits) {
            this.maxbits = bits;
        }
    }

    private int lowZeros(long value) {
        int i = 1;

        for (; i < 32; i++) {
            if (value >> i << i != value) {
                break;
            }
        }
        return i - 1;
    }
}
