/**
 * Template-method class for callback hook execution
 */
public abstract class Task {

    /**
     * Execute with callback
     */
    public final void executeWith(Callback callback) {
        execute();

        if (callback != null) {
          callback.call();
        }
    }

    public abstract void execute();
}
