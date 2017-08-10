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

package org.opencms.widgets.serialdate;

import java.util.Date;
import java.util.SortedSet;

/**
 * Interface for serial date beans.
 *
 * Serial date beans allow easy access to values stored by the {@link org.opencms.widgets.CmsSerialDateWidget}.
 */
public interface I_CmsSerialDateBean {

    /**
     * Returns all dates of the whole series as {@link Date} objects, sorted ascendingly.
     * @return  all dates of the whole series as {@link Date} objects, sorted ascendingly.
     */
    public SortedSet<Date> getDates();

    /**
     * Returns all dates of the whole series in milliseconds, sorted ascendingly.
     * @return  all dates of the whole series in milliseconds, sorted ascendingly.
     */
    public SortedSet<Long> getDatesAsLong();

    /**
     * Returns the duration of a single event in milliseconds, or <code>null</code> if no end date is specified.
     * @return the duration of a single event in milliseconds, or <code>null</code> if no end date is specified.
     */
    public Long getEventDuration();

    /**
     * Returns all exceptions from the series, sorted ascendingly.
     * @return all exceptions from the series, sorted ascendingly.
     */
    public SortedSet<Date> getExceptions();

    /**
     * Returns a flag, indicating if the series has more dates than allowed.
     * @return a flag, indicating if the series has more dates than allowed.
     */
    public boolean hasTooManyDates();
}
