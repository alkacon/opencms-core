/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/list/A_CmsListAction.java,v $
 * Date   : $Date: 2005/05/10 11:26:53 $
 * Version: $Revision: 1.5 $
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

/**
 * The default skeleton for a list action.<p>
 * 
 * @author Michael Moossen (m.moossen@alkacon.com) 
 * @version $Revision: 1.5 $
 * @since 5.7.3
 */
public abstract class A_CmsListAction extends A_CmsHtmlIconButton implements I_CmsListAction {

    /** Confirmation Message. */
    private CmsMessageContainer m_confirmationMsg;

    /** The id of the associated list. */
    private final String m_listId;

    /**
     * Default Constructor.<p>
     * 
     * @param listId the id of the list
     * @param id unique id
     */
    public A_CmsListAction(String listId, String id) {
        
        super(id);
        m_listId = listId;
    }
    
    
    /**
     * Full Constructor.<p>
     * 
     * @param listId the id of the list
     * @param id unique id
     * @param name the name
     * @param helpText the help text
     * @param iconPath the link to the icon
     * @param enabled <code>true</code> if enabled
     * @param confirmationMessage the confirmation message
     */
    public A_CmsListAction(
        String listId,
        String id,
        CmsMessageContainer name,
        CmsMessageContainer helpText,
        String iconPath,
        boolean enabled,
        CmsMessageContainer confirmationMessage) {

        this(listId, id);
        setName(name);
        setHelpText(helpText);
        setEnabled(enabled);
        setIconPath(iconPath);
        setConfirmationMessage(confirmationMessage);
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
            confirmationMsg = Messages.get().container(Messages.GUI_LIST_EMPTY_MESSAGE_0);
        }
        m_confirmationMsg = confirmationMsg;
    }
}