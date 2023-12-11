/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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

package org.opencms.ui.components;

import org.opencms.ui.A_CmsUI;
import org.opencms.ui.Messages;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import com.vaadin.shared.ui.datefield.DateTimeResolution;
import com.vaadin.ui.DateTimeField;

/**
 * Convenience subclass of PopupDateField which comes preconfigured with a resolution and validation error message.<p>
 */
public class CmsDateField extends DateTimeField {

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance.<p<
     */
    public CmsDateField() {

        super();
        setResolution(DateTimeResolution.MINUTE);
        String parseError = Messages.get().getBundle(A_CmsUI.get().getLocale()).key(Messages.GUI_INVALID_DATE_FORMAT_0);
        setParseErrorMessage(parseError);
    }

    /**
     * Converts a {@link Date} object to a {@link LocalDateTime} object.<p>
     *
     * @param date the date
     *
     * @return the local date time
     */
    public static LocalDateTime dateToLocalDateTime(Date date) {

        if (date == null) {
            return null;
        }
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    /**
     * Converts a {@link LocalDateTime} object to a {@link Date} object.<p>
     *
     * @param local the local date time
     *
     * @return the date
     */
    public static Date localDateTimeToDate(LocalDateTime local) {

        if (local == null) {
            return null;
        }
        ZonedDateTime zdt = local.atZone(ZoneId.systemDefault());
        return Date.from(zdt.toInstant());
    }

    /**
     * Convenience method returning the field value converted to date.<p>
     *
     * @return the date
     */
    public Date getDate() {

        return localDateTimeToDate(getValue());
    }

    /**
     * Convenience method to set the LocalDateTime field value to the given date.<p>
     *
     * @param date the date to set
     */
    public void setDate(Date date) {

        setValue(dateToLocalDateTime(date));
    }
}
