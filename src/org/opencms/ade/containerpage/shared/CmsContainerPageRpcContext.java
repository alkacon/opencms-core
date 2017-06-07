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

package org.opencms.ade.containerpage.shared;

import org.opencms.util.CmsUUID;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Bean containing the 'context' of the edited container page for use in RPC calls.<p>
 */
public class CmsContainerPageRpcContext implements IsSerializable {

    /**
     * Structure id of the page.<p>
     */
    private CmsUUID m_pageStructureId;

    /**
     * Template context key.<p>
     */
    private String m_templateContext;

    /**
     * Creates a new instance.<p>
     *
     * @param pageStructureId the page structure id
     * @param templateContext the template context key
     */
    public CmsContainerPageRpcContext(CmsUUID pageStructureId, String templateContext) {

        m_pageStructureId = pageStructureId;
        m_templateContext = templateContext;

    }

    /**
     * Default constructor for serialization.<p>
     */
    protected CmsContainerPageRpcContext() {

        // empty

    }

    /**
     * Returns the pageStructureId.<p>
     *
     * @return the pageStructureId
     */
    public CmsUUID getPageStructureId() {

        return m_pageStructureId;
    }

    /**
     * Returns the templateContext.<p>
     *
     * @return the templateContext
     */
    public String getTemplateContext() {

        return m_templateContext;
    }

}
