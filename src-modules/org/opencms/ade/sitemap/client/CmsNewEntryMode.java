/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/Attic/CmsNewEntryMode.java,v $
 * Date   : $Date: 2010/05/26 13:55:48 $
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

package org.opencms.ade.sitemap.client;

import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;

import java.util.Map;

/**
 * The mode handler for the 'new entry' mode of the sitemap entry editor.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 * 
 */
public class CmsNewEntryMode implements I_CmsSitemapEntryEditorModeHandler {

    /** Default name for new entries. */
    private static final String NEW_NAME = "new";

    /** The sitemap controller for this mode handler. */
    private CmsSitemapController m_controller;

    /** The sitemap entry for this mode handler. */
    private CmsClientSitemapEntry m_entry;

    /** 
     * Creates a new instance of this mode handler class.<p>
     * 
     * @param controller the sitemap controller for this mode handler
     * 
     * @param entry the sitemap entry for this mode handler 
     */
    public CmsNewEntryMode(CmsSitemapController controller, CmsClientSitemapEntry entry) {

        m_controller = controller;
        m_entry = entry;
    }

    /**
     * @see org.opencms.ade.sitemap.client.I_CmsSitemapEntryEditorModeHandler#createPath(java.lang.String)
     */
    public String createPath(String urlName) {

        return m_entry.getSitePath() + urlName + "/";
    }

    /**
     * @see org.opencms.ade.sitemap.client.I_CmsSitemapEntryEditorModeHandler#getName()
     */
    public String getName() {

        return NEW_NAME;
    }

    /**
     * @see org.opencms.ade.sitemap.client.I_CmsSitemapEntryEditorModeHandler#getTitle()
     */
    public String getTitle() {

        return "";
    }

    /**
     * @see org.opencms.ade.sitemap.client.I_CmsSitemapEntryEditorModeHandler#handleSubmit(java.lang.String, java.lang.String, java.lang.String, java.util.Map)
     */
    public void handleSubmit(String newTitle, String newUrlName, String vfsPath, Map<String, String> fieldValues) {

        // create
        String path = createPath(newUrlName);
        CmsClientSitemapEntry entry = new CmsClientSitemapEntry();
        entry.setName(newUrlName);
        entry.setSitePath(path);
        entry.setTitle(newTitle);
        // TODO: handle VFS path
        entry.setVfsPath("/");
        entry.getProperties().putAll(fieldValues);
        m_controller.create(entry);

    }

    /**
     * @see org.opencms.ade.sitemap.client.I_CmsSitemapEntryEditorModeHandler#isPathAllowed(java.lang.String)
     */
    public boolean isPathAllowed(String path) {

        return m_controller.getEntry(path) == null;
    }
}
