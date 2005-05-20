/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/list/CmsListSearchAction.java,v $
 * Date   : $Date: 2005/05/20 09:52:37 $
 * Version: $Revision: 1.3 $
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

import org.opencms.i18n.CmsMessageContainer;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.tools.A_CmsHtmlIconButton;
import org.opencms.workplace.tools.CmsHtmlIconButtonStyleEnum;

/**
 * Default implementation for a seach action in an html list.<p>
 * 
 * @author Michael Moossen (m.moossen@alkacon.com) 
 * @version $Revision: 1.3 $
 * @since 5.7.3
 */
public class CmsListSearchAction extends CmsListIndependentAction {

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
     * @param listId the id of the associated list
     * @param column the column to search in
     */
    public CmsListSearchAction(String listId, CmsListColumnDefinition column) {

        this(
            listId,
            SEARCH_ACTION_ID,
            new CmsMessageContainer(Messages.get(), Messages.GUI_LIST_ACTION_SEARCH_NAME_0),
            "list/search.gif",
            new CmsMessageContainer(Messages.get(), Messages.GUI_LIST_ACTION_SEARCH_HELP_1, new Object[] {"${key."
                + column.getName().getKey()
                + "}"}),
            new CmsMessageContainer(Messages.get(), Messages.GUI_LIST_ACTION_SEARCH_CONF_0),
            column.getId());
    }

    /**
     * Customized Constructor.<p>
     * 
     * @param listId the id of the associated list
     * @param id unique id
     * @param name the name
     * @param icon the icon
     * @param helpText the help text
     * @param confirmationMessage the confirmation message
     * @param columnId the column to sort
     */
    public CmsListSearchAction(
        String listId,
        String id,
        CmsMessageContainer name,
        String icon,
        CmsMessageContainer helpText,
        CmsMessageContainer confirmationMessage,
        String columnId) {

        super(listId, id, name, helpText, icon, true, confirmationMessage);
        m_columnId = columnId;
        m_defaultShowAllAction = new CmsListIndependentAction(listId, SHOWALL_ACTION_ID) {

            /**
             * @see org.opencms.workplace.tools.I_CmsHtmlIconButton#buttonHtml(CmsWorkplace)
             */
            public String buttonHtml(CmsWorkplace wp) {

                String onClic = getListId()
                    + "ListSearchAction('"
                    + getId()
                    + "', '"
                    + CmsStringUtil.escapeJavaScript(wp.resolveMacros(getConfirmationMessage().key(wp.getLocale())))
                    + "');";
                return A_CmsHtmlIconButton.defaultButtonHtml(
                    CmsHtmlIconButtonStyleEnum.SMALL_ICON_TEXT,
                    getId(),
                    getName().key(wp.getLocale()),
                    getHelpText().key(wp.getLocale()),
                    isEnabled(),
                    getIconPath(),
                    onClic);
            }
        };
        m_defaultShowAllAction.setName(new CmsMessageContainer(Messages.get(), Messages.GUI_LIST_ACTION_SHOWALL_NAME_0));
        m_defaultShowAllAction.setHelpText(new CmsMessageContainer(
            Messages.get(),
            Messages.GUI_LIST_ACTION_SHOWALL_HELP_0));
        m_defaultShowAllAction.setIconPath("list/showall.gif");
        m_defaultShowAllAction.setEnabled(true);
        m_defaultShowAllAction.setConfirmationMessage(new CmsMessageContainer(
            Messages.get(),
            Messages.GUI_LIST_ACTION_SHOWALL_CONF_0));
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsHtmlIconButton#buttonHtml(CmsWorkplace)
     */
    public String buttonHtml(CmsWorkplace wp) {

        String onClic = getListId()
            + "ListSearchAction('"
            + getId()
            + "', '"
            + CmsStringUtil.escapeJavaScript(wp.resolveMacros(getConfirmationMessage().key(wp.getLocale())))
            + "');";
        return A_CmsHtmlIconButton.defaultButtonHtml(
            CmsHtmlIconButtonStyleEnum.SMALL_ICON_TEXT,
            getId(),
            getName().key(wp.getLocale()),
            getHelpText().key(wp.getLocale()),
            isEnabled(),
            getIconPath(),
            onClic);
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