package io.openems.common.worker;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.openems.common.utils.Mutex;

/**
 * Defines a generic Worker Thread.
 * 
 * <p>
 * The business logic of the Worker is inside the {@link #forever()} method. It
 * is executed by default every {@link #getCycleTime()} seconds. Additionally
 * execution can be triggered by calling the {@link #triggerNextRun()} method.
 * 
 * <p>
 * If Cycle-Time is negative (e.g. by using
 * {@link #ALWAYS_WAIT_FOR_TRIGGER_NEXT_RUN}) the forever() method is called
 * only after triggering it.
 * 
 * <p>
 * If Cycle-Time is zero (e.g. by using {@link #DO_NOT_WAIT}), the forever()
 * method is always called immediately without any delay.
 */
public abstract class AbstractWorker {

	public final static int ALWAYS_WAIT_FOR_TRIGGER_NEXT_RUN = -1;
	public final static int DO_NOT_WAIT = 0;

	private final Logger log = LoggerFactory.getLogger(AbstractWorker.class);

	private final AtomicBoolean isStopped = new AtomicBoolean(false);
	private final Mutex cycleMutex = new Mutex(false);

	/**
	 * Initializes the worker and starts the worker thread
	 * 
	 * @param name
	 */
	public void activate(String name) {
		if (name != null) {
			this.worker.setName(name);
			this.worker.start();
		}
	}

	/**
	 * Stops the worker thread
	 */
	public void deactivate() {
		this.isStopped.set(true);
		this.worker.interrupt();
	}

	/**
	 * This method is called in a loop forever until the Thread gets interrupted.
	 */
	protected abstract void forever() throws Throwable;

	/**
	 * Gets the cycleTime of this worker in [ms].
	 * <ul>
	 * <li>&gt; 0 sets the minimum execution time of one Cycle
	 * <li>= 0 never wait between two consecutive executions of forever()
	 * <li>&lt; 0 causes the Cycle to sleep forever until 'triggerNextRun()' is
	 * called
	 * </ul>
	 * 
	 * @return
	 */
	protected abstract int getCycleTime();

	/**
	 * Allows the next execution of the forever() method.
	 */
	public void triggerNextRun() {
		this.cycleMutex.release();
	}

	private final Thread worker = new Thread() {
		public void run() {
			long onWorkerExceptionSleep = 1; // seconds
			long cycleStart = System.currentTimeMillis();
			while (!isStopped.get()) {
				try {
					/*
					 * Wait for next cycle
					 */
					int cycleTime = AbstractWorker.this.getCycleTime();
					if (cycleTime == DO_NOT_WAIT) {
						// no wait
					} else if (cycleTime > 0) {
						// wait remaining cycleTime
						long sleep = cycleTime - (System.currentTimeMillis() - cycleStart);
						if (sleep > 0) {
							AbstractWorker.this.cycleMutex.awaitOrTimeout(sleep, TimeUnit.MILLISECONDS);
						}
					} else { // < 0 (ALWAYS_WAIT_FOR_TRIGGER_NEXT_RUN)
						// wait till next run is triggered
						AbstractWorker.this.cycleMutex.await();
					}

					// store start time
					cycleStart = System.currentTimeMillis();

					/*
					 * Call forever() forever.
					 */
					AbstractWorker.this.forever();

					// Everything went ok -> reset onWorkerExceptionSleep
					onWorkerExceptionSleep = 1;

				} catch (Throwable e) {
					if (e instanceof InterruptedException && AbstractWorker.this.isStopped.get()) {
						// nothing
					} else {
						/*
						 * Handle Worker-Exceptions
						 */
						log.error("Worker error. " + e.getClass().getSimpleName() + ": " + e.getMessage() //
								+ (e.getCause() != null ? " - Caused by: " + e.getCause().getMessage() : ""));
						e.printStackTrace();
					}
					onWorkerExceptionSleep = onWorkerExceptionSleep(onWorkerExceptionSleep);
				}
			}
		};
	};

	/**
	 * Little helper method: Sleep and don't let yourself interrupt by a
	 * ForceRun-Flag. It is not making sense anyway, because something is wrong with
	 * the setup if we landed here.
	 *
	 * @param duration in seconds
	 */
	private long onWorkerExceptionSleep(long duration) {
		if (duration < 60) {
			duration += 1;
		}
		long targetTime = System.currentTimeMillis() + (duration * 1000);
		do {
			try {
				long thisDuration = (targetTime - System.currentTimeMillis()) / 1000;
				if (thisDuration > 0) {
					Thread.sleep(thisDuration);
				}
			} catch (InterruptedException e1) {
				log.warn("WorkerExceptionSleep caused " + e1.getMessage());
			}
		} while (targetTime > System.currentTimeMillis());
		return duration;
	}
}
