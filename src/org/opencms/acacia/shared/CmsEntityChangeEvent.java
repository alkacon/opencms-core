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

package org.opencms.acacia.shared;

import com.google.gwt.event.logical.shared.ValueChangeEvent;

/** 
 * Value change event for entities with an additional field that indicates the type of change. 
 */
public class CmsEntityChangeEvent extends ValueChangeEvent<CmsEntity> {

    /** 
     * Enum indicating the type of changes. 
     */
    public enum ChangeType {
        add, remove, change;
    }

    private ChangeType m_changeType;

    /** 
     * Creates a new event instance.
     * 
     * @param value the new entity value
     * @param type the change type 
     */
    protected CmsEntityChangeEvent(CmsEntity value, ChangeType type) {

        // TODO Auto-generated constructor stub
        super(value);
        m_changeType = type;

    }

    /** 
     * Gets the change type. 
     * 
     * @return the change type 
     */
    public ChangeType getChangeType() {

        return m_changeType;
    }

}
