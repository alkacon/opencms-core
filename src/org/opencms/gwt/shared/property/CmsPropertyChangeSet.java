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

package org.opencms.gwt.shared.property;

import org.opencms.util.CmsUUID;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A bean representing a set of property changes.<p>
 *
 * @since 8.0.0
 */
public class CmsPropertyChangeSet implements IsSerializable {

    /** The list of changes. */
    private List<CmsPropertyModification> m_propertyChanges;

    /** The structure id of the target to which the property changes should be applied.<p> */
    private CmsUUID m_target;

    /**
     * Creates a new property change set.<p>
     *
     * @param target the structure of the target resource
     * @param propertyChanges the property changes themselves
     */
    public CmsPropertyChangeSet(CmsUUID target, List<CmsPropertyModification> propertyChanges) {

        m_propertyChanges = propertyChanges;
        m_target = target;
    }

    /**
     * Hidden default constructor for serialization.<p>
     */
    protected CmsPropertyChangeSet() {

        // only used for serialization
    }

    /**
     * Gets the list of property change beans.<p>
     *
     * @return the list of property change beans
     */
    public List<CmsPropertyModification> getChanges() {

        return m_propertyChanges;
    }

    /**
     * Gets the structure id of the target resource.<p>
     *
     * @return the structure id of the target resource
     */
    public CmsUUID getTargetStructureId() {

        return m_target;
    }

}
