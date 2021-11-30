package com.piksel.sequoia.clientsdk;

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
