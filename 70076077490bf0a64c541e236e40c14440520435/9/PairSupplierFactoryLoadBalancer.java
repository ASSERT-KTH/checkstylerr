/*
 * Copyright (c) 2010-2012 Grid Dynamics Consulting Services, Inc, All Rights Reserved
 * http://www.griddynamics.com
 *
 * This library is free software; you can redistribute it and/or modify it under the terms of
 * the Apache License; either
 * version 2.0 of the License, or any later version.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.griddynamics.jagger.invoker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @ingroup Main_Distributors_group */

public abstract class PairSupplierFactoryLoadBalancer<Q, E> extends QueryPoolLoadBalancer<Q, E> {
    
    private final static Logger log = LoggerFactory.getLogger(PairSupplierFactoryLoadBalancer.class);
    private final PairSupplierFactory<Q, E> pairSupplierFactory;
    private volatile PairSupplier<Q, E> pairSupplier;
    
    protected PairSupplierFactoryLoadBalancer(PairSupplierFactory<Q, E> pairSupplierFactory) {
        this.pairSupplierFactory = pairSupplierFactory;
    }
    
    protected PairSupplier<Q, E> getPairSupplier() {
        return pairSupplier;
    }
    
    @Override
    public void init() {
        synchronized (lock) {
            if (initialized) {
                log.debug("already initialized. returning...");
                return;
            }
            super.init();
            
            pairSupplier = pairSupplierFactory.create(queryProvider, endpointProvider);
            log.info("{} pairs in total to balance", pairSupplier.size());
        }
    }
}
