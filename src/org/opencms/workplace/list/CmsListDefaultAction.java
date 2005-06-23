/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/list/CmsListDefaultAction.java,v $
 * Date   : $Date: 2005/06/23 11:11:43 $
 * Version: $Revision: 1.14 $
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

import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.tools.A_CmsHtmlIconButton;
import org.opencms.workplace.tools.CmsHtmlIconButtonStyleEnum;

import java.text.MessageFormat;

/**
 * Implementation of a default action in a html list column.<p>
 * 
 * @author Michael Moossen  
 * 
 * @version $Revision: 1.14 $ 
 * 
 * @since 6.0.0 
 */
public class CmsListDefaultAction extends CmsListDirectAction {

    /** Id of the column. */
    private String m_column;

    /**
     * Default Constructor.<p>
     * 
     * @param id unique id
     */
    public CmsListDefaultAction(String id) {

        super(id);
    }

    /**
     * @see org.opencms.workplace.list.CmsListDirectAction#buttonHtml(CmsWorkplace)
     */
    public String buttonHtml(CmsWorkplace wp) {

        if (m_column == null) {
            return super.buttonHtml(wp);
        }
        String id = getId() + getItem().getId();
        String name = (getItem().get(m_column) != null) ? getItem().get(m_column).toString() : getName().key(
            wp.getLocale());
        String confirmationMessage = getConfirmationMessage().key(wp.getLocale());
        String helpText = getHelpText().key(wp.getLocale());
        if (getColumn() != null && getItem().get(getColumn()) != null) {
            confirmationMessage = new MessageFormat(confirmationMessage, wp.getLocale()).format(new Object[] {getItem().get(
                getColumn())});
            helpText = new MessageFormat(helpText, wp.getLocale()).format(new Object[] {getItem().get(getColumn())});
        }
        StringBuffer onClic = new StringBuffer(128);
        onClic.append("listAction('");
        onClic.append(getListId());
        onClic.append("', '");
        onClic.append(getId());
        onClic.append("', '");
        if (getColumn() == null
            || getItem().get(getColumn()) != null
            || confirmationMessage.equals(new MessageFormat(confirmationMessage, wp.getLocale()).format(new Object[] {""}))) {
            onClic.append("conf" + getId());
        } else {
            onClic.append(CmsStringUtil.escapeJavaScript(confirmationMessage));
        }
        onClic.append("', '");
        onClic.append(CmsStringUtil.escapeJavaScript(getItem().getId()));
        onClic.append("');");

        if (getColumn() == null
            || getItem().get(getColumn()) == null
            || helpText.equals(new MessageFormat(helpText, wp.getLocale()).format(new Object[] {""}))) {
            return A_CmsHtmlIconButton.defaultButtonHtml(
                CmsHtmlIconButtonStyleEnum.SMALL_ICON_TEXT,
                id,
                getId(),
                name,
                helpText,
                isEnabled(),
                getIconPath(),
                onClic.toString(),
                true);
        }

        return A_CmsHtmlIconButton.defaultButtonHtml(
            CmsHtmlIconButtonStyleEnum.SMALL_ICON_TEXT,
            id,
            name,
            helpText,
            isEnabled(),
            getIconPath(),
            onClic.toString());
    }

    /**
     * The id of the column to use.<p>
     * 
     * @param column the column id
     */
    public void setColumn(String column) {

        m_column = column;
    }

}