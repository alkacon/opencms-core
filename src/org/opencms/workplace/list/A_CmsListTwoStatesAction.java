/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/list/Attic/A_CmsListTwoStatesAction.java,v $
 * Date   : $Date: 2005/06/23 10:47:20 $
 * Version: $Revision: 1.7 $
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

import org.opencms.file.CmsObject;
import org.opencms.workplace.CmsWorkplace;

/**
 * Abstract implementation of a two state action for a html list.<p>
 * 
 * You have to extend this class and implement the <code>{@link #selectAction()}</code> method,
 * where you can use the <code>{@link #getCms()}</code> to access the cms context.<p>
 * 
 * @author Michael Moossen  
 * 
 * @version $Revision: 1.7 $ 
 * 
 * @since 6.0.0 
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
     * Be careful while using the cms object to select the proper action, 
     * this cms object will be set only once the first time the action is needed, after that
     * every user/session that may access to this action will be using the same cms object.<p>
     * 
     * So use only methods that do not need role checks and those that do not are current user dependent.<p>  
     * 
     * @param id the unique id
     * @param cms the cms context
     */
    protected A_CmsListTwoStatesAction(String id, CmsObject cms) {

        super(id);
        m_cms = cms;
    }

    /**
     * @see org.opencms.workplace.list.CmsListDirectAction#confirmationTextHtml(org.opencms.workplace.CmsWorkplace)
     */
    public String confirmationTextHtml(CmsWorkplace wp) {

        return m_firstAction.confirmationTextHtml(wp) + m_secondAction.confirmationTextHtml(wp);
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
     * @see org.opencms.workplace.list.CmsListDirectAction#helpTextHtml(org.opencms.workplace.CmsWorkplace)
     */
    public String helpTextHtml(CmsWorkplace wp) {

        return m_firstAction.helpTextHtml(wp) + m_secondAction.helpTextHtml(wp);
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
     * @see org.opencms.workplace.list.CmsListDirectAction#setItem(org.opencms.workplace.list.CmsListItem)
     */
    public void setItem(CmsListItem item) {

        super.setItem(item);
        getFirstAction().setItem(item);
        getSecondAction().setItem(item);
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