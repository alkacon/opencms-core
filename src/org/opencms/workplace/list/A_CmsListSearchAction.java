/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/list/A_CmsListSearchAction.java,v $
 * Date   : $Date: 2005/07/04 12:29:51 $
 * Version: $Revision: 1.10 $
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
 * @author Michael Moossen  
 * 
 * @version $Revision: 1.10 $ 
 * 
 * @since 6.0.0 
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
    private static final String SEARCH_ICON = "list/search.png";

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
    private static final String SHOWALL_ICON = "list/showall.png";

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
     */
    protected A_CmsListSearchAction() {

        super(SEARCH_ACTION_ID);
        setName(SEARCH_NAME);
        setIconPath(SEARCH_ICON);
        setConfirmationMessage(SEARCH_CONFIRMATION);
        setHelpText(null);

        m_defaultShowAllAction = createDefaultShowAllAction();
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsHtmlIconButton#buttonHtml(CmsWorkplace)
     */
    public String buttonHtml(CmsWorkplace wp) {

        String onClic = "listSearchAction('"
            + getListId()
            + "', '"
            + getId()
            + "', '"
            + CmsStringUtil.escapeJavaScript(wp.resolveMacros(getConfirmationMessage().key(wp.getLocale())))
            + "');";
        return A_CmsHtmlIconButton.defaultButtonHtml(
            wp.getJsp(),
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
     * @see org.opencms.workplace.list.A_CmsListAction#setListId(java.lang.String)
     */
    public void setListId(String listId) {

        super.setListId(listId);
        m_defaultShowAllAction.setListId(listId);
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
     * @return default show all action
     */
    private I_CmsListAction createDefaultShowAllAction() {

        I_CmsListAction defaultShowAllAction = new CmsListIndependentAction(SHOWALL_ACTION_ID) {

            /**
             * @see org.opencms.workplace.tools.I_CmsHtmlIconButton#buttonHtml(CmsWorkplace)
             */
            public String buttonHtml(CmsWorkplace wp) {

                String onClic = "listSearchAction('"
                    + getListId()
                    + "', '"
                    + getId()
                    + "', '"
                    + CmsStringUtil.escapeJavaScript(wp.resolveMacros(getConfirmationMessage().key(wp.getLocale())))
                    + "');";
                return A_CmsHtmlIconButton.defaultButtonHtml(
                    wp.getJsp(),
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