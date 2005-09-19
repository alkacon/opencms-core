/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/list/Attic/A_CmsListToggleAction.java,v $
 * Date   : $Date: 2005/09/19 07:51:00 $
 * Version: $Revision: 1.11 $
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

/**
 * Abstract implementation of a toggle action for a html list.<p>
 * 
 * You have to extend this class and implement the <code>{@link #selectAction()}</code> method.<p>
 * 
 * @author Michael Moossen  
 * 
 * @version $Revision: 1.11 $ 
 * 
 * @since 6.0.0 
 */
public abstract class A_CmsListToggleAction extends CmsListDirectAction {

    /**
     * Default Constructor.<p>
     * 
     * @param id unique id
     */
    protected A_CmsListToggleAction(String id) {

        super(id);
    }

    /**
     * @see org.opencms.workplace.list.CmsListDirectAction#getColumn()
     */
    public String getColumn() {

        return selectAction().getColumn();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListAction#getConfirmationMessage()
     */
    public CmsMessageContainer getConfirmationMessage() {

        return selectAction().getConfirmationMessage();
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsHtmlIconButton#getHelpText()
     */
    public CmsMessageContainer getHelpText() {

        return selectAction().getHelpText();
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsHtmlIconButton#getIconPath()
     */
    public String getIconPath() {

        return selectAction().getIconPath();
    }

    /**
     * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#getId()
     */
    public String getId() {

        return selectAction().getId();
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsHtmlIconButton#getName()
     */
    public CmsMessageContainer getName() {

        return selectAction().getName();
    }

    /**
     * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#isEnabled()
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
}