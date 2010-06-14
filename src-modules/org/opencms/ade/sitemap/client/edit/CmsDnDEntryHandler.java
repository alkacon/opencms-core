/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/edit/Attic/CmsDnDEntryHandler.java,v $
 * Date   : $Date: 2010/06/14 08:41:25 $
 * Version: $Revision: 1.2 $
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
import org.opencms.file.CmsResource;

import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The handler for the 'collision resolution while drag'n drop' mode of the sitemap entry editor.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 8.0.0
 */
public class CmsDnDEntryHandler extends A_CmsSitemapEntryEditorHandler {

    /** The callback. */
    private AsyncCallback<String> m_callback;

    /** The drag'n drop destination path. */
    private String m_destPath;

    /**
     * Creates a new instance of this class.<p>
     * 
     * @param controller the sitemap controller for this mode 
     * @param entry the sitemap entry for this mode 
     * @param destPath 
     * @param callback the callback
     */
    public CmsDnDEntryHandler(
        CmsSitemapController controller,
        CmsClientSitemapEntry entry,
        String destPath,
        AsyncCallback<String> callback) {

        super(controller, entry);
        m_callback = callback;
        m_destPath = destPath;
    }

    /**
     * @see org.opencms.ade.sitemap.client.edit.I_CmsSitemapEntryEditorHandler#getDescriptionText()
     */
    public String getDescriptionText() {

        return Messages.get().key(Messages.GUI_PROPERTY_EDITOR_TEXT_0);
    }

    /**
     * @see org.opencms.ade.sitemap.client.edit.I_CmsSitemapEntryEditorHandler#handleCancel()
     */
    @Override
    public void handleCancel() {

        // cancel drag'n drop action
        m_callback.onFailure(null);
    }

    /**
     * @see org.opencms.ade.sitemap.client.edit.I_CmsSitemapEntryEditorHandler#handleSubmit(java.lang.String, java.lang.String, java.lang.String, java.util.Map)
     */
    public void handleSubmit(String newTitle, String newUrlName, String vfsPath, Map<String, String> fieldValues) {

        // finalize dnd action
        m_callback.onSuccess(newUrlName);

        // commit the rest
        m_controller.edit(m_controller.getEntry(m_destPath), newTitle, vfsPath, fieldValues);
    }

    /**
     * @see org.opencms.ade.sitemap.client.edit.I_CmsSitemapEntryEditorHandler#isPathAllowed(java.lang.String)
     */
    public boolean isPathAllowed(String urlName) {

        return (getController().getEntry(getPath(urlName)) == null);
    }

    /**
     * Returns the path for the given URL name.<p>
     * 
     * @param urlName the URL name to create the path for
     * 
     * @return the new path for the given URL name
     */
    protected String getPath(String urlName) {

        return CmsResource.getParentFolder(m_destPath) + urlName + "/";
    }
}
