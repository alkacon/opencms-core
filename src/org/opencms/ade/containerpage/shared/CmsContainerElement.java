/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/ade/containerpage/shared/CmsContainerElement.java,v $
 * Date   : $Date: 2011/06/10 06:57:36 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2011 Alkacon Software (http://www.alkacon.com)
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

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Bean holding basic container element information.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsContainerElement implements IsSerializable {

    /** The element client id. */
    private String m_clientId;

    /** Flag indicating a new element. */
    private boolean m_new;

    /** The resource type for new elements. If this field is not empty, the element is regarded as new and not created yet. */
    private String m_resourceType;

    /** The full site path. */
    private String m_sitePath;

    /**
     * Returns the client id.<p>
     *
     * @return the client id
     */
    public String getClientId() {

        return m_clientId;
    }

    /**
     * Returns the resource type name for elements.<p>
     * 
     * @return the resource type name
     */
    public String getResourceType() {

        return m_resourceType;
    }

    /**
     * Returns the site path.<p>
     *
     * @return the site path
     */
    public String getSitePath() {

        return m_sitePath;
    }

    /**
     * Returns if the element is new and has not been created in the VFS yet.<p>
     * 
     * @return <code>true</code> if the element is not created in the VFS yet
     */
    public boolean isNew() {

        return m_new;
    }

    /**
     * Sets the client id.<p>
     *
     * @param clientId the client id to set
     */
    public void setClientId(String clientId) {

        m_clientId = clientId;
    }

    /**
     * Sets the 'new' flag.<p>
     * 
     * @param isNew <code>true</code> on a new element
     */
    public void setNew(boolean isNew) {

        m_new = isNew;
    }

    /**
     * Sets the element resource type.<p>
     * 
     * @param resourceType the element resource type
     */
    public void setResourceType(String resourceType) {

        m_resourceType = resourceType;
    }

    /**
     * Sets the site path.<p>
     *
     * @param sitePath the site path to set
     */
    public void setSitePath(String sitePath) {

        m_sitePath = sitePath;
    }

}
