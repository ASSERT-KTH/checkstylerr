/*
 * Copyright (c) 2015 Couchbase, Inc.
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
package com.couchbase.client.core.env.resources;

import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import rx.Observable;
import rx.Subscriber;

/**
 * {@link ShutdownHook} hook for an {@link EventLoopGroup}.
 *
 * @author Simon Baslé
 * @since 2.2
 */
public class IoPoolShutdownHook implements ShutdownHook {

    private final EventLoopGroup ioPool;
    private volatile boolean shutdown;

    public IoPoolShutdownHook(EventLoopGroup ioPool) {
        this.ioPool = ioPool;
        this.shutdown = false;
    }

    public Observable<Boolean> shutdown() {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(final Subscriber<? super Boolean> subscriber) {
                ioPool.shutdownGracefully().addListener(new GenericFutureListener() {
                    @Override
                    public void operationComplete(final Future future) throws Exception {
                        if (!subscriber.isUnsubscribed()) {
                            try {
                                if (future.isSuccess()) {
                                    subscriber.onNext(true);
                                    shutdown = true;
                                    subscriber.onCompleted();
                                } else {
                                    subscriber.onError(future.cause());
                                }
                            } catch (Exception ex) {
                                subscriber.onError(ex);
                            }
                        }
                    }
                });
            }
        }).onErrorResumeNext(Observable.just(true));
    }

    @Override
    public boolean isShutdown() {
        return shutdown;
    }
}
