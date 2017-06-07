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

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A bean which represents the information for the delete dialog.<p>
 *
 * @since 8.0.0
 */
public class CmsDeleteResourceBean implements IsSerializable {

    /** The list of broken links. */
    private List<CmsBrokenLinkBean> m_brokenLinks;

    /** The page info for displaying the CmsListItemWidget. */
    private CmsListInfoBean m_pageInfo;

    /** The site path of the resource that to get the broken links for. */
    private String m_sitePath;

    /**
     * Constructor.<p>
     * @param sitePath site path of the resource that to get the broken links for
     * @param pageInfo page info
     * @param brokenLinks list of broken links
     */
    public CmsDeleteResourceBean(String sitePath, CmsListInfoBean pageInfo, List<CmsBrokenLinkBean> brokenLinks) {

        m_sitePath = sitePath;
        m_pageInfo = pageInfo;
        m_brokenLinks = brokenLinks;
    }

    /**
     * Hidden default constructor.<p>
     */
    protected CmsDeleteResourceBean() {

        // noop
    }

    /**
     * Returns the broken links.<p>
     *
     * @return the broken links
     */
    public List<CmsBrokenLinkBean> getBrokenLinks() {

        return m_brokenLinks;
    }

    /**
     * Returns the pageInfo.<p>
     *
     * @return the pageInfo
     */
    public CmsListInfoBean getPageInfo() {

        return m_pageInfo;
    }

    /**
     * Returns the sitePath.<p>
     *
     * @return the sitePath
     */
    public String getSitePath() {

        return m_sitePath;
    }

    /**
     * Sets the broken links.<p>
     *
     * @param brokenLinks the broken links to set
     */
    public void setBrokenLinks(List<CmsBrokenLinkBean> brokenLinks) {

        m_brokenLinks = brokenLinks;
    }

    /**
     * Sets the pageInfo.<p>
     *
     * @param pageInfo the pageInfo to set
     */
    public void setPageInfo(CmsListInfoBean pageInfo) {

        m_pageInfo = pageInfo;
    }

    /**
     * Sets the sitePath.<p>
     *
     * @param sitePath the sitePath to set
     */
    public void setSitePath(String sitePath) {

        m_sitePath = sitePath;
    }
}
