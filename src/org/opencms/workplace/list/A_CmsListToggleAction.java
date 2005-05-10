/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/list/Attic/A_CmsListToggleAction.java,v $
 * Date   : $Date: 2005/05/10 12:04:58 $
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

/**
 * Abstract implementation of a toggle action for a html list.<p>
 * 
 * You have to extend this class and implement the <code>{@link #selectAction()}</code> method.<p>
 * 
 * @author Michael Moossen (m.moossen@alkacon.com) 
 * @version $Revision: 1.1 $
 * @since 5.7.3
 */
public abstract class A_CmsListToggleAction extends CmsListDefaultAction {

    /**
     * Default Constructor.<p>
     * 
     * @param listId the id of the associated list
     * @param id unique id
     */
    protected A_CmsListToggleAction(String listId, String id) {

        super(listId, id);

    }

    /**
     * @see org.opencms.workplace.list.I_CmsHtmlIconButton#buttonHtml(CmsWorkplace)
     */
    public String buttonHtml(CmsWorkplace wp) {

        String id = getId() + getItem().getId();
        String onClic = getListId()
            + "ListAction('"
            + getId()
            + "', '"
            + CmsStringUtil.escapeJavaScript(wp.resolveMacros(getConfirmationMessage().key(wp.getLocale())))
            + "', '"
            + CmsStringUtil.escapeJavaScript(getItem().getId())
            + "');";

        return A_CmsHtmlIconButton.defaultButtonHtml(
            id,
            null,
            getHelpText().key(wp.getLocale()),
            isEnabled(),
            getIconPath(),
            onClic);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListAction#getConfirmationMessage()
     */
    public CmsMessageContainer getConfirmationMessage() {

        return selectAction().getConfirmationMessage();
    }

    /**
     * @see org.opencms.workplace.list.I_CmsHtmlIconButton#getHelpText()
     */
    public CmsMessageContainer getHelpText() {

        return selectAction().getHelpText();
    }

    /**
     * @see org.opencms.workplace.list.I_CmsHtmlIconButton#getIconPath()
     */
    public String getIconPath() {

        return selectAction().getIconPath();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsHtmlIconButton#getId()
     */
    public String getId() {

        return selectAction().getId();
    }

    /**
     * @see org.opencms.workplace.list.I_CmsHtmlIconButton#getName()
     */
    public CmsMessageContainer getName() {

        return selectAction().getName();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsHtmlIconButton#isEnabled()
     */
    public boolean isEnabled() {

        return selectAction().isEnabled();
    }

    /**
     * Selects and sets the current action.<p>
     *
     * @return the selected action
     */
    public abstract I_CmsListDirectAction selectAction();

    /**
     * @see org.opencms.workplace.list.CmsListDirectAction#setItem(org.opencms.workplace.list.CmsListItem)
     */
    public void setItem(CmsListItem item) {

        super.setItem(item);
        selectAction().setItem(item);
    }

}