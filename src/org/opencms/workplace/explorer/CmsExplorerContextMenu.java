/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/explorer/CmsExplorerContextMenu.java,v $
 * Date   : $Date: 2007/02/06 11:29:35 $
 * Version: $Revision: 1.13.4.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;

/**
 * Provides methods to build a context menu for an explorer resource type.<p>
 * 
 * This object stores all entries which are displayed in a context menu in a sorted list.
 * The sort order is specified in an attribute of the context menu subnodes
 * in the OpenCms configuration.<p> 
 * 
 * @author Andreas Zahner 
 * 
 * @version $Revision: 1.13.4.2 $ 
 * 
 * @since 6.0.0 
 */
public class CmsExplorerContextMenu {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsExplorerContextMenu.class);

    /** All context menu entries. */
    private List m_allEntries;
    
    /** Indicated if this is a multi context menu. */
    private boolean m_multiMenu;

    /**
     * Default constructor.<p>
     */
    public CmsExplorerContextMenu() {

        m_allEntries = new ArrayList();
    }
    
    /**
     * Adds a list of CmsContextMenuItem objects to the context menu list.<p>
     * 
     * The list is sorted by their order after that operation.<p>
     * 
     * @param entries a list of initialized context menu items
     */
    public void addEntries(List entries) {

        m_allEntries.addAll(entries);
        sortEntries();
    }

    /**
     * Adds a single CmsContextMenuItem object to the context menu list.<p>
     * 
     * The list is sorted by their order after that operation.<p>
     * 
     * @param entry a single context menu item
     */
    public void addEntry(CmsExplorerContextMenuItem entry) {

        m_allEntries.add(entry);
        sortEntries();
    }

    /**
     * Adds a single context menu entry to the list of context menu items.<p>
     * 
     * @param key the key of the current entry 
     * @param uri the dialog URI to call with the current entry
     * @param rules the display rules
     * @param target the frame target of the menu entry
     * @param order the sort order of the current entry
     */
    public void addMenuEntry(String key, String uri, String rules, String target, String order) {

        Integer orderValue = new Integer(0);
        try {
            orderValue = Integer.valueOf(order);
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().getBundle().key(Messages.LOG_WRONG_ORDER_CONTEXT_MENU_1, key));
            }
        }
        CmsExplorerContextMenuItem item = new CmsExplorerContextMenuItem(
            CmsExplorerContextMenuItem.TYPE_ENTRY,
            key,
            uri,
            rules,
            target,
            orderValue);

        addEntry(item);
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_ADD_MENU_ENTRY_2, key, order));
        }
    }

    /**
     * Adds a menu separator to the list of context menu items.<p>
     * 
     * @param order the sort order of the separator
     */
    public void addMenuSeparator(String order) {

        Integer orderValue = new Integer(0);
        try {
            orderValue = Integer.valueOf(order);
        } catch (Exception e) {
            LOG.error(Messages.get().getBundle().key(Messages.LOG_WRONG_MENU_SEP_ORDER_0, order));
        }
        CmsExplorerContextMenuItem item = new CmsExplorerContextMenuItem(
            CmsExplorerContextMenuItem.TYPE_SEPARATOR,
            null,
            null,
            null,
            null,
            orderValue);
        addEntry(item);
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_WRONG_MENU_SEP_ORDER_0, order));
        }
    }

    /**
     * @see java.lang.Object#clone()
     */
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
    public List getAllEntries() {

        return m_allEntries;
    }

    /**
     * Tests if the context menu is empty.<p>
     * 
     * @return true or false
     */
    public boolean isEmpty() {

        boolean empty = true;
        if (m_allEntries.size() > 0) {
            empty = false;
        }
        return empty;
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
     * The list is sorted by their order after that operation.<p>
     * 
     * @param entries all entries of the context menu
     */
    public void setAllEntries(List entries) {

        m_allEntries = entries;
        sortEntries();
    }

    /**
     * Sets if the menu is a multi context menu for more than one selected file.<p>
     * 
     * @param multiMenu true, if the menu is a multi context menu for more than one selected file, otherwise false
     */
    public void setMultiMenu(boolean multiMenu) {

        m_multiMenu = multiMenu;
    }

    /**
     * Sorts the list of entries according to the value of the "order" attribute in the configuration.<p>
     */
    public void sortEntries() {

        Collections.sort(m_allEntries);
    }
}