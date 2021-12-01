/*
 * Copyright 2007 Open Source Applications Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.unitedinternet.cosmo.hibernate.validator;

import java.io.IOException;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.ValidationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.unitedinternet.cosmo.calendar.util.CalendarUtils;
import org.unitedinternet.cosmo.icalendar.ICalendarConstants;

/**
 * Check if a Calendar object contains a valid VAVAILABILITY
 */
public class AvailabilityValidator implements ConstraintValidator<Availability, Calendar>  {

    private static final Log LOG = LogFactory.getLog(AvailabilityValidator.class);
    
    public boolean isValid(Calendar value, ConstraintValidatorContext context) {
        if(value==null)
            return true;
        
        Calendar calendar = null;
        try {
            calendar = (Calendar) value;
            
            // validate entire icalendar object
            calendar.validate(true);
            
            // additional check to prevent bad .ics
            CalendarUtils.parseCalendar(calendar.toString());
            
            // make sure we have a VAVAILABILITY
            ComponentList comps = calendar.getComponents();
            if(comps==null) {
                LOG.warn("error validating availability: " + calendar.toString());
                return false;
            }
            
            comps = comps.getComponents(ICalendarConstants.COMPONENT_VAVAILABLITY);
            if(comps==null || comps.size()==0) {
                LOG.warn("error validating availability: " + calendar.toString());
                return false;
            }
            
            return true;
            
        } catch(ValidationException ve) {
            LOG.warn("availability validation error", ve );
            LOG.warn("error validating availability: " + calendar.toString() );
        } catch(ParserException e) {
            LOG.warn("parse error", e);
            LOG.warn("error parsing availability: " + calendar.toString() );
        } catch (IOException | RuntimeException e) {
            LOG.warn(e);
        }
        return false;
    }

    public void initialize(Availability parameters) {
        // nothing to do
    }

}

