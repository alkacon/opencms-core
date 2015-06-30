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

package org.opencms.ade.containerpage.shared;

import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Bean which holds the results of saving a group container.<p>
 */
public class CmsGroupContainerSaveResult implements IsSerializable {

    /** The group container element data. */
    private Map<String, CmsContainerElementData> m_elementData;

    /** The elements which were removed from the group container. */
    private List<CmsRemovedElementStatus> m_removedElements;

    /**
     * Creates a new instance.<p>
     *
     * @param elementData the group container elements
     * @param removedElements the removed group container elements
     */
    public CmsGroupContainerSaveResult(
        Map<String, CmsContainerElementData> elementData,
        List<CmsRemovedElementStatus> removedElements) {

        m_elementData = elementData;
        m_removedElements = removedElements;
    }

    /**
     * Default constructor for serialization.<p>
     */
    protected CmsGroupContainerSaveResult() {

        // hidden default constructor for serialization
    }

    /**
     * Gets the group container elements.<p>
     *
     * @return the group container elements
     */
    public Map<String, CmsContainerElementData> getElementData() {

        return m_elementData;
    }

    /**
     * Gets the list of removed elements.<p>
     *
     * @return the list of removed elements
     */
    public List<CmsRemovedElementStatus> getRemovedElements() {

        return m_removedElements;
    }

}
