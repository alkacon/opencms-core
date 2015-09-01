/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.gwt.client.util;

import com.google.gwt.event.logical.shared.ValueChangeEvent;

/**
 * Value change event with additional data.<p>
 *
 * @param <T> the value type
 */
public class CmsExtendedValueChangeEvent<T> extends ValueChangeEvent<T> {

    /** Flag to indicate whether validation should be triggered for this event. */
    private boolean m_inhibitValidation;

    /**
     * Creates a new value change event instance.<p>
     *
     * @param value the new value
     */
    public CmsExtendedValueChangeEvent(T value) {

        super(value);

    }

    /**
     * Return true if validation shouldn't be triggered by this event.<p>
     *
     * @return true if validation shouldn't be triggered by this event
     */
    public boolean isInhibitValidation() {

        return m_inhibitValidation;
    }

    /**
     * Sets the inhibitValidation flag.<p>
     *
     * @param inhibitValidation the new flag value
     */
    public void setInhibitValidation(boolean inhibitValidation) {

        m_inhibitValidation = inhibitValidation;
    }
}
