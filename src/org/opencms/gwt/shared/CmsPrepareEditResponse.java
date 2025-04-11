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

import java.io.Serializable;

/**
 * Response bean class for the 'prepareEdit' RPC method.<p>
 */
public class CmsPrepareEditResponse implements Serializable {

    /** Generated serial version id. */
    private static final long serialVersionUID = 6147076596551311120L;

    /** The resource root path. */
    private String m_rootPath;

    /** The resource site path. */
    private String m_sitePath;

    /** The resource structure id. */
    private CmsUUID m_structureId;

    /**
     * Default constructor.<p>
     */
    public CmsPrepareEditResponse() {

        // do nothing

    }

    /**
     * Gets the resource root path.<p>
     *
     * @return the resource root path
     */
    public String getRootPath() {

        return m_rootPath;
    }

    /**
     * Gets the resource site path.<p>
     *
     * @return the resource site path
     */
    public String getSitePath() {

        return m_sitePath;
    }

    /**
     * Gets the resource structure id.<p>
     *
     * @return the resource structure id
     */
    public CmsUUID getStructureId() {

        return m_structureId;
    }

    /**
     * Sets the resource root path.<p>
     *
     * @param rootPath the resource root path
     */
    public void setRootPath(String rootPath) {

        m_rootPath = rootPath;
    }

    /**
     * Sets the resource site path.<p>
     *
     * @param sitePath the resource site path
     */
    public void setSitePath(String sitePath) {

        m_sitePath = sitePath;
    }

    /**
     * Sets the resource structure id.<p>
     *
     * @param structureId the resource structure id
     */
    public void setStructureId(CmsUUID structureId) {

        m_structureId = structureId;
    }
}
