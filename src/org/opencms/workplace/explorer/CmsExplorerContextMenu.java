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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.explorer;

import org.opencms.main.CmsLog;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;

/**
 * Provides methods to build a context menu for an explorer resource type.<p>
 *
 * This object stores all entries which are displayed in a context menu in a sorted list.<p>
 *
 * @since 6.0.0
 */
public class CmsExplorerContextMenu {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsExplorerContextMenu.class);

    /** All context menu entries. */
    private List<CmsExplorerContextMenuItem> m_allEntries;

    /** Indicated if this is a multi context menu. */
    private boolean m_multiMenu;

    /**
     * Default constructor.<p>
     */
    public CmsExplorerContextMenu() {

        m_allEntries = new ArrayList<CmsExplorerContextMenuItem>();
    }

    /**
     * Adds a menu entry to the list of context menu items.<p>
     *
     * @param item the entry item to add to the list
     */
    public void addContextMenuEntry(CmsExplorerContextMenuItem item) {

        item.setType(CmsExplorerContextMenuItem.TYPE_ENTRY);
        m_allEntries.add(item);
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_ADD_MENU_ENTRY_2, item.getKey(), item.getUri()));
        }
    }

    /**
     * Adds a menu separator to the list of context menu items.<p>
     *
     * @param item the separator item to add to the list
     */
    public void addContextMenuSeparator(CmsExplorerContextMenuItem item) {

        item.setType(CmsExplorerContextMenuItem.TYPE_SEPARATOR);
        m_allEntries.add(item);
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_ADD_MENU_SEPARATOR_1, item.getType()));
        }
    }

    /**
     * Adds a list of CmsContextMenuItem objects to the context menu list.<p>
     *
     * The list is sorted by their order after that operation.<p>
     *
     * @param entries a list of initialized context menu items
     */
    public void addEntries(List<CmsExplorerContextMenuItem> entries) {

        m_allEntries.addAll(entries);
    }

    /**
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() {

        CmsExplorerContextMenu objectClone = new CmsExplorerContextMenu();
        objectClone.setMultiMenu(m_multiMenu);
        objectClone.setAllEntries(m_allEntries);
        return objectClone;
    }

    /**
     * Returns all entries of the context menu.<p>
     *
     * @return all entries of the context menu
     */
    public List<CmsExplorerContextMenuItem> getAllEntries() {

        return m_allEntries;
    }

    /**
     * Tests if the context menu is empty.<p>
     *
     * @return true or false
     */
    public boolean isEmpty() {

        return m_allEntries.isEmpty();
    }

    /**
     * Returns true if the menu is a multi context menu for more than one selected file.<p>
     *
     * @return if the menu is a multi context menu for more than one selected file
     */
    public boolean isMultiMenu() {

        return m_multiMenu;
    }

    /**
     * Sets all entries of the context menu.<p>
     *
     * @param entries all entries of the context menu
     */
    public void setAllEntries(List<CmsExplorerContextMenuItem> entries) {

        m_allEntries = entries;
    }

    /**
     * Sets if the menu is a multi context menu for more than one selected file.<p>
     *
     * @param multiMenu true, if the menu is a multi context menu for more than one selected file, otherwise false
     */
    public void setMultiMenu(boolean multiMenu) {

        m_multiMenu = multiMenu;
    }

}