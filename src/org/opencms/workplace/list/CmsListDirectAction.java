/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/list/CmsListDirectAction.java,v $
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
 * Default implementation of a direct action for a html list column.<p>
 * 
 * @author Michael Moossen  
 * 
 * @version $Revision: 1.14 $ 
 * 
 * @since 6.0.0 
 */
public class CmsListDirectAction extends A_CmsListAction implements I_CmsListDirectAction {

    /** Id of the Column to use. */
    private String m_columnId;

    /** List item. */
    private CmsListItem m_listItem;

    /**
     * Default Constructor.<p>
     * 
     * @param id unique id
     */
    public CmsListDirectAction(String id) {

        super(id);
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsHtmlIconButton#buttonHtml(CmsWorkplace)
     */
    public String buttonHtml(CmsWorkplace wp) {

        String id = getId() + getItem().getId();
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
            || getItem().get(getColumn()) == null
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
                CmsHtmlIconButtonStyleEnum.SMALL_ICON_ONLY,
                id,
                getId(),
                getName().key(wp.getLocale()),
                helpText,
                isEnabled(),
                getIconPath(),
                onClic.toString(),
                true);
        }
        return A_CmsHtmlIconButton.defaultButtonHtml(CmsHtmlIconButtonStyleEnum.SMALL_ICON_ONLY, id, getName().key(
            wp.getLocale()), helpText, isEnabled(), getIconPath(), onClic.toString());
    }

    /**
     * @see org.opencms.workplace.list.I_CmsListDirectAction#confirmationTextHtml(org.opencms.workplace.CmsWorkplace)
     */
    public String confirmationTextHtml(CmsWorkplace wp) {

        StringBuffer html = new StringBuffer(512);
        String cm = getConfirmationMessage().key(wp.getLocale());
        String confMessage = new MessageFormat(cm, wp.getLocale()).format(new Object[] {""});
        if (getColumn() == null
            || confMessage.equals(new MessageFormat(cm, wp.getLocale()).format(new Object[] {getItem().get(getColumn())}))) {
            html.append(A_CmsListAction.defaultConfirmationHtml(getId(), confMessage));
        }
        return html.toString();
    }

    /**
     * @see org.opencms.workplace.list.I_CmsListDirectAction#getColumn()
     */
    public String getColumn() {

        return m_columnId;
    }

    /**
     * @see org.opencms.workplace.list.I_CmsListDirectAction#getItem()
     */
    public CmsListItem getItem() {

        return m_listItem;
    }

    /**
     * @see org.opencms.workplace.list.I_CmsListDirectAction#helpTextHtml(org.opencms.workplace.CmsWorkplace)
     */
    public String helpTextHtml(CmsWorkplace wp) {

        StringBuffer html = new StringBuffer(512);
        String ht = getHelpText().key(wp.getLocale());
        String helptext = new MessageFormat(ht, wp.getLocale()).format(new Object[] {""});
        if (getColumn() == null
            || helptext.equals(new MessageFormat(ht, wp.getLocale()).format(new Object[] {getItem().get(getColumn())}))) {
            html.append(A_CmsHtmlIconButton.defaultHelpHtml(getId(), helptext));
        }
        return html.toString();
    }

    /**
     * @see org.opencms.workplace.list.I_CmsListDirectAction#setColumn(java.lang.String)
     */
    public void setColumn(String columnId) {

        m_columnId = columnId;
    }

    /**
     * @see org.opencms.workplace.list.I_CmsListDirectAction#setItem(org.opencms.workplace.list.CmsListItem)
     */
    public void setItem(CmsListItem item) {

        m_listItem = item;
    }

}