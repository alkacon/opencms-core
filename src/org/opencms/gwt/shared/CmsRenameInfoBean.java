/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.gwt.shared;

import org.opencms.util.CmsUUID;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Bean class containing the information needed for the Rename dialog.<p>
 */
public class CmsRenameInfoBean implements IsSerializable {

    /** The list info bean. */
    private CmsListInfoBean m_listInfo;

    /** The resource site path. */
    private String m_sitePath;

    /** The resource structure id. */
    private CmsUUID m_structureId;

    /**
     * Creates a new instance.<p>
     *
     * @param sitePath the site path of the resource
     * @param structureId the structure id of the resource
     * @param listInfo the list info bean for the resource
     */
    public CmsRenameInfoBean(String sitePath, CmsUUID structureId, CmsListInfoBean listInfo) {

        m_listInfo = listInfo;
        m_sitePath = sitePath;
        m_structureId = structureId;
    }

    /**
     * Empty default constructor for serialization.<p>
     */
    protected CmsRenameInfoBean() {

        // empty
    }

    /**
     * Gets the list info bean for the resource.<p>
     *
     * @return the list info bean
     */
    public CmsListInfoBean getListInfo() {

        return m_listInfo;
    }

    /**
     * Gets the site path of the resource.<p>
     *
     * @return the site path
     */
    public String getSitePath() {

        return m_sitePath;
    }

    /**
     * Gets the structure id of the resource.<p>
     *
     * @return the structure id
     */
    public CmsUUID getStructureId() {

        return m_structureId;
    }

}
