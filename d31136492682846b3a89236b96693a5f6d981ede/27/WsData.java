package io.openems.backend.b2bwebsocket;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.openems.backend.common.metadata.User;
import io.openems.common.exceptions.OpenemsError;
import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;

public class WsData extends io.openems.common.websocket.WsData {

	private final B2bWebsocket parent;
	private final SubscribedEdgesChannelsWorker worker;
	private CompletableFuture<User> user = new CompletableFuture<User>();

	public WsData(B2bWebsocket parent) {
		this.parent = parent;
		this.worker = new SubscribedEdgesChannelsWorker(parent, this);
	}

	@Override
	public void dispose() {
		this.worker.dispose();
	}

	public void setUser(User user) {
		this.user.complete(user);
	}

	public CompletableFuture<User> getUser() {
		return this.user;
	}

	public User getUserWithTimeout(long timeout, TimeUnit unit) throws OpenemsNamedException {
		try {
			return this.user.get(timeout, unit);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			throw OpenemsError.COMMON_USER_NOT_AUTHENTICATED.exception("UNKNOWN");
		}
	}

	public Optional<User> getUserOpt() {
		return Optional.ofNullable(this.user.getNow(null));
	}

	/**
	 * Gets the SubscribedChannelsWorker to take care of subscribe to CurrentData.
	 * 
	 * @return the SubscribedChannelsWorker
	 */
	public SubscribedEdgesChannelsWorker getSubscribedChannelsWorker() {
		return this.worker;
	}

	@Override
	public String toString() {
		if (this.user == null) {
			return "B2bWebsocket.WsData [user=UNKNOWN]";
		} else {
			return "B2bWebsocket.WsData [user=" + user + "]";
		}
	}

	@Override
	protected ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay,
			TimeUnit unit) {
		return this.parent.executor.scheduleWithFixedDelay(command, initialDelay, delay, unit);
	}
}
