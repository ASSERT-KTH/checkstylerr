package io.openems.edge.ess.api;

import java.util.ArrayList;
import java.util.List;

import io.openems.edge.common.channel.Channel;
import io.openems.edge.common.sum.GridMode;

/**
 * Helper class to find the effective Grid-Mode of multiple Ess.
 */
public class CalculateGridMode {

	private final List<GridMode> values = new ArrayList<>();

	/**
	 * Adds a Channel-Value.
	 * 
	 * @param channel
	 */
	public void addValue(Channel<GridMode> channel) {
		GridMode gridMode = channel.getNextValue().asEnum();
		this.values.add(gridMode);
	}

	/**
	 * Finds the effective Grid-Mode.
	 * 
	 * @return
	 */
	public GridMode calculate() {
		int onGrids = 0;
		int offGrids = 0;
		for (GridMode gridMode : this.values) {
			switch (gridMode) {
			case OFF_GRID:
				offGrids++;
				break;
			case ON_GRID:
				onGrids++;
				break;
			case UNDEFINED:
				break;
			}
		}

		GridMode result = GridMode.UNDEFINED;
		if (this.values.size() == onGrids) {
			result = GridMode.ON_GRID;
		}
		if (this.values.size() == offGrids) {
			result = GridMode.OFF_GRID;
		}
		return result;
	}
}
