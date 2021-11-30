package com.piksel.sequoia.clientsdk;

/*-
 * #%L
 * Sequoia Java Client SDK
 * %%
 * Copyright (C) 2018 Piksel
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.concurrent.TimeUnit;

import com.piksel.sequoia.annotations.PublicEvolving;
import com.piksel.sequoia.clientsdk.exception.NotImplementedException;

/**
 * Allows user code to receive notification that a component has been initialised or shutdown.
 */
@PublicEvolving
public interface Lifecycle {

    /**
     * Waits for the component to become initialised for no more than the given time.
     * 
     * @param time
     *            the maximum time to wait
     * @param timeunit
     *            the time unit of the timeout argument
     * @throws SequoiaClientInitialisationException
     *             if the client has not been initialised yet or encountered an error during initialisation
     */
    void awaitInitialised(int time, TimeUnit timeunit);
    
    /**
     * Waits for the component to become shutdown for no more than the given
     * time.
     * @param time the maximum time to wait
     * @param timeunit the time unit of the timeout argument
     * @throws SequoiaClientShutdownException
     *             if the client has not been shutdown yet
     * @throws IllegalStateException
     *             if the client encountered an error during shutdown
     */
    void shutdown(int time, TimeUnit timeunit);

    /**
     * Resets instance values
     *
     * @throws NotImplementedException
     *             if the method has not been overridden in the instance.
     */
    default void reset() {
        throw new NotImplementedException();
    }
}
