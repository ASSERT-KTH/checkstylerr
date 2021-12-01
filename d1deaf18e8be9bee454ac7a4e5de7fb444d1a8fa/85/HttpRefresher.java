/**
 * Copyright (C) 2014 Couchbase, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALING
 * IN THE SOFTWARE.
 */
package com.couchbase.client.core.config.refresher;

import com.couchbase.client.core.ClusterFacade;
import com.couchbase.client.core.config.BucketConfig;
import com.couchbase.client.core.config.ClusterConfig;
import com.couchbase.client.core.config.ConfigurationException;
import com.couchbase.client.core.logging.CouchbaseLogger;
import com.couchbase.client.core.logging.CouchbaseLoggerFactory;
import com.couchbase.client.core.message.config.BucketStreamingRequest;
import com.couchbase.client.core.message.config.BucketStreamingResponse;
import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Keeps the bucket config fresh through a HTTP streaming connection.
 *
 * @author Michael Nitschinger
 * @since 1.0
 */
public class HttpRefresher extends AbstractRefresher {

    /**
     * The logger used.
     */
    private static final CouchbaseLogger LOGGER = CouchbaseLoggerFactory.getInstance(HttpRefresher.class);

    private static final String TERSE_PATH = "/pools/default/bs/";
    private static final String VERBOSE_PATH = "/pools/default/bucketsStreaming/";

    public HttpRefresher(final ClusterFacade cluster) {
        super(cluster);
    }

    @Override
    public Observable<Boolean> registerBucket(final String name, final String password) {
        Observable<BucketStreamingResponse> response = super.registerBucket(name, password).flatMap(new Func1<Boolean, Observable<BucketStreamingResponse>>() {
            @Override
            public Observable<BucketStreamingResponse> call(Boolean aBoolean) {
                return cluster()
                    .<BucketStreamingResponse>send(new BucketStreamingRequest(TERSE_PATH, name, password))
                    .doOnNext(new Action1<BucketStreamingResponse>() {
                        @Override
                        public void call(BucketStreamingResponse response) {
                            if (!response.status().isSuccess()) {
                                throw new ConfigurationException("Could not load terse config.");
                            }
                        }
                    });
            }
        }).onErrorResumeNext(new Func1<Throwable, Observable<BucketStreamingResponse>>() {
            @Override
            public Observable<BucketStreamingResponse> call(Throwable throwable) {
                return cluster()
                    .<BucketStreamingResponse>send(new BucketStreamingRequest(VERBOSE_PATH, name, password))
                    .doOnNext(new Action1<BucketStreamingResponse>() {
                        @Override
                        public void call(BucketStreamingResponse response) {
                            if (!response.status().isSuccess()) {
                                throw new ConfigurationException("Could not load terse config.");
                            }
                        }
                    });
            }
        });

        repeatConfigUntilUnsubscribed(name, response);

        return response.map(new Func1<BucketStreamingResponse, Boolean>() {
            @Override
            public Boolean call(BucketStreamingResponse response) {
                return response.status().isSuccess();
            }
        });
    }

    /**
     * Helper method to push configs until unsubscribed, even when a stream closes.
     *
     * @param name the name of the bucket.
     * @param response the response source observable to resubscribe if needed.
     */
    private void repeatConfigUntilUnsubscribed(final String name, Observable<BucketStreamingResponse> response) {
        response.flatMap(new Func1<BucketStreamingResponse, Observable<String>>() {
            @Override
            public Observable<String> call(final BucketStreamingResponse response) {
                LOGGER.debug("Config stream started for {} on {}.", name, response.host());

                return response
                    .configs()
                    .map(new Func1<String, String>() {
                        @Override
                        public String call(String s) {
                            return s.replace("$HOST", response.host());
                        }
                    })
                    .doOnCompleted(new Action0() {
                        @Override
                        public void call() {
                            LOGGER.debug("Config stream ended for {} on {}.", name, response.host());
                        }
                    });
            }
        })
        .repeatWhen(new Func1<Observable<? extends Void>, Observable<?>>() {
            @Override
            public Observable<?> call(Observable<? extends Void> observable) {
                return observable.flatMap(new Func1<Void, Observable<?>>() {
                    @Override
                    public Observable<?> call(Void aVoid) {
                        if (HttpRefresher.this.registrations().containsKey(name)) {
                            LOGGER.debug("Resubscribing config stream for bucket {}, still registered.", name);
                            return Observable.just(true);
                        } else {
                            LOGGER.debug("Not resubscribing config stream for bucket {}, not registered.", name);
                            return Observable.empty();
                        }
                    }
                });
            }
        }).subscribe(new Action1<String>() {
            @Override
            public void call(String rawConfig) {
                pushConfig(rawConfig);
            }
        });
    }

    @Override
    public Observable<Boolean> shutdown() {
        return null;
    }

    @Override
    public void markTainted(BucketConfig config) {

    }

    @Override
    public void markUntainted(BucketConfig config) {

    }

    @Override
    public void refresh(ClusterConfig config) {
    }
}
