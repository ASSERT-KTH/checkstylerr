package com.healerjean.proj.cache.avengers.H01_hyperloglog.dto;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Experiment {
    private int n;
    private BitKeeper keeper;

    public Experiment(int n) {
        this.n = n;
        this.keeper = new BitKeeper();
    }

    public void work() {
        for (int i = 0; i < n; i++) {
            this.keeper.random();
        }
    }

    public void debug() {
        System.out.printf("%d %.2f %d\n", this.n, Math.log(this.n) / Math.log(2), this.keeper.maxbits);
    }

}
