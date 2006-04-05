/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/accounts/CmsShowGroupUsersList.java,v $
 * Date   : $Date: 2006/03/27 14:52:49 $
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

package org.opencms.workplace.tools.accounts;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListMetadata;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * User groups overview view.<p>
 * 
 * @author Michael Moossen  
 * 
 * @version $Revision: 1.11 $ 
 * 
 * @since 6.0.0 
 */
public class CmsShowGroupUsersList extends A_CmsGroupUsersList {

    /** list id constant. */
    public static final String LIST_ID = "lsgu";

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsShowGroupUsersList(CmsJspActionElement jsp) {

        this(jsp, LIST_ID);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsShowGroupUsersList(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Protected constructor.<p>
     * @param jsp an initialized JSP action element
     * @param listId the id of the specialized list
     */
    protected CmsShowGroupUsersList(CmsJspActionElement jsp, String listId) {

        super(jsp, listId, Messages.get().container(Messages.GUI_GROUPUSERS_LIST_NAME_0), false);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#defaultActionHtmlStart()
     */
    public String defaultActionHtmlStart() {

        return getList().listJs(getLocale()) + dialogContentStart(getParamTitle());
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListMultiActions()
     */
    public void executeListMultiActions() throws CmsRuntimeException {

        throwListUnsupportedActionException();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListSingleActions()
     */
    public void executeListSingleActions() throws CmsRuntimeException {

        throwListUnsupportedActionException();
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsGroupUsersList#getUsers()
     */
    protected List getUsers() throws CmsException {

        return getCms().getUsersOfGroup(getParamGroupname());
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsGroupUsersList#setDefaultAction(org.opencms.workplace.list.CmsListColumnDefinition)
     */
    protected void setDefaultAction(CmsListColumnDefinition loginCol) {

        // no-op        
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsGroupUsersList#setIconAction(org.opencms.workplace.list.CmsListColumnDefinition)
     */
    protected void setIconAction(CmsListColumnDefinition iconCol) {

        CmsListDirectAction iconAction = new CmsListDirectAction(LIST_ACTION_ICON);
        iconAction.setName(Messages.get().container(Messages.GUI_USERS_LIST_INGROUP_NAME_0));
        iconAction.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_INGROUP_HELP_0));
        iconAction.setIconPath(A_CmsUsersList.PATH_BUTTONS + "user.png");
        iconAction.setEnabled(false);
        iconCol.addDirectAction(iconAction);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setMultiActions(CmsListMetadata metadata) {

        // noop
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsGroupUsersList#setStateActionCol(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setStateActionCol(CmsListMetadata metadata) {

        // no-op
    }
}
