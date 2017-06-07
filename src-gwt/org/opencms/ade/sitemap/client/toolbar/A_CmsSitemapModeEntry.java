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

package org.opencms.ade.sitemap.client.toolbar;

import org.opencms.gwt.client.ui.contextmenu.A_CmsContextMenuItem;
import org.opencms.gwt.client.ui.contextmenu.CmsContextMenuItem;
import org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuEntry;

import java.util.List;

/**
 * Common super class for all sitemap context menu entries.<p>
 *
 * @since 8.0.0
 */
public abstract class A_CmsSitemapModeEntry implements I_CmsContextMenuEntry {

    /** The label (text) for the menu entry. */
    private String m_label;

    /**
     * Constructor.<p>
     *
     * @param label the label
     */
    public A_CmsSitemapModeEntry(String label) {

        m_label = label;
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuEntry#generateMenuItem()
     */
    public A_CmsContextMenuItem generateMenuItem() {

        return new CmsContextMenuItem(this);
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuEntry#getIconClass()
     */
    public String getIconClass() {

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

        return m_label;
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuEntry#getName()
     */
    public String getName() {

        return null;
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuEntry#getReason()
     */
    public String getReason() {

        return null;
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuEntry#getSubMenu()
     */
    public List<I_CmsContextMenuEntry> getSubMenu() {

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

        return true;
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
