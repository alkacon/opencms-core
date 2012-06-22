/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.sitemap.client.toolbar;

import org.opencms.ade.sitemap.client.CmsSitemapView;
import org.opencms.ade.sitemap.client.alias.CmsAliasEditor;
import org.opencms.ade.sitemap.client.alias.CmsAliasMessages;
import org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuEntry;

import java.util.List;

/**
 * The sitemap toolbar context menu entry used for displaying the alias editor.<p> 
 */
public class CmsAliasContextMenuEntry implements I_CmsContextMenuEntry {

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuEntry#execute()
     */
    public void execute() {

        CmsAliasEditor editor = new CmsAliasEditor();
        editor.show();
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuEntry#getImageClass()
     */
    public String getImageClass() {

        return null;
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuEntry#getImagePath()
     */
    public String getImagePath() {

        return null;
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuEntry#getJspPath()
     */
    public String getJspPath() {

        return null;
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuEntry#getLabel()
     */
    public String getLabel() {

        return CmsAliasMessages.messageContextMenuEditAliases();
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuEntry#getName()
     */
    public String getName() {

        return "ALIAS";
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuEntry#getReason()
     */
    public String getReason() {

        return CmsAliasMessages.messageEditAliasesNotPermitted();
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuEntry#getSubMenu()
     */
    public List<I_CmsContextMenuEntry> getSubMenu() {

        // TODO: Auto-generated method stub
        return null;
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuEntry#hasSubMenu()
     */
    public boolean hasSubMenu() {

        return false;
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuEntry#isActive()
     */
    public boolean isActive() {

        return CmsSitemapView.getInstance().getController().getData().canEditAliases();
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuEntry#isSeparator()
     */
    public boolean isSeparator() {

        return false;
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuEntry#isVisible()
     */
    public boolean isVisible() {

        return true;
    }

}
