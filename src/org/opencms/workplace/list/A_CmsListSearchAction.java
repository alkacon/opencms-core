/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/list/A_CmsListSearchAction.java,v $
 * Date   : $Date: 2005/05/20 15:11:42 $
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

import org.opencms.i18n.CmsMessageContainer;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.tools.A_CmsHtmlIconButton;
import org.opencms.workplace.tools.CmsHtmlIconButtonStyleEnum;

/**
 * Abstract implementation of a search action.<p>
 * 
 * It provides the default show all action accessor and the rendering method.<p>
 * 
 * @author Michael Moossen (m.moossen@alkacon.com) 
 * @version $Revision: 1.1 $
 * @since 5.7.3
 */
public abstract class A_CmsListSearchAction extends CmsListIndependentAction {

    /** The action id for the search action. */
    public static final String SEARCH_ACTION_ID = "search";

    /** The action id for the show all action. */
    public static final String SHOWALL_ACTION_ID = "showall";
    /** Default confirmation message for search action. */
    private static final CmsMessageContainer SEARCH_CONFIRMATION = new CmsMessageContainer(
        Messages.get(),
        Messages.GUI_LIST_ACTION_SEARCH_CONF_0);
    /** Default icon for search action. */
    private static final String SEARCH_ICON = "list/search.gif";

    /** Default name for search action. */
    private static final CmsMessageContainer SEARCH_NAME = new CmsMessageContainer(
        Messages.get(),
        Messages.GUI_LIST_ACTION_SEARCH_NAME_0);
    /** Default confirmation message for show all action. */
    private static final CmsMessageContainer SHOWALL_CONFIRMATION = new CmsMessageContainer(
        Messages.get(),
        Messages.GUI_LIST_ACTION_SHOWALL_CONF_0);
    /** Default help text for show all action. */
    private static final CmsMessageContainer SHOWALL_HELPTEXT = new CmsMessageContainer(
        Messages.get(),
        Messages.GUI_LIST_ACTION_SHOWALL_HELP_0);
    /** Default icon for show all action. */
    private static final String SHOWALL_ICON = "list/showall.gif";

    /** Default name for show all action. */
    private static final CmsMessageContainer SHOWALL_NAME = new CmsMessageContainer(
        Messages.get(),
        Messages.GUI_LIST_ACTION_SHOWALL_NAME_0);

    /** A default show all action. */
    public final I_CmsListAction m_defaultShowAllAction;

    /** Show all action. */
    private I_CmsListAction m_showAllAction = null;

    /**
     * Default Constructor.<p>
     * 
     * @param listId the id of the associated list
     */
    protected A_CmsListSearchAction(String listId) {

        super(listId, SEARCH_ACTION_ID);
        setName(SEARCH_NAME);
        setIconPath(SEARCH_ICON);
        setConfirmationMessage(SEARCH_CONFIRMATION);
        setHelpText(null);

        m_defaultShowAllAction = createDefaultShowAllAction(listId);
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

    /**
     * Creates a default show all action.<p>
     * 
     * @param listId the is of the list
     * 
     * @return default show all action
     */
    private I_CmsListAction createDefaultShowAllAction(String listId) {

        I_CmsListAction defaultShowAllAction = new CmsListIndependentAction(listId, SHOWALL_ACTION_ID) {

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
        defaultShowAllAction.setName(SHOWALL_NAME);
        defaultShowAllAction.setHelpText(SHOWALL_HELPTEXT);
        defaultShowAllAction.setIconPath(SHOWALL_ICON);
        defaultShowAllAction.setConfirmationMessage(SHOWALL_CONFIRMATION);
        return defaultShowAllAction;
    }
}