/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/edit/Attic/I_CmsSitemapEntryEditorHandler.java,v $
 * Date   : $Date: 2011/05/06 08:33:51 $
 * Version: $Revision: 1.11 $
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

package org.opencms.ade.sitemap.client.edit;

import org.opencms.ade.sitemap.client.control.CmsSitemapController;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.ade.sitemap.shared.CmsPropertyModification;
import org.opencms.gwt.shared.CmsListInfoBean;

import java.util.List;

/**
 * An interface for sitemap entry editor modes.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.11 $
 * 
 * @since 8.0.0
 */
public interface I_CmsSitemapEntryEditorHandler {

    /**
     * Returns the sitemap controller.<p>
     * 
     * @return the sitemap controller
     */
    CmsSitemapController getController();

    /**
     * Returns the description text which will be displayed in the sitemap entry editor.<p>
     * 
     * @return a description text 
     */
    String getDescriptionText();

    /**
     * Returns the text which should be used for the title of the sitemap entry editor dialog.
     *  
     * @return the dialog title for the sitemap entry editor 
     */
    String getDialogTitle();

    /**
     * Returns the current entry.<p>
     * 
     * @return the current entry
     */
    CmsClientSitemapEntry getEntry();

    /**
     * Returns the URL names which the new URL name of the entry must not be equal to.<p>
     * 
     * @return a list of forbidden URL names 
     */
    List<String> getForbiddenUrlNames();

    /**
     * Returns the URL name with which the sitemap entry editor should be initialized.<p>
     * 
     * @return the initial URL name
     */
    String getName();

    /**
     * Returns the page info bean.<p>
     * 
     * @return the page info bean
     */
    CmsListInfoBean getPageInfo();

    /**
     * Returns the title with which the sitemap entry editor should be initialized.<p>
     * 
     * @return the initial title 
     */
    String getTitle();

    /**
     * Handles the submit action for the sitemap entry editor.<p>
     * 
     * @param newUrlName the new url name 
     * @param vfsPath the new vfs path 
     * @param propertyChanges the property changes  
     * @param editedName if true, the URL name has been edited 
     * @param reloadMode the information about which entry should reloaded 
     */
    void handleSubmit(
        String newUrlName,
        String vfsPath,
        List<CmsPropertyModification> propertyChanges,
        boolean editedName,
        CmsSitemapController.ReloadMode reloadMode);

    /**
     * Returns if the handled entry has an editable name.<p>
     * 
     * @return <code>true</code> if the handled entry has an editable name
     */
    boolean hasEditableName();

    /** 
     * Should return true if the sitemap editor is running in simple mode.<p>
     * 
     * @return true if the sitemap editor is running in simple mode 
     */
    boolean isSimpleMode();

}
