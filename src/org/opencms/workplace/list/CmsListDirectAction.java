/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/list/CmsListDirectAction.java,v $
 * Date   : $Date: 2005/10/13 11:06:32 $
 * Version: $Revision: 1.18 $
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
import java.util.Locale;

/**
 * Default implementation of a direct action for a html list column.<p>
 * 
 * @author Michael Moossen  
 * 
 * @version $Revision: 1.18 $ 
 * 
 * @since 6.0.0 
 */
public class CmsListDirectAction extends A_CmsListAction implements I_CmsListDirectAction {

    /** Id of the Column to use when setting param in help and conf texts. */
    private String m_columnForTexts;

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
     * Help method to resolve the help text to use.<p>
     * 
     * @param locale the used locale
     * 
     * @return the help text
     */
    protected String resolveHelpText(Locale locale) {

        String helpText = getHelpText().key(locale);
        if (getColumnForTexts() != null && getItem().get(getColumnForTexts()) != null) {
            helpText = new MessageFormat(helpText, locale).format(new Object[] {getItem().get(getColumnForTexts())});
        }
        return helpText;
    }

    /**
     * Help method to resolve the on clic text to use.<p>
     * 
     * @param locale the used locale
     * 
     * @return the on clic text
     */
    protected String resolveOnClic(Locale locale) {

        String confirmationMessage = getConfirmationMessage().key(locale);
        if (getColumnForTexts() != null && getItem().get(getColumnForTexts()) != null) {
            confirmationMessage = new MessageFormat(confirmationMessage, locale).format(new Object[] {getItem().get(
                getColumnForTexts())});
        }
        StringBuffer onClic = new StringBuffer(128);
        onClic.append("listAction('");
        onClic.append(getListId());
        onClic.append("', '");
        onClic.append(getId());
        onClic.append("', '");
        if (getColumnForTexts() == null
            || getItem().get(getColumnForTexts()) == null
            || confirmationMessage.equals(new MessageFormat(confirmationMessage, locale).format(new Object[] {""}))) {
            onClic.append("conf" + getId());
        } else {
            onClic.append(CmsStringUtil.escapeJavaScript(confirmationMessage));
        }
        onClic.append("', '");
        onClic.append(CmsStringUtil.escapeJavaScript(getItem().getId()));
        onClic.append("');");
        return onClic.toString();
    }

    /**
     * Help method to resolve the name to use.<p>
     * 
     * @param locale the used locale
     * 
     * @return the name
     */
    protected String resolveName(Locale locale) {

        return getName().key(locale);
    }

    /**
     * Help method to resolve the style of the button.<p>
     * 
     * @return the style of the button
     */
    protected CmsHtmlIconButtonStyleEnum resolveButtonStyle() {
        
        return CmsHtmlIconButtonStyleEnum.SMALL_ICON_ONLY;
    }
    
    /**
     * @see org.opencms.workplace.tools.I_CmsHtmlIconButton#buttonHtml(CmsWorkplace)
     */
    public String buttonHtml(CmsWorkplace wp) {

        if (!isVisible()) {
            return "";
        }
        return A_CmsHtmlIconButton.defaultButtonHtml(
            wp.getJsp(),
            resolveButtonStyle(),
            getId() + getItem().getId(),
            getId(),
            resolveName(wp.getLocale()),
            resolveHelpText(wp.getLocale()),
            isEnabled(),
            getIconPath(),
            null,
            resolveOnClic(wp.getLocale()),
            getColumnForTexts() == null);
    }

    /**
     * @see org.opencms.workplace.list.I_CmsListDirectAction#confirmationTextHtml(org.opencms.workplace.CmsWorkplace)
     */
    public String confirmationTextHtml(CmsWorkplace wp) {

        StringBuffer html = new StringBuffer(512);
        String cm = getConfirmationMessage().key(wp.getLocale());
        String confMessage = new MessageFormat(cm, wp.getLocale()).format(new Object[] {""});
        if (getColumnForTexts() == null
            || confMessage.equals(new MessageFormat(cm, wp.getLocale()).format(new Object[] {getItem().get(getColumnForTexts())}))) {
            html.append(A_CmsListAction.defaultConfirmationHtml(getId(), confMessage));
        }
        return html.toString();
    }

    /**
     * @see org.opencms.workplace.list.I_CmsListDirectAction#getColumnForTexts()
     */
    public String getColumnForTexts() {

        return m_columnForTexts;
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
        if (getColumnForTexts() == null
            || helptext.equals(new MessageFormat(ht, wp.getLocale()).format(new Object[] {getItem().get(getColumnForTexts())}))) {
            html.append(A_CmsHtmlIconButton.defaultHelpHtml(getId(), helptext));
        }
        return html.toString();
    }

    /**
     * @see org.opencms.workplace.list.I_CmsListDirectAction#setColumnForTexts(java.lang.String)
     */
    public void setColumnForTexts(String columnId) {

        m_columnForTexts = columnId;
    }

    /**
     * @see org.opencms.workplace.list.I_CmsListDirectAction#setItem(org.opencms.workplace.list.CmsListItem)
     */
    public void setItem(CmsListItem item) {

        m_listItem = item;
    }

}