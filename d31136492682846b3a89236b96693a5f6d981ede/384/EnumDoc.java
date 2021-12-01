package io.openems.edge.common.channel;

import java.util.Arrays;

import io.openems.common.channel.ChannelCategory;
import io.openems.common.exceptions.OpenemsError;
import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.common.types.OpenemsType;
import io.openems.common.types.OptionsEnum;
import io.openems.edge.common.channel.internal.AbstractDoc;
import io.openems.edge.common.channel.value.Value;
import io.openems.edge.common.component.OpenemsComponent;

public class EnumDoc extends AbstractDoc<Integer> {

	private final OptionsEnum[] options;

	public EnumDoc(OptionsEnum[] options) {
		super(OpenemsType.INTEGER);
		this.options = options;
	}

	@Override
	public ChannelCategory getChannelCategory() {
		return ChannelCategory.ENUM;
	}

	@Override
	protected EnumDoc self() {
		return this;
	}

	public OptionsEnum[] getOptions() {
		return options;
	}

	/**
	 * Initial-Value. Default: none
	 * 
	 * @param initialValue
	 * @return myself
	 */
	public EnumDoc initialValue(OptionsEnum initialValue) {
		this.initialValue(initialValue.getValue());
		return this.self();
	}

	/**
	 * Creates an instance of {@link Channel} for the given Channel-ID using its
	 * Channel-{@link Doc}.
	 * 
	 * @param channelId the Channel-ID
	 * @return the Channel
	 */
	@SuppressWarnings("unchecked")
	@Override
	public EnumReadChannel createChannelInstance(OpenemsComponent component,
			io.openems.edge.common.channel.ChannelId channelId) {
		switch (this.getAccessMode()) {
		case READ_ONLY:
			return new EnumReadChannel(component, channelId, this, this.getUndefinedOption());
		case READ_WRITE:
		case WRITE_ONLY:
			return new EnumWriteChannel(component, channelId, this, this.getUndefinedOption());
		}
		throw new IllegalArgumentException(
				"Unable to initialize Channel-ID [" + channelId.id() + "] from OptionsEnumDoc!");
	}

	/**
	 * Gets the Undefined-Option, i.e. the default Option if the value has not been
	 * set.
	 * 
	 * @return the Undefined-Option
	 */
	public OptionsEnum getUndefinedOption() {
		if (this.options.length == 0) {
			return null;
		}
		return this.options[0].getUndefined();
	}

	/**
	 * Gets the Option from a String.
	 * 
	 * @param name the name of the option. Comparison is case insensitive
	 * @return the {@link OptionsEnum}
	 * @throws OpenemsNamedException if there is no option with that name
	 */
	public OptionsEnum getOptionFromString(String name) throws OpenemsNamedException {
		for (OptionsEnum e : this.options) {
			if (e.getName().equalsIgnoreCase(name)) {
				return e;
			}
		}
		throw OpenemsError.EDGE_CHANNEL_NO_OPTION.exception(name, Arrays.toString(this.options));
	}

	/**
	 * Gets the Option value from a String.
	 * 
	 * @param name the name of the option. Comparison is case insensitive
	 * @return the integer value of the {@link OptionsEnum}
	 * @throws OpenemsNamedException if there is no option with that name
	 */
	public int getOptionValueFromString(String name) throws OpenemsNamedException {
		return this.getOptionFromString(name).getValue();
	}

	/**
	 * Gets the {@link OptionsEnum} from the integer value.
	 * 
	 * @param value the integer value of the option
	 * @return the {@link OptionsEnum}
	 */
	public OptionsEnum getOption(Integer value) {
		if (this.options.length == 0) {
			return null;
		}
		OptionsEnum undefined = this.options[0].getUndefined();
		if (value == null) {
			return undefined;
		}
		for (OptionsEnum e : this.options) {
			if (e.getValue() == value) {
				return e;
			}
		}
		return undefined;
	}

	/**
	 * Gets the name of the Option or 'UNDEFINED' if there is no option with that
	 * value
	 * 
	 * @param value the integer value of the Option
	 * @return the name of the Option as a String
	 */
	public String getOptionString(Integer value) {
		OptionsEnum option = this.getOption(value);
		if (option == null) {
			return Value.UNDEFINED_VALUE_STRING;
		}
		return option.getName();
	}
}
