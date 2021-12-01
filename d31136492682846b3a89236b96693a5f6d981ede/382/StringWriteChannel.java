package io.openems.edge.common.channel;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.common.function.ThrowingConsumer;
import io.openems.edge.common.component.OpenemsComponent;

public class StringWriteChannel extends StringReadChannel implements WriteChannel<String> {

	public static class MirrorToDebugChannel implements Consumer<Channel<String>> {

		private final Logger log = LoggerFactory.getLogger(MirrorToDebugChannel.class);

		private final ChannelId targetChannelId;

		public MirrorToDebugChannel(ChannelId targetChannelId) {
			this.targetChannelId = targetChannelId;
		}

		@Override
		public void accept(Channel<String> channel) {
			if (!(channel instanceof StringWriteChannel)) {
				this.log.error("Channel [" + channel.address()
						+ "] is not an StringWriteChannel! Unable to register \"onSetNextWrite\"-Listener!");
				return;
			}
			
			// on each setNextWrite to the channel -> store the value in the DEBUG-channel
			((StringWriteChannel) channel).onSetNextWrite(value -> {
				channel.getComponent().channel(this.targetChannelId).setNextValue(value);
			});
		}
	}

	public StringWriteChannel(OpenemsComponent component, ChannelId channelId, StringDoc channelDoc) {
		super(component, channelId, channelDoc);
	}

	private Optional<String> nextWriteValueOpt = Optional.empty();

	/**
	 * Internal method. Do not call directly.
	 * 
	 * @param value
	 */
	@Deprecated
	@Override
	public void _setNextWriteValue(String value) {
		this.nextWriteValueOpt = Optional.ofNullable(value);
	}

	@Override
	public Optional<String> getNextWriteValue() {
		return this.nextWriteValueOpt;
	}

	/*
	 * onSetNextWrite
	 */
	@Override
	public List<ThrowingConsumer<String, OpenemsNamedException>> getOnSetNextWrites() {
		return super.getOnSetNextWrites();
	}

	@Override
	public void onSetNextWrite(ThrowingConsumer<String, OpenemsNamedException> callback) {
		this.getOnSetNextWrites().add(callback);
	}

}
