package org.unitedinternet.cosmo.ext;

import java.util.HashSet;
import java.util.Set;

import org.unitedinternet.cosmo.model.ICalendarItem;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.hibernate.EntityConverter;

import net.fortuna.ical4j.model.Calendar;

/**
 * Helper class that allows translating <code>Calendar</code> objects to <code>NoteItem</code>-s.
 * 
 * @author daniel grigore
 *
 */
public class ContentConverter {

    private final EntityConverter entityConverter;

    public ContentConverter(EntityConverter entityConverter) {
        super();
        this.entityConverter = entityConverter;
    }

    public Set<NoteItem> asItems(Calendar calendar) {
        Set<NoteItem> items = new HashSet<>();
        if (calendar != null) {
            Set<ICalendarItem> calendarItems = this.entityConverter.convertCalendar(calendar);

            for (ICalendarItem item : calendarItems) {
                /**
                 * Only VEVENT are supported currently. VTODO or VJOURNAL are not yet supported.
                 */
                if (item instanceof NoteItem) {
                    items.add((NoteItem) item);
                    item.setName(item.getIcalUid() + ".ics");
                }
            }
        }
        return items;
    }
}
