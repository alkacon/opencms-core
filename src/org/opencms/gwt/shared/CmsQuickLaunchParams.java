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

package org.opencms.gwt.shared;

import org.opencms.util.CmsUUID;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Parameters used by the quick launch provider.<p>
 */
public class CmsQuickLaunchParams implements IsSerializable {

    /** Context (sitmap or page editor). */
    private String m_context;

    /** Page id. */
    private CmsUUID m_pageId;

    /** Detail content id. */
    private CmsUUID m_detailId;

    /** Return code. */
    private String m_returnCode;

    /** Path. */
    private String m_path;

    /**
     * Creates a new instance.<p>
     *
     * @param context the quick launch context
     * @param pageId the page id
     * @param detailId the detail content id
     * @param returnCode the return code
     * @param path  the path
     */
    public CmsQuickLaunchParams(String context, CmsUUID pageId, CmsUUID detailId, String returnCode, String path) {
        m_context = context;
        m_pageId = pageId;
        m_detailId = detailId;
        m_returnCode = returnCode;
        m_path = path;
    }

    /**
     * Default constructor for serialization.<p>
     */
    protected CmsQuickLaunchParams() {
        // do nothing
    }

    /**
     * Returns the context.<p>
     *
     * @return the context
     */
    public String getContext() {

        return m_context;
    }

    /**
     * Returns the detailId.<p>
     *
     * @return the detailId
     */
    public CmsUUID getDetailId() {

        return m_detailId;
    }

    /**
     * Returns the pageId.<p>
     *
     * @return the pageId
     */
    public CmsUUID getPageId() {

        return m_pageId;
    }

    /**
     * Gets the path.<p>
     *
     * @return the path
     */
    public String getPath() {

        return m_path;
    }

    /**
     * Returns the returnCode.<p>
     *
     * @return the returnCode
     */
    public String getReturnCode() {

        return m_returnCode;
    }

    /**
     * Returns true if the quick launcher is called from the page editor.<p>
     *
     * @return true if the quick launcher was called from the page editor
     */
    public boolean isPageContext() {

        return CmsGwtConstants.QuickLaunch.CONTEXT_PAGE.equals(m_context);
    }

    /**
     * Returns true if the quick launcher is called from the sitemap editor.<p>
     *
     * @return true if the quick launcher was called from the sitemap editor
     */
    public boolean isSitemapContext() {

        return CmsGwtConstants.QuickLaunch.CONTEXT_SITEMAP.equals(m_context);
    }

}
