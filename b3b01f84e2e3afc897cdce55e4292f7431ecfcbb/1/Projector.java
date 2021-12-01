package io.jes.reactors;

import java.util.Objects;
import javax.annotation.Nonnull;

import io.jes.JEventStore;
import io.jes.lock.LockManager;
import io.jes.offset.Offset;

public abstract class Projector extends Reactor {

    private final LockManager lockManager;

    public Projector(@Nonnull JEventStore store, @Nonnull Offset offset, @Nonnull LockManager lockManager) {
        super(store, offset);
        this.lockManager = Objects.requireNonNull(lockManager, "LockManager must not be null");
    }

    @Override
    void tailStore() {
        lockManager.doProtectedWrite(key, super::tailStore);
    }

    public void recreate() {
        lockManager.doProtectedWrite(key, () -> {
            offset.reset(key);
            onRecreate();
        });
    }

    /**
     * This method used to clean up all state (projection) made by this Projector.
     * Note: this method MUST NOT use any methods that are protected by {@link #lockManager} instance.
     */
    protected abstract void onRecreate();
}
