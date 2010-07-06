/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/input/datebox/Attic/CmsDateChangeEvent.java,v $
 * Date   : $Date: 2010/07/06 12:08:04 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.gwt.client.ui.input.datebox;

import java.util.Date;

import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.user.datepicker.client.CalendarUtil;

/**
 * Implements the ValueChangeEvent for a date object.<p>
 *
 * @version 0.1
 * 
 * @author Ruediger Kurz
 */
class CmsDateChangeEvent extends ValueChangeEvent<Date> {

    /**
     * Fires value change event if the old value is not equal to the new value.
     * Use this call rather than making the decision to short circuit yourself for
     * safe handling of null.
     * 
     * @param <S> The event source
     * @param source the source of the handlers
     * @param oldValue the oldValue, may be null
     * @param newValue the newValue, may be null
     */
    public static <S extends HasValueChangeHandlers<Date> & HasHandlers> void fireIfNotEqualDates(
        S source,
        Date oldValue,
        Date newValue) {

        if (ValueChangeEvent.shouldFire(source, oldValue, newValue)) {
            source.fireEvent(new CmsDateChangeEvent(newValue));
        }
    }

    /**
     * Creates a new date value change event.
     * 
     * @param value the value
     */
    protected CmsDateChangeEvent(Date value) {

        // The date must be copied in case one handler causes it to change.
        super(CalendarUtil.copyDate(value));
    }

    /**
     * @see com.google.gwt.event.logical.shared.ValueChangeEvent#getValue()
     */
    @Override
    public Date getValue() {

        return CalendarUtil.copyDate(super.getValue());
    }
}
