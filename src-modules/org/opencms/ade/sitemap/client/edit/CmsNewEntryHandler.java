/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/edit/Attic/CmsNewEntryHandler.java,v $
 * Date   : $Date: 2010/10/07 07:56:35 $
 * Version: $Revision: 1.8 $
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

package org.opencms.ade.sitemap.client.edit;

import org.opencms.ade.sitemap.client.Messages;
import org.opencms.ade.sitemap.client.control.CmsSitemapController;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.xml.sitemap.properties.CmsSimplePropertyValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The mode handler for the 'new entry' mode of the sitemap entry editor.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.8 $
 * 
 * @since 8.0.0
 * 
 */
public class CmsNewEntryHandler extends A_CmsSitemapEntryEditorHandler {

    /** Default name for new entries. */
    private static final String NEW_NAME = "new";

    /** 
     * Creates a new instance of this mode handler class.<p>
     * 
     * @param controller the sitemap controller for this mode handler
     * 
     * @param entry the sitemap entry for this mode handler 
     */
    public CmsNewEntryHandler(CmsSitemapController controller, CmsClientSitemapEntry entry) {

        super(controller, entry);
    }

    /**
     * @see org.opencms.ade.sitemap.client.edit.I_CmsSitemapEntryEditorHandler#getDescriptionText()
     */
    public String getDescriptionText() {

        return Messages.get().key(Messages.GUI_PROPERTY_EDITOR_TEXT_NEW_0);
    }

    /**
     * @see org.opencms.ade.sitemap.client.edit.I_CmsSitemapEntryEditorHandler#getDialogTitle()
     */
    public String getDialogTitle() {

        return Messages.get().key(Messages.GUI_NEW_ENTRY_TITLE_0);
    }

    /**
     * @see org.opencms.ade.sitemap.client.edit.I_CmsSitemapEntryEditorHandler#getForbiddenUrlNames()
     */
    public List<String> getForbiddenUrlNames() {

        List<String> result = new ArrayList<String>();
        for (CmsClientSitemapEntry child : m_entry.getSubEntries()) {
            result.add(child.getName());
        }
        return result;
    }

    /**
     * @see org.opencms.ade.sitemap.client.edit.I_CmsSitemapEntryEditorHandler#getName()
     */
    @Override
    public String getName() {

        return NEW_NAME;
    }

    /**
     * @see org.opencms.ade.sitemap.client.edit.I_CmsSitemapEntryEditorHandler#getTitle()
     */
    @Override
    public String getTitle() {

        return "";
    }

    /**
     * @see org.opencms.ade.sitemap.client.edit.I_CmsSitemapEntryEditorHandler#handleSubmit(java.lang.String, java.lang.String, java.lang.String, java.util.Map, boolean)
     */
    public void handleSubmit(
        String newTitle,
        String newUrlName,
        String vfsPath,
        Map<String, CmsSimplePropertyValue> fieldValues,
        boolean editedName) {

        // create
        String path = getPath(newUrlName);
        CmsClientSitemapEntry entry = new CmsClientSitemapEntry();
        entry.setName(newUrlName);
        entry.setSitePath(path);
        entry.setTitle(newTitle);
        entry.setVfsPath(vfsPath);
        entry.getProperties().putAll(fieldValues);
        entry.setPosition(-1);
        m_controller.create(entry);
    }

    /**
     * @see org.opencms.ade.sitemap.client.edit.I_CmsSitemapEntryEditorHandler#hasEditableName()
     */
    public boolean hasEditableName() {

        return true;
    }

    /**
     * Returns the path for the given URL name.<p>
     * 
     * @param urlName the URL name to create the path for
     * 
     * @return the new path for the given URL name
     */
    protected String getPath(String urlName) {

        return m_entry.getSitePath() + urlName + "/";
    }

}
