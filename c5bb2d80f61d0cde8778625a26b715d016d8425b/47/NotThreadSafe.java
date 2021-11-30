package com.sample;

public class NotThreadSafe {

    StringBuilder builder = new StringBuilder();

    public void add(String text) {

        synchronized (this) {
            this.builder.append(text);
        }
    }

    @Override
    public String toString() {
        return builder.toString();
    }
}