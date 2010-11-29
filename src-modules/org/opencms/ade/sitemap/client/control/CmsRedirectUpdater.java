/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/control/Attic/CmsRedirectUpdater.java,v $
 * Date   : $Date: 2010/11/29 10:33:35 $
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

package org.opencms.ade.sitemap.client.control;

import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for updating redirects in the currently edited sitemap.<p>
 * 
 * TODO: handle delete and restore
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsRedirectUpdater {

    private List<CmsClientSitemapEntry> m_deletedRedirects = new ArrayList<CmsClientSitemapEntry>();

    /** Registered items with internal redirects. */
    private List<CmsClientSitemapEntry> m_internalRedirects = new ArrayList<CmsClientSitemapEntry>();

    /**
     * Default constructor.<p>
     */
    public CmsRedirectUpdater() {

        // do nothing 

    }

    /**
     * Updates registered redirects when a sitemap entry is moved.<p>
     * 
     * @param src the source path of the move operation 
     * @param target the target path 
     */
    public void handleMove(String src, String target) {

        for (CmsClientSitemapEntry entry : m_internalRedirects) {
            String redirect = entry.getRedirect();
            if (CmsStringUtil.isPrefixPath(src, redirect)) {
                String newRedirect = redirect.replace(src, target);
                entry.setRedirect(newRedirect, true);
            }
        }
    }

    /**
     * Registers a new sitemap entry with a redirect.<p>
     * 
     * @param entry the entry to register 
     */
    public void handleSave(CmsClientSitemapEntry entry) {

        m_internalRedirects.remove(entry);
        if (entry.hasInternalRedirect()) {
            m_internalRedirects.add(entry);
        }
    }

}
