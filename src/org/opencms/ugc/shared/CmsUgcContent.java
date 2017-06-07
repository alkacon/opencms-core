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

package org.opencms.ugc.shared;

import org.opencms.util.CmsUUID;

import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * The form content information bean.<p>
 */
public class CmsUgcContent implements IsSerializable {

    /** The edit session id. */
    private CmsUUID m_sessionId;

    /** The content values. */
    private Map<String, String> m_contentValues;

    /** The edited resource structure id. */
    private CmsUUID m_strucureId;

    /** The edited resource site path. */
    private String m_sitePath;

    /** The resource type name. */
    private String m_resourceType;

    /**
     * Returns the content values.<p>
     *
     * @return the content values
     */
    public Map<String, String> getContentValues() {

        return m_contentValues;
    }

    /**
     * Returns  the resource type name.<p>
     *
     * @return the resource type name
     */
    public String getResourceType() {

        return m_resourceType;
    }

    /**
     * Returns the editing session id.<p>
     *
     * @return the session id
     */
    public CmsUUID getSessionId() {

        return m_sessionId;
    }

    /**
     * Returns the edited resource site path.<p>
     *
     * @return the site path
     */
    public String getSitePath() {

        return m_sitePath;
    }

    /**
     * Returns the edited resource structure id.<p>
     *
     * @return the structure id
     */
    public CmsUUID getStrucureId() {

        return m_strucureId;
    }

    /**
     * Sets the content values.<p>
     *
     * @param contentValues the content values
     */
    public void setContentValues(Map<String, String> contentValues) {

        m_contentValues = contentValues;
    }

    /**
     * Sets the resource type name
     *
     * @param resourceType the resource type
     */
    public void setResourceType(String resourceType) {

        m_resourceType = resourceType;
    }

    /**
     * Sets the editing session id.<p>
     *
     * @param sessionId the session id
     */
    public void setSessionId(CmsUUID sessionId) {

        m_sessionId = sessionId;
    }

    /**
     * Sets the site path.<p>
     *
     * @param sitePath the site path
     */
    public void setSitePath(String sitePath) {

        m_sitePath = sitePath;
    }

    /**
     * Sets the structure id.<p>
     *
     * @param strucureId the structure id
     */
    public void setStrucureId(CmsUUID strucureId) {

        m_strucureId = strucureId;
    }

}
