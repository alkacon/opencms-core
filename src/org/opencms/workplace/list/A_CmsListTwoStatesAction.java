/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/list/Attic/A_CmsListTwoStatesAction.java,v $
 * Date   : $Date: 2005/05/18 13:19:27 $
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

import org.opencms.file.CmsObject;

/**
 * Abstract implementation of a two state action for a html list.<p>
 * 
 * You have to extend this class and implement the <code>{@link #selectAction()}</code> method,
 * where you can use the <code>{@link #getCms()}</code> to access the cms context.<p>
 * 
 * @author Michael Moossen (m.moossen@alkacon.com) 
 * @version $Revision: 1.2 $
 * @since 5.7.3
 */
public abstract class A_CmsListTwoStatesAction extends A_CmsListToggleAction {

    /** The cms context. */
    private CmsObject m_cms;

    /** The activation action. */
    private I_CmsListDirectAction m_firstAction;
    /** The desactivation action. */
    private I_CmsListDirectAction m_secondAction;

    /**
     * Default Constructor.<p>
     * 
     * @param listId the id of the associated list
     * @param id unique id
     * @param cms the cms context
     */
    protected A_CmsListTwoStatesAction(String listId, String id, CmsObject cms) {

        super(listId, id);
        m_cms = cms;
    }

    /**
     * Full Constructor.<p>
     * 
     * @param listId the id of the associated list
     * @param id unique id
     * @param cms the cms context
     * @param firstAction the first action
     * @param secondAction the second action
     */
    protected A_CmsListTwoStatesAction(
        String listId,
        String id,
        CmsObject cms,
        I_CmsListDirectAction firstAction,
        I_CmsListDirectAction secondAction) {

        this(listId, id, cms);
        setFirstAction(firstAction);
        setSecondAction(secondAction);
    }

    /**
     * Returns the cms context object.<p>
     *
     * @return the cms context
     */
    public CmsObject getCms() {

        return m_cms;
    }

    /**
     * Returns the first Action.<p>
     *
     * @return the first Action
     */
    public I_CmsListDirectAction getFirstAction() {

        return m_firstAction;
    }

    /**
     * Returns the second Action.<p>
     *
     * @return the second Action
     */
    public I_CmsListDirectAction getSecondAction() {

        return m_secondAction;
    }

    /**
     * Sets the first Action.<p>
     *
     * @param firstAction the activation Action to set
     */
    public void setFirstAction(I_CmsListDirectAction firstAction) {

        m_firstAction = firstAction;
    }

    /**
     * Sets the second Action.<p>
     *
     * @param secondAction the second Action to set
     */
    public void setSecondAction(I_CmsListDirectAction secondAction) {

        m_secondAction = secondAction;
    }
}