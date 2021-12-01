/**
 * Copyright 2016 Flipkart Internet Pvt. Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.flipkart.storm.mysql;

/**
 * The transaction event with the retry count.
 */
public class RetryTransactionEvent {

    private final TransactionEvent  txEvent;
    private final int               numRetries;
    private final long              timeWhenEmitted;

    /**
     * Instatiate a transaction event with the number of retries already done.
     *
     * @param txEvent the transaction event
     * @param numRetries the num retries
     * @param timeWhenEmitted time when this event was emitted from then spout.
     */
    public RetryTransactionEvent(TransactionEvent txEvent, int numRetries, long timeWhenEmitted) {
        this.txEvent    = txEvent;
        this.numRetries = numRetries;
        this.timeWhenEmitted = timeWhenEmitted;
    }

    public TransactionEvent getTxEvent() {
        return txEvent;
    }

    public int getNumRetries() {
        return numRetries;
    }

    public long getTimeWhenEmitted() { return timeWhenEmitted; }

    @Override
    public String toString() {
        return "RetryTransactionEvent{" +
                "txEvent=" + txEvent +
                ", numRetries=" + numRetries +
                ", timeWhenEmitted=" + timeWhenEmitted +
                '}';
    }
}
