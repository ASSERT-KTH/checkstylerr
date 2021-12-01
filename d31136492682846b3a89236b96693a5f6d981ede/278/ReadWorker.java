package io.openems.edge.evcs.keba.kecontact;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import io.openems.common.worker.AbstractWorker;
import io.openems.edge.evcs.api.Evcs;

public class ReadWorker extends AbstractWorker {

	private final KebaKeContact parent;

	private LocalDateTime lastReport1 = LocalDateTime.MIN;
	private LocalDateTime lastReport2 = LocalDateTime.MIN;
	private LocalDateTime lastReport3 = LocalDateTime.MIN;
	private boolean validateReport1 = false;
	private boolean validateReport2 = false;
	private boolean validateReport3 = false;
	private static int MAX_TIME_TILL_REPLY = 15; // sec

	public ReadWorker(KebaKeContact parent) {
		this.parent = parent;
	}

	@Override
	public void activate(String name) {
		super.activate(name);
	}

	@Override
	public void deactivate() {
		super.deactivate();
	}

	@Override
	protected void forever() throws InterruptedException {

		// REPORT 1
		if (this.lastReport1.isBefore(LocalDateTime.now().minusSeconds(Report.REPORT1.getRequestSeconds()))) {
			this.lastReport1 = LocalDateTime.now();
			this.parent.send("report 1");
			this.validateReport1 = true;
			Thread.sleep(10);
		}

		// REPORT 2
		if (this.lastReport2.isBefore(LocalDateTime.now().minusSeconds(Report.REPORT2.getRequestSeconds()))) {
			this.lastReport2 = LocalDateTime.now();
			this.parent.send("report 2");
			this.validateReport2 = true;
			Thread.sleep(10);
		}

		// REPORT 3
		if (this.lastReport3.isBefore(LocalDateTime.now().minusSeconds(Report.REPORT3.getRequestSeconds()))) {
			this.lastReport3 = LocalDateTime.now();
			this.parent.send("report 3");
			this.validateReport3 = true;
			Thread.sleep(10);
		}

		// RESULTS
		// Sets the state of the component if the report doesn't answer in a few seconds
		if (this.validateReport1 && this.lastReport1.isBefore(LocalDateTime.now().minusSeconds(MAX_TIME_TILL_REPLY))) {
			currentCommunication(this.parent.getReadHandler().hasResultandReset(Report.REPORT1));
			this.validateReport1 = false;
		}
		if (this.validateReport2 && this.lastReport2.isBefore(LocalDateTime.now().minusSeconds(MAX_TIME_TILL_REPLY))) {
			currentCommunication(this.parent.getReadHandler().hasResultandReset(Report.REPORT2));
			this.validateReport2 = false;
		}

		if (this.validateReport3 && this.lastReport3.isBefore(LocalDateTime.now().minusSeconds(MAX_TIME_TILL_REPLY))) {
			currentCommunication(this.parent.getReadHandler().hasResultandReset(Report.REPORT3));
			this.validateReport3 = false;
		}
	}

	@Override
	protected int getCycleTime() {
		// get minimum required time till next report
		LocalDateTime now = LocalDateTime.now();
		if (this.lastReport1.isBefore(now.minusSeconds(Report.REPORT1.getRequestSeconds()))
				|| this.lastReport2.isBefore(now.minusSeconds(Report.REPORT2.getRequestSeconds()))
				|| this.lastReport3.isBefore(now.minusSeconds(Report.REPORT3.getRequestSeconds()))) {
			return 0;
		}
		long tillReport1 = ChronoUnit.MILLIS.between(now.minusSeconds(Report.REPORT1.getRequestSeconds()),
				this.lastReport1);
		long tillReport2 = ChronoUnit.MILLIS.between(now.minusSeconds(Report.REPORT2.getRequestSeconds()),
				this.lastReport2);
		long tillReport3 = ChronoUnit.MILLIS.between(now.minusSeconds(Report.REPORT3.getRequestSeconds()),
				this.lastReport3);
		long min = Math.min(Math.min(tillReport1, tillReport2), tillReport3);
		if (min < 0) {
			return 0;
		} else if (min > Integer.MAX_VALUE) {
			return Integer.MAX_VALUE;
		} else {
			return (int) min;
		}
	}

	@Override
	public void triggerNextRun() {

		// reset times for next report query
		this.lastReport1 = LocalDateTime.MIN;
		this.lastReport2 = LocalDateTime.MIN;
		this.lastReport3 = LocalDateTime.MIN;

		super.triggerNextRun();
	}

	/**
	 * Set the current fail state of the EVCS to true or false.
	 * 
	 * @param receivedAMessage return value from the ReadHandler
	 */
	private void currentCommunication(boolean receivedAMessage) {
		this.parent.channel(Evcs.ChannelId.CHARGINGSTATION_COMMUNICATION_FAILED).setNextValue(!receivedAMessage);
	}

}
