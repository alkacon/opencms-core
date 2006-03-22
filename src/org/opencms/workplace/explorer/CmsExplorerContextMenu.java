/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/explorer/CmsExplorerContextMenu.java,v $
 * Date   : $Date: 2006/03/22 08:33:22 $
 * Version: $Revision: 1.12.2.4 $
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

import org.opencms.file.CmsObject;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPermissionSet;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

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
 * @version $Revision: 1.12.2.4 $ 
 * 
 * @since 6.0.0 
 */
public class CmsExplorerContextMenu {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsExplorerContextMenu.class);

    /** All context menu entries. */
    private List m_allEntries;
    /** Stores already generated javascript menu outputs with a Locale object as key. */
    private HashMap m_generatedScripts;
    /** Indicated if this is a multi context menu. */
    private boolean m_multiMenu;

    /**
     * Default constructor.<p>
     */
    public CmsExplorerContextMenu() {

        m_allEntries = new ArrayList();
        m_generatedScripts = new HashMap();
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
                LOG.error(Messages.get().key(Messages.LOG_WRONG_ORDER_CONTEXT_MENU_1, key));
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
            LOG.debug(Messages.get().key(Messages.LOG_ADD_MENU_ENTRY_2, key, order));
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
            LOG.error(Messages.get().key(Messages.LOG_WRONG_MENU_SEP_ORDER_0, order));
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
            LOG.debug(Messages.get().key(Messages.LOG_WRONG_MENU_SEP_ORDER_0, order));
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
     * Builds the Javascript to create the context menu.<p>
     * 
     * @param cms the CmsObject
     * @param settings the explorer type settings for which the context menu is created
     * @param resTypeId the id of the resource type which uses the context menu
     * @param locale the locale to generate the context menu for
     * @return the JavaScript output to create the context menu
     */
    public String getJSEntries(CmsObject cms, CmsExplorerTypeSettings settings, int resTypeId, Locale locale) {

        // try to get the stored entries from the Map
        String entries = (String)m_generatedScripts.get(locale);
        if (entries == null) {
            //CmsMessages messages = OpenCms.getWorkplaceManager().getMessages(locale);

            // entries not yet in Map, so generate them
            StringBuffer result = new StringBuffer(4096);
            String jspWorkplaceUri = OpenCms.getLinkManager().substituteLink(cms, CmsWorkplace.PATH_WORKPLACE);

            if (!isMultiMenu()) {
                // create the JS for the resource object
                result.append("\nvi.resource[").append(resTypeId).append("]=new res(\"").append(settings.getName()).append(
                    "\", ");
                result.append("\"");
                result.append(Messages.get().key(locale, settings.getKey()));
                result.append("\", vi.skinPath + \"filetypes/");
                result.append(settings.getIcon());
                result.append("\", \"");
                result.append(settings.getNewResourceUri());
                result.append("\", true);\n");
            }

            Iterator i = getAllEntries().iterator();
            while (i.hasNext()) {
                // create the context menu items
                CmsExplorerContextMenuItem item = (CmsExplorerContextMenuItem)i.next();
                result.append("addMenuEntry(");
                if (isMultiMenu()) {
                    result.append("'multi'");
                } else {
                    result.append(resTypeId);
                }
                result.append(", ");
                if (CmsExplorerContextMenuItem.TYPE_ENTRY.equals(item.getType())) {
                    // create a menu entry
                    result.append("\"").append(Messages.get().key(locale, item.getKey())).append("\", ");
                    result.append("\"");
                    if (item.getUri().startsWith("/")) {
                        result.append(OpenCms.getLinkManager().substituteLink(cms, item.getUri()));
                    } else {
                        result.append(jspWorkplaceUri);
                        result.append(item.getUri());
                    }

                    result.append("\", ");
                    // check the item target
                    String target = item.getTarget();
                    if (target == null) {
                        target = "";
                    }
                    result.append("\"'");
                    result.append(target);
                    result.append("'\", ");
                    // remove all blanks from the rule String
                    String rules = CmsStringUtil.substitute(item.getRules(), " ", "");
                    // parse the rules to create the autolock column
                    rules = parseRules(rules, item.getKey());
                    result.append("\"");
                    result.append(rules);
                    result.append("\");\n");
                    // result: addMenuEntry([id], "[language_key]", "[dialogURI]", "'[target]'", "ddiiiiaaaiaaaiddddddddddddiiiidddd");
                } else {
                    // create a separator entry
                    result.append("\"-\", \" \", \"''\", \"\");\n");
                    // result: addMenuEntry([id], "-", " ", "''", "ddaaaaaaaaaaaaddddddddddddaaaadddd");
                }
            }
            entries = result.toString();
            // store the generated entries
            m_generatedScripts.put(locale, entries);
        }

        if (!isMultiMenu()) {
            // determine if this resource type is editable for the current user
            CmsPermissionSet permissions = settings.getAccess().getPermissions(cms);
            if (!permissions.requiresWritePermission()) {
                // the type is not editable, set editable to false
                entries += "vi.resource[" + resTypeId + "].editable = false;\n";
            }
        }

        return entries;
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

    /**
     * Parses the rules and adds a column for the autolock feature of resources.<p>
     * 
     * @param rules the current rules
     * @param key the key name of the current item
     * @return the rules with added autlock rules column
     */
    private String parseRules(String rules, String key) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(rules)) {
            return "";
        }
        StringBuffer newRules = new StringBuffer(rules.length() + 4);
        newRules.append(rules.substring(0, 6));
        if (Messages.GUI_EXPLORER_CONTEXT_LOCK_0.equalsIgnoreCase(key) || Messages.GUI_EXPLORER_CONTEXT_UNLOCK_0.equalsIgnoreCase(key)) {
            // for "lock" and "unlock" item, use same rules as "unlocked" column
            newRules.append(rules.substring(2, 6));
        } else {
            // for all other items, use same rules as "locked exclusively by current user" column
            newRules.append(rules.substring(6, 10));
        }
        newRules.append(rules.substring(6));
        return newRules.toString();
    }
}