/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/list/A_CmsListAction.java,v $
 * Date   : $Date: 2005/06/23 10:47:20 $
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
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.tools.A_CmsHtmlIconButton;

/**
 * The default skeleton for a list action.<p>
 * 
 * @author Michael Moossen  
 * 
 * @version $Revision: 1.14 $ 
 * 
 * @since 6.0.0 
 */
public abstract class A_CmsListAction extends A_CmsHtmlIconButton implements I_CmsListAction {

    /** Confirmation Message. */
    private CmsMessageContainer m_confirmationMsg;

    /** The id of the associated list. */
    private String m_listId;

    /**
     * Default Constructor.<p>
     * 
     * @param id unique id
     */
    public A_CmsListAction(String id) {

        super(id);
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(id)) {
            throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_LIST_INVALID_NULL_ARG_1, "id"));
        }
        setConfirmationMessage(null);
    }

    /**
     * Generates html for the confirmation message when having one confirmation message
     * for several actions.<p>
     * 
     * @param confId the id of the confirmation message
     * @param confText the confirmation message
     * 
     * @return html code
     */
    public static String defaultConfirmationHtml(String confId, String confText) {

        StringBuffer html = new StringBuffer(1024);
        html.append("<div class='hide' id='conf");
        html.append(confId);
        html.append("'>");
        html.append(CmsStringUtil.isEmptyOrWhitespaceOnly(confText) ? "null" : confText);
        html.append("</div>\n");
        return html.toString();
    }

    /**
     * @see org.opencms.workplace.list.I_CmsListAction#getConfirmationMessage()
     */
    public CmsMessageContainer getConfirmationMessage() {

        return m_confirmationMsg;
    }

    /**
     * @see org.opencms.workplace.list.I_CmsListAction#getListId()
     */
    public String getListId() {

        return m_listId;
    }

    /**
     * @see org.opencms.workplace.list.I_CmsListAction#setConfirmationMessage(org.opencms.i18n.CmsMessageContainer)
     */
    public void setConfirmationMessage(CmsMessageContainer confirmationMsg) {

        if (confirmationMsg == null) {
            confirmationMsg = C_EMPTY_MESSAGE;
        }
        m_confirmationMsg = confirmationMsg;
    }

    /**
     * @see org.opencms.workplace.list.I_CmsListAction#setListId(java.lang.String)
     */
    public void setListId(String listId) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(listId)) {
            throw new CmsIllegalArgumentException(Messages.get().container(
                Messages.ERR_LIST_INVALID_NULL_ARG_1,
                "listId"));
        }
        m_listId = listId;
    }
}