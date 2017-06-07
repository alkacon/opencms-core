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

package org.opencms.ade.sitemap.client.hoverbar;

import org.opencms.gwt.client.ui.contextmenu.A_CmsContextMenuItem;
import org.opencms.gwt.client.ui.contextmenu.CmsContextMenuItem;
import org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuEntry;

import java.util.List;

/**
 * Common super class for all sitemap context menu entries.<p>
 *
 * @since 8.0.0
 */
public abstract class A_CmsSitemapMenuEntry implements I_CmsContextMenuEntry {

    /** The reason if the entry is de-activated. */
    private String m_disabledReason;

    /** The hoverbar. */
    private CmsSitemapHoverbar m_hoverbar;

    /** Flag to indicate if this menu entry is active. */
    private boolean m_isActive;

    /** Flag to indicate if the menu entry is visible. */
    private boolean m_isVisible;

    /** The label (text) for the menu entry. */
    private String m_label;

    /**
     * Constructor.<p>
     *
     * @param hoverbar the hoverbar
     */
    public A_CmsSitemapMenuEntry(CmsSitemapHoverbar hoverbar) {

        m_hoverbar = hoverbar;
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

        return m_disabledReason;
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

        return m_isActive;
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

        return m_isVisible;
    }

    /**
     * Executed when the context-menu is opened.<p>
     */
    public abstract void onShow();

    /**
     * Sets if this menu entry is active.<p>
     *
     * @param active <code>true</code> to set this menu entry active
     */
    public void setActive(boolean active) {

        m_isActive = active;
    }

    /**
     * Sets the reason if the entry is de-activated.<p>
     *
     * @param reason the reason if the entry is de-activated
     */
    public void setDisabledReason(String reason) {

        m_disabledReason = reason;
    }

    /**
     * Sets the label (text) for the menu entry.<p>
     *
     * @param label the label (text) for the menu entry
     */
    public void setLabel(String label) {

        m_label = label;
    }

    /**
     * Sets if the menu entry is visible.<p>
     *
     * @param visible <code>true</code> to set the entry visible
     */
    public void setVisible(boolean visible) {

        m_isVisible = visible;
    }

    /**
     * De-attaches the hoverbar.<p>
     */
    protected void deattachHoverbar() {

        m_hoverbar.hide();
    }

    /**
     * Returns the hoverbar.<p>
     *
     * @return the hoverbar
     */
    protected CmsSitemapHoverbar getHoverbar() {

        return m_hoverbar;
    }
}
