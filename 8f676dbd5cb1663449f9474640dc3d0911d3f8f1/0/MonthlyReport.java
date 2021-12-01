package cc.blynk.server.core.model.widgets.ui.reporting.type;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;

/**
 * The Blynk Project.
 * Created by Dmitriy Dumanskiy.
 * Created on 22.05.18.
 */
public class MonthlyReport extends DailyReport {

    public final DayOfMonth dayOfMonth;

    @JsonCreator
    public MonthlyReport(@JsonProperty("atTime") long atTime,
                         @JsonProperty("durationType") ReportDurationType durationType,
                         @JsonProperty("startTs") long startTs,
                         @JsonProperty("endTs") long endTs,
                         @JsonProperty("dayOfMonth") DayOfMonth dayOfMonth) {
        super(atTime, durationType, startTs, endTs);
        this.dayOfMonth = dayOfMonth;
    }

    @Override
    public ZonedDateTime getNextTriggerTime(ZonedDateTime zonedNow, ZoneId zoneId) {
        ZonedDateTime zonedStartAt = buildZonedStartAt(zonedNow, zoneId);

        switch (dayOfMonth) {
            case LAST:
                zonedStartAt = zonedStartAt.with(TemporalAdjusters.lastDayOfMonth());
                return zonedStartAt.isAfter(zonedNow) ? zonedStartAt :
                        zonedStartAt.plusDays(1).with(TemporalAdjusters.lastDayOfMonth());
            case FIRST:
            default:
                zonedStartAt = zonedStartAt.with(TemporalAdjusters.firstDayOfMonth());
                return zonedStartAt.isAfter(zonedNow) ? zonedStartAt :
                        zonedStartAt.with(TemporalAdjusters.firstDayOfNextMonth());

        }
    }
}
