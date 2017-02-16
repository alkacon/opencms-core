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

import org.opencms.ade.containerpage.shared.CmsCntPageData.ElementDeleteMode;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.util.CmsUUID;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A bean used to store information about a container page element which was just removed.<p>
 */
public class CmsRemovedElementStatus implements IsSerializable {

    /** The element delete mode. */
    private ElementDeleteMode m_elementDeleteMode;

    /** The list info bean to display. */
    private CmsListInfoBean m_elementInfo;

    /** True if this element is a possible deletion candidate. */
    private boolean m_isDeletionCandidate;

    /** The structure id of the removed element. */
    private CmsUUID m_structureId;

    /**
     * Creates a new instance.<p>
     *
     * @param structureId the structure id of the removed element
     * @param elementInfo the list info bean for the removed element
     * @param deletable true if this is a possible deletion candidate
     * @param elementDeleteMode the element delete mode
     */
    public CmsRemovedElementStatus(
        CmsUUID structureId,
        CmsListInfoBean elementInfo,
        boolean deletable,
        ElementDeleteMode elementDeleteMode) {

        m_isDeletionCandidate = deletable;
        m_elementInfo = elementInfo;
        m_structureId = structureId;
        m_elementDeleteMode = elementDeleteMode;
    }

    /**
     * Default constructor for serialization.<p>
     */
    protected CmsRemovedElementStatus() {

        // empty default constructor for serialization
    }

    /**
     * Gets the element delete mode.<p>
     *
     * @return the element delete mode
     */
    public ElementDeleteMode getElementDeleteMode() {

        return m_elementDeleteMode;
    }

    /**
     * Gets the list info bean for the removed element.<p>
     *
     * @return the list info bean for the removed element
     */
    public CmsListInfoBean getElementInfo() {

        return m_elementInfo;
    }

    /**
     * Gets the structure id of the removed element.<p>
     *
     * @return the structure id of the removed element
     */
    public CmsUUID getStructureId() {

        return m_structureId;
    }

    /**
     * Returns true if the removed element is a possible candidate for deletion.<p>
     *
     * @return true if the removed element is a deletion candidate
     */
    public boolean isDeletionCandidate() {

        return m_isDeletionCandidate;
    }

}
