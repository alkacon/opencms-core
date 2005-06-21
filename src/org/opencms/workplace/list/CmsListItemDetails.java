/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/list/CmsListItemDetails.java,v $
 * Date   : $Date: 2005/06/21 15:54:15 $
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
 * For adding detail information to the list items add an instance 
 * of this class to the list metadata and fill the data like an
 * additional column.<p>
 * 
 * For detail contents you may use HTML code.<p>
 * 
 * @author Michael Moossen (m.moossen@alkacon.com)
 * @version $Revision: 1.5 $
 * @since 5.7.3
 */
public class CmsListItemDetails extends CmsListColumnDefinition {

    /** Id of the first column to include. */
    private String m_atColumn;

    /** Action for hidding the details. */
    private I_CmsListAction m_hideAction;

    /** Action for showing the details. */
    private I_CmsListAction m_showAction;

    /**
     * Default constructor.<p>
     * 
     * @param id the unique id
     */
    public CmsListItemDetails(String id) {

        super(id);
        // set default actions
        setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        m_hideAction = new CmsListIndependentAction(id);
        m_hideAction.setIconPath(A_CmsListDialog.ICON_DETAILS_HIDE);
        m_showAction = new CmsListIndependentAction(id);
        m_showAction.setIconPath(A_CmsListDialog.ICON_DETAILS_SHOW);
    }

    /**
     * Sets the id of the first column to include.<p>
     *
     * @param atColumn the id of the first column to set
     */
    public void setAtColumn(String atColumn) {

        m_atColumn = atColumn;
    }

    /**
     * Sets the hide details Action.<p>
     *
     * @param hideAction the hide details Action to set
     */
    public void setHideAction(I_CmsListAction hideAction) {

        m_hideAction = hideAction;
    }

    /**
     * Sets the show details Action.<p>
     *
     * @param showAction the showdetails Action to set
     */
    public void setShowAction(I_CmsListAction showAction) {

        m_showAction = showAction;
    }

    /**
     * Sets the name of the show action.<p>
     * 
     * @param showActionName the name of the show action
     */
    public void setShowActionName(CmsMessageContainer showActionName) {

        m_showAction.setName(showActionName);
    }

    /**
     * Sets the help text of the show action.<p>
     * 
     * @param showActionHelp the help text of the show action
     */
    public void setShowActionHelpText(CmsMessageContainer showActionHelp) {

        m_showAction.setHelpText(showActionHelp);
    }

    /**
     * Sets the name of the hide action.<p>
     * 
     * @param hideActionName the name of the hide action
     */
    public void setHideActionName(CmsMessageContainer hideActionName) {

        m_hideAction.setName(hideActionName);
    }

    /**
     * Sets the help text of the hide action.<p>
     * 
     * @param hideActionHelp the help text of the hide action
     */
    public void setHideActionHelpText(CmsMessageContainer hideActionHelp) {

        m_hideAction.setHelpText(hideActionHelp);
    }

    /**
     * Returns the current active action.<p>
     *
     * @return the current active action
     */
    public I_CmsListAction getAction() {

        return isVisible() ? m_hideAction : m_showAction;
    }

    /**
     * Returns the id of the first column to include.<p>
     *
     * @return the id of the first column to include
     */
    public String getAtColumn() {

        return m_atColumn;
    }
    
    /**
     * Sets the id of the list.<p>
     * 
     * @param listId the id of the list
     */
    public void setListId(String listId) {
        
        m_hideAction.setListId(listId);
        m_showAction.setListId(listId);
    }
}
