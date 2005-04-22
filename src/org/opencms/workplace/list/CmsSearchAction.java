/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/list/Attic/CmsSearchAction.java,v $
 * Date   : $Date: 2005/04/22 08:38:52 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.workplace.list;

/**
 * Default implementation for a seach action in an html list.<p>
 * 
 * @author Michael Moossen (m.moossen@alkacon.com) 
 * @version $Revision: 1.1 $
 * @since 5.7.3
 */
public class CmsSearchAction extends CmsListIndependentAction {

    /** The action id for the search action. */
    public static final String SEARCH_ACTION_ID = "search";
    
    /** The action id for the show all action. */
    public static final String SHOWALL_ACTION_ID = "showall";
    
    /** A default show all action. */
    public final I_CmsListAction m_defaultShowAllAction;

    /** Column name to search in. */
    private final String m_columnId;

    /** Show all action. */
    private I_CmsListAction m_showAllAction = null;

    /**
     * Default Constructor.<p>
     * 
     * @param list the list
     * @param columnId the column to sort
     */
    public CmsSearchAction(CmsHtmlList list, String columnId) {

        this(
            list,
            SEARCH_ACTION_ID, "${key." + Messages.GUI_LIST_ACTION_SEARCH_NAME_0 + "}",
            "${key." + Messages.GUI_LIST_ACTION_SEARCH_ICON_0 + "}",
            "${key." + Messages.GUI_LIST_ACTION_SEARCH_HELP_1 + "|" + list.getMetadata().getColumnDefinition(columnId).getName() + "}",
            columnId);
    }

    /**
     * Customized Constructor.<p>
     * 
     * @param list the list
     * @param id unique id
     * @param name the name
     * @param icon the icon
     * @param helpText the help text
     * @param columnId the column to sort
     */
    public CmsSearchAction(CmsHtmlList list, String id, String name, String icon, String helpText, String columnId) {

        super(list, id, name, icon, helpText, true, ""); 
        m_columnId = columnId;
        m_defaultShowAllAction = new CmsListIndependentAction(
            list,
            SHOWALL_ACTION_ID,
            "${key." + Messages.GUI_LIST_ACTION_SHOWALL_NAME_0 + "}",
            "${key." + Messages.GUI_LIST_ACTION_SHOWALL_ICON_0 + "}",
            "${key." + Messages.GUI_LIST_ACTION_SHOWALL_HELP_0 + "}",
            true,
            null);
    }

    /**
     * Returns the column name to sort.<p>
     * 
     * @return the column name to sort
     */
    public String getColumnId() {

        return m_columnId;
    }

    /**
     * Returns the Show-All action.<p>
     *
     * @return the Show-All action
     */
    public I_CmsListAction getShowAllAction() {

        return m_showAllAction;
    }

    /**
     * Sets the Show-All action.<p>
     *
     * @param showAllAction the Show-All action to set
     */
    public void setShowAllAction(I_CmsListAction showAllAction) {

        m_showAllAction = showAllAction;
    }

    /**
     * Sets the current used show-all action to the default.<p>
     */
    public void useDefaultShowAllAction() {

        m_showAllAction = m_defaultShowAllAction;
    }
}