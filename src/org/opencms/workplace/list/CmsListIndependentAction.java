/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/list/CmsListIndependentAction.java,v $
 * Date   : $Date: 2005/04/22 14:44:11 $
 * Version: $Revision: 1.2 $
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
 * Default implementation of a independent action for a html list.<p>
 * 
 * @author Michael Moossen (m.moossen@alkacon.com) 
 * @version $Revision: 1.2 $
 * @since 5.7.3
 */
public class CmsListIndependentAction extends A_CmsListAction {

    /**
     * Default Constructor.<p>
     * 
     * @param listId the id of the associated list
     * @param id unique id
     * @param name the name
     * @param iconPath the link to the icon
     * @param helpText the help text
     * @param enabled if enabled
     * @param confirmationMessage the confirmation message
     */
    public CmsListIndependentAction(
        String listId,
        String id,
        CmsMessageContainer name,
        String iconPath,
        CmsMessageContainer helpText,
        boolean enabled,
        CmsMessageContainer confirmationMessage) {

        super(listId, id, name, iconPath, helpText, enabled, confirmationMessage);
    }

    /**
     * @see org.opencms.workplace.list.I_CmsHtmlButton#buttonHtml(CmsWorkplace)
     */
    public String buttonHtml(CmsWorkplace wp) {

        if (isEnabled()) {
            String onClic = getListId()
                + "ListIndepAction('"
                + getId()
                + "', '"
                + CmsStringUtil.escapeJavaScript(wp.resolveMacros(getConfirmationMessage().key(wp.getLocale())))
                + "');";
            return A_CmsHtmlIconButton.defaultButtonHtml(getId(), getName().key(wp.getLocale()), getHelpText().key(
                wp.getLocale()), isEnabled(), getIconPath(), onClic);
        }
        return "";
    }

}