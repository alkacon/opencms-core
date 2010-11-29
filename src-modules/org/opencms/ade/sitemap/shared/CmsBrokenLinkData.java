/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/shared/Attic/CmsBrokenLinkData.java,v $
 * Date   : $Date: 2010/11/29 08:25:32 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.sitemap.shared;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Stores broken link data.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsBrokenLinkData implements IsSerializable {

    private List<CmsSitemapBrokenLinkBean> m_brokenLinks;
    private List<CmsClientSitemapEntry> m_closedEntries;

    /**
     * Returns the broken links.<p>
     *
     * @return the broken links
     */
    public List<CmsSitemapBrokenLinkBean> getBrokenLinks() {

        return m_brokenLinks;
    }

    /**
     * Returns the closed entries.<p>
     *
     * @return the closed entries
     */
    public List<CmsClientSitemapEntry> getClosedEntries() {

        return m_closedEntries;
    }

    /**
     * Sets the broken links.<p>
     *
     * @param brokenLinks the broken links to set
     */
    public void setBrokenLinks(List<CmsSitemapBrokenLinkBean> brokenLinks) {

        m_brokenLinks = brokenLinks;
    }

    /**
     * Sets the closed entries.<p>
     *
     * @param closedEntries the closed entries to set
     */
    public void setClosedEntries(List<CmsClientSitemapEntry> closedEntries) {

        m_closedEntries = closedEntries;
    }
}
